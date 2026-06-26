package com.example.demo.controllers;

import com.example.demo.dtos.AplazarPartidoDTO;
import com.example.demo.dtos.PartidoRequestDTO;
import com.example.demo.dtos.PartidoResponseDTO;
import com.example.demo.dtos.ResultadoDTO;
import com.example.demo.services.PartidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;

    @GetMapping
    public ResponseEntity<List<PartidoResponseDTO>> listar(
            @RequestParam(required = false) Long fechaId) {
        return ResponseEntity.ok(partidoService.listarPartidos(fechaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDTO> crear(@Valid @RequestBody PartidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partidoService.crearPartido(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDTO> modificar(
            @PathVariable Long id,
            @Valid @RequestBody PartidoRequestDTO dto) {
        return ResponseEntity.ok(partidoService.modificarPartido(id, dto));
    }

    @PatchMapping("/{id}/aplazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDTO> aplazar(
            @PathVariable Long id,
            @Valid @RequestBody AplazarPartidoDTO dto) {
        return ResponseEntity.ok(partidoService.aplazarPartido(id, dto));
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDTO> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(partidoService.iniciarPartido(id));
    }

    @PatchMapping("/{id}/resultado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PartidoResponseDTO> registrarResultado(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoDTO dto) {
        return ResponseEntity.ok(partidoService.registrarResultado(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        partidoService.eliminarPartido(id);
        return ResponseEntity.noContent().build();
    }
}
