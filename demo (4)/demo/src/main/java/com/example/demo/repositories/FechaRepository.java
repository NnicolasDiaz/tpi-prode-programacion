package com.example.demo.repositories;

import com.example.demo.models.Fecha;
import com.example.demo.models.enums.EstadoFecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FechaRepository extends JpaRepository<Fecha, Long> {
    List<Fecha> findByEstadoAndFechaEliminacionIsNull(EstadoFecha estado);
    boolean existsByNombre(String nombre);
    Optional<Fecha> findByNombre(String nombre);
}
