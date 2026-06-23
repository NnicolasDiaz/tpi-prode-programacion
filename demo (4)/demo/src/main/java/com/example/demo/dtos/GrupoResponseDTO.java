package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrupoResponseDTO {
    private Long id;
    private String nombre;
    private String codigoDeAcceso;
    private int cantidadIntegrantes;
}
