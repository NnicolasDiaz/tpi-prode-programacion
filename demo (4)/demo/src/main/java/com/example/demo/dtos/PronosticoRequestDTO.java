package com.example.demo.dtos;

import lombok.Data;

@Data
public class PronosticoRequestDTO {
    private Long partidoId;
    private Integer golesLocal;
    private Integer golesVisitante;
}
