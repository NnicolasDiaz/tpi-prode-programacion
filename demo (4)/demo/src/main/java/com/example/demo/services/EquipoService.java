package com.example.demo.services;

import com.example.demo.dtos.EquipoRequestDTO;
import com.example.demo.dtos.EquipoResponseDTO;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Equipo;
import com.example.demo.repositories.EquipoRepository;
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
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final PartidoRepository partidoRepository;

    public EquipoResponseDTO crearEquipo(EquipoRequestDTO dto) {
        if (equipoRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe un equipo con el nombre: " + dto.getNombre());
        }
        String abrev = (dto.getAbreviatura() != null && !dto.getAbreviatura().isBlank())
                ? dto.getAbreviatura()
                : "";
        Equipo equipo = Equipo.builder()
                .nombre(dto.getNombre())
                .abreviatura(abrev)
                .imagenUrl(dto.getImagenUrl())
                .build();
        return toDTO(equipoRepository.save(equipo));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarEquipos() {
        return equipoRepository.findByFechaEliminacionIsNull()
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponseDTO buscarPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    public void eliminarEquipo(Long id) {
        Equipo equipo = findOrThrow(id);
        if (partidoRepository.existsByEquipoLocalIdOrEquipoVisitanteId(id, id)) {
            throw new BusinessException("No se puede eliminar: el equipo tiene partidos asociados");
        }
        equipo.setFechaEliminacion(LocalDateTime.now(ZoneOffset.UTC));
        equipoRepository.save(equipo);
    }

    private Equipo findOrThrow(Long id) {
        return equipoRepository.findById(id)
                .filter(e -> e.getFechaEliminacion() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    private EquipoResponseDTO toDTO(Equipo e) {
        return new EquipoResponseDTO(e.getId(), e.getNombre(), e.getAbreviatura(), e.getImagenUrl());
    }
}
