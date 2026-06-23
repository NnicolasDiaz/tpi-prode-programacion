package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FechaResponseDTO {
    private Long id;
    private String nombre;
    private String estado;
}
