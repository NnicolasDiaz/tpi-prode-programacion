package com.example.demo.services;

import com.example.demo.dtos.PronosticoRequestDTO;
import com.example.demo.dtos.PronosticoResponseDTO;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Partido;
import com.example.demo.models.Pronostico;
import com.example.demo.models.Usuario;
import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.repositories.PartidoRepository;
import com.example.demo.repositories.PronosticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PronosticoService {

    private final PronosticoRepository pronosticoRepository;
    private final PartidoRepository partidoRepository;

    public PronosticoResponseDTO crearOModificar(PronosticoRequestDTO dto, Usuario usuario) {
        Partido partido = partidoRepository.findById(dto.getPartidoId())
                .filter(p -> p.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        if (partido.getEstado() != EstadoPartido.POR_JUGARSE) {
            throw new BusinessException("Solo se puede pronosticar en partidos POR_JUGARSE");
        }

        // Regla crítica: bloqueo 30 minutos antes del inicio (RF1 y RF5.1)
        LocalDateTime horaLimite = partido.getFechaHoraInicio().minusMinutes(30);
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(horaLimite)) {
            throw new BusinessException("Tiempo expirado: no se puede pronosticar a menos de 30 minutos del inicio");
        }

        Optional<Pronostico> existente = pronosticoRepository
                .findByUsuarioIdAndPartidoId(usuario.getId(), partido.getId());

        Pronostico pronostico;
        if (existente.isPresent()) {
            pronostico = existente.get();
            pronostico.setGolesLocal(dto.getGolesLocal());
            pronostico.setGolesVisitante(dto.getGolesVisitante());
        } else {
            pronostico = Pronostico.builder()
                    .usuario(usuario)
                    .partido(partido)
                    .golesLocal(dto.getGolesLocal())
                    .golesVisitante(dto.getGolesVisitante())
                    .build();
        }

        return toDTO(pronosticoRepository.save(pronostico));
    }

    @Transactional(readOnly = true)
    public List<PronosticoResponseDTO> misPronosticos(Usuario usuario, EstadoPartido estadoFiltro) {
        return pronosticoRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(p -> estadoFiltro == null || p.getPartido().getEstado() == estadoFiltro)
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PronosticoResponseDTO> pronosticosDeTerceros(Long partidoId, Usuario usuarioActual) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        // Privacidad: solo se pueden ver después de que expire el bloqueo (RF5.3)
        LocalDateTime horaLimite = partido.getFechaHoraInicio().minusMinutes(30);
        if (LocalDateTime.now(ZoneOffset.UTC).isBefore(horaLimite)) {
            throw new BusinessException("No se pueden ver los pronósticos de otros usuarios hasta 30 minutos antes del inicio");
        }

        return pronosticoRepository.findByPartidoId(partidoId).stream()
                .filter(p -> !p.getUsuario().getId().equals(usuarioActual.getId()))
                .map(this::toDTO)
                .toList();
    }

    private PronosticoResponseDTO toDTO(Pronostico p) {
        Partido partido = p.getPartido();
        return new PronosticoResponseDTO(
                p.getId(),
                partido.getId(),
                partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getNombre(),
                partido.getFechaHoraInicio(),
                partido.getEstado().name(),
                p.getGolesLocal(),
                p.getGolesVisitante(),
                p.getResultadoPronostico() != null ? p.getResultadoPronostico().name() : null,
                p.getFechaCreacion()
        );
    }
}
