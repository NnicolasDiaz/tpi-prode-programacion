package com.example.demo.repositories;

import com.example.demo.models.Pronostico;
import com.example.demo.models.enums.ResultadoPronostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PronosticoRepository extends JpaRepository<Pronostico, Long> {
    Optional<Pronostico> findByUsuarioIdAndPartidoId(Long usuarioId, Long partidoId);
    List<Pronostico> findByUsuarioId(Long usuarioId);
    List<Pronostico> findByPartidoId(Long partidoId);
    boolean existsByUsuarioIdAndPartidoId(Long usuarioId, Long partidoId);
    boolean existsByPartidoId(Long partidoId);
    long countByUsuarioIdAndResultadoPronostico(Long usuarioId, ResultadoPronostico resultado);

    List<Pronostico> findByPartidoIdIn(List<Long> ids);

    void deleteByUsuarioIdIn(List<Long> usuarioIds);

    @Query("SELECT p.usuario.id, MIN(p.fechaCreacion) FROM Pronostico p GROUP BY p.usuario.id")
    List<Object[]> findOldestPronosticoDates();
}
