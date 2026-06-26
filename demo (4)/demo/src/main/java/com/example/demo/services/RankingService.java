package com.example.demo.services;

import com.example.demo.dtos.RankingFechaItemDTO;
import com.example.demo.dtos.RankingItemDTO;
import com.example.demo.models.Grupo;
import com.example.demo.models.Partido;
import com.example.demo.models.Pronostico;
import com.example.demo.models.Rol;
import com.example.demo.models.Usuario;
import com.example.demo.models.enums.ResultadoPronostico;
import com.example.demo.repositories.GrupoRepository;
import com.example.demo.repositories.PartidoRepository;
import com.example.demo.repositories.PronosticoRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final UsuarioRepository usuarioRepository;
    private final PronosticoRepository pronosticoRepository;
    private final GrupoRepository grupoRepository;
    private final PartidoRepository partidoRepository;

    public List<RankingItemDTO> rankingGlobal() {
        List<Usuario> usuarios = usuarioRepository
                .findByFechaEliminacionIsNullAndRolNotOrderByPuntosDescCantidadExactosDesc(Rol.ADMINISTRADOR);
        return buildRanking(usuarios);
    }

    public List<RankingItemDTO> rankingGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));

        List<Usuario> integrantes = new ArrayList<>(grupo.getIntegrantes());
        return buildRanking(integrantes);
    }

    public List<RankingFechaItemDTO> rankingPorFecha(Long fechaId) {
        List<Long> partidoIds = partidoRepository.findByFechaIdOrderByFechaHoraInicioAsc(fechaId)
                .stream().map(Partido::getId).toList();

        if (partidoIds.isEmpty()) return List.of();

        List<Pronostico> pronosticos = pronosticoRepository.findByPartidoIdIn(partidoIds);

        Map<Long, List<Pronostico>> byUser = pronosticos.stream()
                .collect(Collectors.groupingBy(p -> p.getUsuario().getId()));

        List<RankingFechaItemDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Pronostico>> entry : byUser.entrySet()) {
            Usuario u = entry.getValue().get(0).getUsuario();
            if (u.getRol() == Rol.ADMINISTRADOR) continue;

            int puntos = entry.getValue().stream().mapToInt(p -> {
                if (p.getResultadoPronostico() == null) return 0;
                return switch (p.getResultadoPronostico()) {
                    case EXACTO -> 3;
                    case TENDENCIA -> 1;
                    default -> 0;
                };
            }).sum();

            int exactos = (int) entry.getValue().stream()
                    .filter(p -> p.getResultadoPronostico() == ResultadoPronostico.EXACTO).count();

            result.add(new RankingFechaItemDTO(0, u.getNombre(), u.getApellido(), puntos, exactos, entry.getValue().size()));
        }

        result.sort(
            Comparator.<RankingFechaItemDTO, Integer>comparing(RankingFechaItemDTO::getPuntos).reversed()
                .thenComparing(Comparator.<RankingFechaItemDTO, Integer>comparing(RankingFechaItemDTO::getCantidadExactos).reversed())
        );

        for (int i = 0; i < result.size(); i++) result.get(i).setPosicion(i + 1);
        return result;
    }

    private List<RankingItemDTO> buildRanking(List<Usuario> usuarios) {
        // Tercer criterio de desempate: pronóstico más antiguo (RF7.2)
        Map<Long, LocalDateTime> oldest = pronosticoRepository.findOldestPronosticoDates()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (LocalDateTime) row[1]
                ));

        usuarios.sort(
            Comparator.<Usuario, Integer>comparing(Usuario::getPuntos).reversed()
                .thenComparing(Comparator.<Usuario, Integer>comparing(Usuario::getCantidadExactos).reversed())
                .thenComparing(u -> oldest.getOrDefault(u.getId(), LocalDateTime.MAX))
        );

        List<RankingItemDTO> result = new ArrayList<>();
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            result.add(new RankingItemDTO(i + 1, u.getNombre(), u.getApellido(), u.getPuntos(), u.getCantidadExactos()));
        }
        return result;
    }
}
