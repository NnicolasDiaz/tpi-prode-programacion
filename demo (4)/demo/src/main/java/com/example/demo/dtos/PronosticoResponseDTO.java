package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PronosticoResponseDTO {
    private Long id;
    private Long partidoId;
    private String equipoLocal;
    private String equipoVisitante;
    private LocalDateTime fechaHoraInicioPartido;
    private String estadoPartido;
    private Integer golesLocal;
    private Integer golesVisitante;
    private String resultadoPronostico;
    private LocalDateTime fechaCreacion;
}
