package com.example.demo.dtos;

import lombok.Data;

@Data
public class RegistroUsuarioDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
}
