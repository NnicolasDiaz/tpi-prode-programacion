package com.example.demo.models;

import org.springframework.security.core.GrantedAuthority;

public enum Rol implements GrantedAuthority {
    JUGADOR, ADMINISTRADOR;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
