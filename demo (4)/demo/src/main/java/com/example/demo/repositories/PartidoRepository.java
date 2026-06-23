package com.example.demo.repositories;

import com.example.demo.models.Partido;
import com.example.demo.models.enums.EstadoPartido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByFechaIdOrderByFechaHoraInicioAsc(Long fechaId);
    List<Partido> findByEstado(EstadoPartido estado);
    boolean existsByFechaIdAndEstadoNot(Long fechaId, EstadoPartido estado);
    boolean existsByEquipoLocalIdOrEquipoVisitanteId(Long equipoLocalId, Long equipoVisitanteId);
}
