package com.example.demo.models;

import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.models.enums.Resultado;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaEliminacion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado = EstadoPartido.POR_JUGARSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;

    @Column
    private Integer golesLocal;

    @Column
    private Integer golesVisitante;

    @Enumerated(EnumType.STRING)
    @Column
    private Resultado resultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fecha_id", nullable = false)
    private Fecha fecha;

    @JsonIgnore
    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL)
    private List<Pronostico> pronosticos;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
