package com.example.demo.models;

import com.example.demo.models.enums.EstadoFecha;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fechas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFecha estado = EstadoFecha.PROGRAMADA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaEliminacion;

    @JsonIgnore
    @OneToMany(mappedBy = "fecha", cascade = CascadeType.ALL)
    private List<Partido> partidos;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
