package com.example.demo.services;

import com.example.demo.dtos.FechaRequestDTO;
import com.example.demo.dtos.FechaResponseDTO;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Fecha;
import com.example.demo.models.Partido;
import com.example.demo.models.enums.EstadoFecha;
import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.repositories.FechaRepository;
import com.example.demo.repositories.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FechaService {

    private final FechaRepository fechaRepository;
    private final PartidoRepository partidoRepository;

    public FechaResponseDTO crearFecha(FechaRequestDTO dto) {
        if (fechaRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una fecha con ese nombre");
        }
        Fecha fecha = Fecha.builder().nombre(dto.getNombre()).build();
        return toDTO(fechaRepository.save(fecha));
    }

    @Transactional(readOnly = true)
    public List<FechaResponseDTO> listarFechas(EstadoFecha estado) {
        List<Fecha> fechas = (estado != null)
                ? fechaRepository.findByEstadoAndFechaEliminacionIsNull(estado)
                : fechaRepository.findAll();
        return fechas.stream()
                .filter(f -> f.getFechaEliminacion() == null)
                .map(this::toDTO).toList();
    }

    public FechaResponseDTO modificarNombre(Long id, FechaRequestDTO dto) {
        Fecha fecha = findOrThrow(id);
        if (fecha.getEstado() != EstadoFecha.PROGRAMADA) {
            throw new BusinessException("Solo se puede modificar una fecha en estado PROGRAMADA");
        }
        if (fechaRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una fecha con ese nombre");
        }
        fecha.setNombre(dto.getNombre());
        return toDTO(fechaRepository.save(fecha));
    }

    public void eliminarFecha(Long id) {
        Fecha fecha = findOrThrow(id);
        fecha.setFechaEliminacion(LocalDateTime.now(ZoneOffset.UTC));
        fechaRepository.save(fecha);
    }

    public void recalcularEstado(Long fechaId) {
        Fecha fecha = findOrThrow(fechaId);
        List<Partido> partidos = partidoRepository.findByFechaIdOrderByFechaHoraInicioAsc(fechaId);

        EstadoFecha nuevoEstado;
        if (partidos.isEmpty()) {
            nuevoEstado = EstadoFecha.PROGRAMADA;
        } else if (partidos.stream().anyMatch(p -> p.getEstado() == EstadoPartido.EN_JUEGO)) {
            nuevoEstado = EstadoFecha.EN_JUEGO;
        } else if (partidos.stream().allMatch(p -> p.getEstado() == EstadoPartido.FINALIZADO)) {
            nuevoEstado = EstadoFecha.FINALIZADA;
        } else {
            nuevoEstado = EstadoFecha.PROGRAMADA;
        }

        fecha.setEstado(nuevoEstado);
        fechaRepository.save(fecha);
    }

    private Fecha findOrThrow(Long id) {
        return fechaRepository.findById(id)
                .filter(f -> f.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada: " + id));
    }

    private FechaResponseDTO toDTO(Fecha f) {
        return new FechaResponseDTO(f.getId(), f.getNombre(), f.getEstado().name());
    }
}
