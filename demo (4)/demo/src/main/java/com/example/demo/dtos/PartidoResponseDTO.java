package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PartidoResponseDTO {
    private Long id;
    private Long fechaId;
    private String fechaNombre;
    private Long equipoLocalId;
    private String equipoLocalNombre;
    private Long equipoVisitanteId;
    private String equipoVisitanteNombre;
    private LocalDateTime fechaHoraInicio;
    private String estado;
    private Integer golesLocal;
    private Integer golesVisitante;
    private String resultado;
}
