package com.example.demo.services;

import com.example.demo.dtos.RankingItemDTO;
import com.example.demo.models.Grupo;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.GrupoRepository;
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

    public List<RankingItemDTO> rankingGlobal() {
        List<Usuario> usuarios = usuarioRepository
                .findByFechaEliminacionIsNullOrderByPuntosDescCantidadExactosDesc();
        return buildRanking(usuarios);
    }

    public List<RankingItemDTO> rankingGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));

        List<Usuario> integrantes = new ArrayList<>(grupo.getIntegrantes());
        integrantes.sort(Comparator
                .comparingInt(Usuario::getPuntos).reversed()
                .thenComparingInt(Usuario::getCantidadExactos).reversed());
        return buildRanking(integrantes);
    }

    private List<RankingItemDTO> buildRanking(List<Usuario> usuarios) {
        // Tercer criterio de desempate: pronóstico más antiguo (RF7.2)
        Map<Long, LocalDateTime> oldest = pronosticoRepository.findOldestPronosticoDates()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (LocalDateTime) row[1]
                ));

        usuarios.sort(Comparator
                .comparingInt(Usuario::getPuntos).reversed()
                .thenComparingInt(Usuario::getCantidadExactos).reversed()
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
