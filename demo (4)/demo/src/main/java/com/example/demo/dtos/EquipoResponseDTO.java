package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EquipoResponseDTO {
    private Long id;
    private String nombre;
    private String abreviatura;
    private String imagenUrl;
}
