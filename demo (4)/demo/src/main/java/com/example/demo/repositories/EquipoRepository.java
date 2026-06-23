package com.example.demo.repositories;

import com.example.demo.models.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByFechaEliminacionIsNull();
    boolean existsByNombre(String nombre);
}
