package com.example.demo.services;

import com.example.demo.dtos.AplazarPartidoDTO;
import com.example.demo.dtos.PartidoRequestDTO;
import com.example.demo.dtos.PartidoResponseDTO;
import com.example.demo.dtos.ResultadoDTO;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Equipo;
import com.example.demo.models.Fecha;
import com.example.demo.models.Partido;
import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.models.enums.Resultado;
import com.example.demo.repositories.EquipoRepository;
import com.example.demo.repositories.FechaRepository;
import com.example.demo.repositories.PartidoRepository;
import com.example.demo.repositories.PronosticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final FechaRepository fechaRepository;
    private final EquipoRepository equipoRepository;
    private final PronosticoRepository pronosticoRepository;
    private final FechaService fechaService;
    private final PuntuacionService puntuacionService;

    public PartidoResponseDTO crearPartido(PartidoRequestDTO dto) {
        Fecha fecha = fechaRepository.findById(dto.getFechaId())
                .filter(f -> f.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada"));

        Equipo local = equipoRepository.findById(dto.getEquipoLocalId())
                .filter(e -> e.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo local no encontrado"));

        Equipo visitante = equipoRepository.findById(dto.getEquipoVisitanteId())
                .filter(e -> e.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo visitante no encontrado"));

        if (local.getId().equals(visitante.getId())) {
            throw new BusinessException("El equipo local y visitante no pueden ser el mismo");
        }

        Partido partido = Partido.builder()
                .fecha(fecha)
                .equipoLocal(local)
                .equipoVisitante(visitante)
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .build();

        return toDTO(partidoRepository.save(partido));
    }

    @Transactional(readOnly = true)
    public List<PartidoResponseDTO> listarPartidos(Long fechaId) {
        List<Partido> partidos = (fechaId != null)
                ? partidoRepository.findByFechaIdOrderByFechaHoraInicioAsc(fechaId)
                : partidoRepository.findAll();
        return partidos.stream()
                .filter(p -> p.getFechaEliminacion() == null)
                .map(this::toDTO).toList();
    }

    public PartidoResponseDTO modificarPartido(Long id, PartidoRequestDTO dto) {
        Partido partido = findOrThrow(id);
        if (partido.getEstado() != EstadoPartido.POR_JUGARSE) {
            throw new BusinessException("Solo se puede modificar un partido en estado POR_JUGARSE");
        }

        if (dto.getEquipoLocalId() != null && dto.getEquipoVisitanteId() != null) {
            if (dto.getEquipoLocalId().equals(dto.getEquipoVisitanteId())) {
                throw new BusinessException("El equipo local y visitante no pueden ser el mismo");
            }
            Equipo local = equipoRepository.findById(dto.getEquipoLocalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo local no encontrado"));
            Equipo visitante = equipoRepository.findById(dto.getEquipoVisitanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo visitante no encontrado"));
            partido.setEquipoLocal(local);
            partido.setEquipoVisitante(visitante);
        }

        if (dto.getFechaHoraInicio() != null) {
            partido.setFechaHoraInicio(dto.getFechaHoraInicio());
        }

        return toDTO(partidoRepository.save(partido));
    }

    public PartidoResponseDTO aplazarPartido(Long id, AplazarPartidoDTO dto) {
        Partido partido = findOrThrow(id);
        if (partido.getEstado() == EstadoPartido.EN_JUEGO || partido.getEstado() == EstadoPartido.FINALIZADO) {
            throw new BusinessException("No se puede aplazar un partido que ya está en juego o finalizado");
        }
        if (dto.getNuevaFechaHoraInicio() == null) {
            throw new BusinessException("Debe indicar la nueva fecha y hora");
        }
        partido.setFechaHoraInicio(dto.getNuevaFechaHoraInicio());
        partido.setEstado(EstadoPartido.APLAZADO);
        return toDTO(partidoRepository.save(partido));
    }

    public PartidoResponseDTO iniciarPartido(Long id) {
        Partido partido = findOrThrow(id);
        if (partido.getEstado() != EstadoPartido.POR_JUGARSE && partido.getEstado() != EstadoPartido.APLAZADO) {
            throw new BusinessException("Solo se puede iniciar un partido en estado POR_JUGARSE o APLAZADO");
        }
        partido.setEstado(EstadoPartido.EN_JUEGO);
        Partido saved = partidoRepository.save(partido);
        fechaService.recalcularEstado(partido.getFecha().getId());
        return toDTO(saved);
    }

    public PartidoResponseDTO registrarResultado(Long id, ResultadoDTO dto) {
        Partido partido = findOrThrow(id);
        if (partido.getEstado() != EstadoPartido.EN_JUEGO) {
            throw new BusinessException("Solo se puede cargar resultado de un partido EN_JUEGO");
        }

        partido.setGolesLocal(dto.getGolesLocal());
        partido.setGolesVisitante(dto.getGolesVisitante());
        partido.setResultado(calcularResultado(dto.getGolesLocal(), dto.getGolesVisitante()));
        partido.setEstado(EstadoPartido.FINALIZADO);
        Partido saved = partidoRepository.save(partido);

        puntuacionService.calcularYAsignarPuntos(saved);
        fechaService.recalcularEstado(partido.getFecha().getId());

        return toDTO(saved);
    }

    public void eliminarPartido(Long id) {
        Partido partido = findOrThrow(id);
        if (partido.getEstado() != EstadoPartido.POR_JUGARSE) {
            throw new BusinessException("Solo se puede eliminar un partido en estado POR_JUGARSE");
        }
        if (pronosticoRepository.existsByPartidoId(id)) {
            throw new BusinessException("No se puede eliminar: el partido tiene pronósticos registrados");
        }
        partido.setFechaEliminacion(LocalDateTime.now(ZoneOffset.UTC));
        partidoRepository.save(partido);
    }

    private Resultado calcularResultado(int golesLocal, int golesVisitante) {
        if (golesLocal > golesVisitante) return Resultado.LOCAL;
        if (golesVisitante > golesLocal) return Resultado.VISITANTE;
        return Resultado.EMPATE;
    }

    private Partido findOrThrow(Long id) {
        return partidoRepository.findById(id)
                .filter(p -> p.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado: " + id));
    }

    private PartidoResponseDTO toDTO(Partido p) {
        return new PartidoResponseDTO(
                p.getId(),
                p.getFecha().getId(),
                p.getFecha().getNombre(),
                p.getEquipoLocal().getId(),
                p.getEquipoLocal().getNombre(),
                p.getEquipoVisitante().getId(),
                p.getEquipoVisitante().getNombre(),
                p.getFechaHoraInicio(),
                p.getEstado().name(),
                p.getGolesLocal(),
                p.getGolesVisitante(),
                p.getResultado() != null ? p.getResultado().name() : null
        );
    }
}
