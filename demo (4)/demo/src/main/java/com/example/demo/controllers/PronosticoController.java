package com.example.demo.controllers;

import com.example.demo.dtos.PronosticoRequestDTO;
import com.example.demo.dtos.PronosticoResponseDTO;
import com.example.demo.models.Usuario;
import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.services.PronosticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pronosticos")
@RequiredArgsConstructor
public class PronosticoController {

    private final PronosticoService pronosticoService;

    @PostMapping
    @PreAuthorize("hasRole('JUGADOR')")
    public ResponseEntity<PronosticoResponseDTO> crearOModificar(
            @Valid @RequestBody PronosticoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pronosticoService.crearOModificar(dto, usuario));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<PronosticoResponseDTO>> misPronosticos(
            @RequestParam(required = false) EstadoPartido estado,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pronosticoService.misPronosticos(usuario, estado));
    }

    @GetMapping("/partido/{partidoId}")
    public ResponseEntity<List<PronosticoResponseDTO>> pronosticosDeTerceros(
            @PathVariable Long partidoId,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pronosticoService.pronosticosDeTerceros(partidoId, usuario));
    }
}
