package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnirseGrupoDTO {

    @NotBlank(message = "El código de acceso es obligatorio")
    private String codigoDeAcceso;
}
