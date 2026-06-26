package com.example.demo.repositories;

import com.example.demo.models.Rol;
import com.example.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByFechaEliminacionIsNullOrderByPuntosDescCantidadExactosDesc();
    List<Usuario> findByFechaEliminacionIsNullAndRolNotOrderByPuntosDescCantidadExactosDesc(Rol rol);

    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.puntos = :puntos, u.cantidadExactos = :exactos WHERE u.id = :id")
    void updatePuntosById(Long id, int puntos, int exactos);
}
