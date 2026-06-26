package com.example.demo.controllers;

import com.example.demo.dtos.RankingFechaItemDTO;
import com.example.demo.dtos.RankingItemDTO;
import com.example.demo.services.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingItemDTO>> rankingGlobal() {
        return ResponseEntity.ok(rankingService.rankingGlobal());
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<RankingItemDTO>> rankingGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(rankingService.rankingGrupo(grupoId));
    }

    @GetMapping("/fecha/{fechaId}")
    public ResponseEntity<List<RankingFechaItemDTO>> rankingPorFecha(@PathVariable Long fechaId) {
        return ResponseEntity.ok(rankingService.rankingPorFecha(fechaId));
    }
}
