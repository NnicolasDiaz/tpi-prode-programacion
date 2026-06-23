package com.example.demo.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartidoRequestDTO {
    private Long fechaId;
    private Long equipoLocalId;
    private Long equipoVisitanteId;
    private LocalDateTime fechaHoraInicio;
}
