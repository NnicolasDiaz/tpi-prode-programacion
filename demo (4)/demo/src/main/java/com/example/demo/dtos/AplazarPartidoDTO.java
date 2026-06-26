package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AplazarPartidoDTO {

    @NotNull(message = "La nueva fecha y hora de inicio son obligatorias")
    private LocalDateTime nuevaFechaHoraInicio;
}
