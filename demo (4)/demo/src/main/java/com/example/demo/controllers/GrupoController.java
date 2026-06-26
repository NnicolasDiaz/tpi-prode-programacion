package com.example.demo.controllers;

import com.example.demo.dtos.GrupoRequestDTO;
import com.example.demo.dtos.GrupoResponseDTO;
import com.example.demo.dtos.RankingItemDTO;
import com.example.demo.dtos.UnirseGrupoDTO;
import com.example.demo.models.Usuario;
import com.example.demo.services.GrupoService;
import com.example.demo.services.RankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;
    private final RankingService rankingService;

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> crear(
            @Valid @RequestBody GrupoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(grupoService.crearGrupo(dto, usuario));
    }

    @PostMapping("/unirse")
    public ResponseEntity<GrupoResponseDTO> unirse(
            @Valid @RequestBody UnirseGrupoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(grupoService.unirseAGrupo(dto.getCodigoDeAcceso(), usuario));
    }

    @GetMapping
    public ResponseEntity<List<GrupoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(grupoService.listarTodos());
    }

    @GetMapping("/mis-grupos")
    public ResponseEntity<List<GrupoResponseDTO>> misGrupos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(grupoService.listarMisGrupos(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.obtenerGrupo(id));
    }

    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<RankingItemDTO>> rankingGrupo(@PathVariable Long id) {
        return ResponseEntity.ok(rankingService.rankingGrupo(id));
    }

    @DeleteMapping("/{id}/abandonar")
    public ResponseEntity<Void> abandonar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        grupoService.abandonarGrupo(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        grupoService.eliminarGrupo(id);
        return ResponseEntity.noContent().build();
    }
}
