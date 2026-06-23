package com.example.demo.repositories;

import com.example.demo.models.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    Optional<Grupo> findByCodigoDeAcceso(String codigoDeAcceso);
    boolean existsByCodigoDeAcceso(String codigoDeAcceso);
}
