package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartidoRequestDTO {

    @NotNull(message = "La jornada (fechaId) es obligatoria")
    private Long fechaId;

    @NotNull(message = "El equipo local es obligatorio")
    private Long equipoLocalId;

    @NotNull(message = "El equipo visitante es obligatorio")
    private Long equipoVisitanteId;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    private LocalDateTime fechaHoraInicio;
}
