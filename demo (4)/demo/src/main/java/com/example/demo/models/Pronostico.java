package com.example.demo.models;

import com.example.demo.models.enums.ResultadoPronostico;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "pronosticos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "partido_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pronostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer golesLocal;

    @Column(nullable = false)
    private Integer golesVisitante;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @Enumerated(EnumType.STRING)
    @Column
    private ResultadoPronostico resultadoPronostico;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}
