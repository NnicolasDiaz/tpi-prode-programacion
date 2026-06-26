package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingFechaItemDTO {
    private int posicion;
    private String nombre;
    private String apellido;
    private int puntos;
    private int cantidadExactos;
    private int totalPronosticos;
}
