package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrupoResponseDTO {
    private Long id;
    private String nombre;
    private String codigoDeAcceso;   // null en el listado público
    private int cantidadIntegrantes;
    private String creadorNombre;
    private String creadorApellido;
}
