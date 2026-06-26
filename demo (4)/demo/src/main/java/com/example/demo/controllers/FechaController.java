package com.example.demo.controllers;

import com.example.demo.dtos.FechaRequestDTO;
import com.example.demo.dtos.FechaResponseDTO;
import com.example.demo.models.enums.EstadoFecha;
import com.example.demo.services.FechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fechas")
@RequiredArgsConstructor
public class FechaController {

    private final FechaService fechaService;

    @GetMapping
    public ResponseEntity<List<FechaResponseDTO>> listar(
            @RequestParam(required = false) EstadoFecha estado) {
        return ResponseEntity.ok(fechaService.listarFechas(estado));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FechaResponseDTO> crear(@Valid @RequestBody FechaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fechaService.crearFecha(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FechaResponseDTO> modificar(
            @PathVariable Long id,
            @Valid @RequestBody FechaRequestDTO dto) {
        return ResponseEntity.ok(fechaService.modificarNombre(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        fechaService.eliminarFecha(id);
        return ResponseEntity.noContent().build();
    }
}
