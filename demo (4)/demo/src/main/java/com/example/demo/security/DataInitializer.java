package com.example.demo.security;

import com.example.demo.models.*;
import com.example.demo.models.enums.EstadoPartido;
import com.example.demo.models.enums.Resultado;
import com.example.demo.models.enums.ResultadoPronostico;
import com.example.demo.repositories.*;
import com.example.demo.services.FechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final FechaRepository fechaRepository;
    private final PartidoRepository partidoRepository;
    private final PronosticoRepository pronosticoRepository;
    private final PasswordEncoder passwordEncoder;
    private final FechaService fechaService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        migrateEstadoPartidoConstraint();
        createAdminIfAbsent();
        crearDatosDemo();
    }

    private void migrateEstadoPartidoConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE partidos DROP CONSTRAINT IF EXISTS partidos_estado_check");
            jdbcTemplate.execute(
                "ALTER TABLE partidos ADD CONSTRAINT partidos_estado_check " +
                "CHECK (estado IN ('POR_JUGARSE','APLAZADO','EN_JUEGO','FINALIZADO'))"
            );
        } catch (Exception e) {
            System.out.println("⚠️  Migración constraint estado: " + e.getMessage());
        }
    }

    private void createAdminIfAbsent() {
        if (!usuarioRepository.existsByEmail("admin@prode.com")) {
            usuarioRepository.save(Usuario.builder()
                    .nombre("Admin").apellido("Sistema")
                    .email("admin@prode.com")
                    .contrasena(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMINISTRADOR)
                    .build());
            System.out.println("✅ Admin creado → email: admin@prode.com  /  contraseña: admin123");
        }
    }

    private void crearDatosDemo() {
        if (usuarioRepository.existsByEmail("jugador1@prode.com")) {
            recalcularSiPuntosEnCero();
            return;
        }

        System.out.println("🚀 Creando datos demo...");

        Equipo arg = equipoOC("Argentina",  "ARG");
        Equipo fra = equipoOC("Francia",    "FRA");
        Equipo bra = equipoOC("Brasil",     "BRA");
        Equipo esp = equipoOC("España",     "ESP");
        Equipo ale = equipoOC("Alemania",   "ALE");
        Equipo por = equipoOC("Portugal",   "POR");
        Equipo ing = equipoOC("Inglaterra", "ING");
        Equipo uru = equipoOC("Uruguay",    "URU");
        Equipo mex = equipoOC("México",     "MEX");
        Equipo pol = equipoOC("Polonia",    "POL");

        Fecha fecha1 = fechaOC("Jornada 1 - Fase de Grupos");
        Fecha fecha2 = fechaOC("Jornada 2 - Fase de Grupos");
        Fecha fecha3 = fechaOC("Octavos de Final");

        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 18, 0);
        Partido p1 = partidoFinalizado(fecha1, arg, mex, base,              2, 0, Resultado.LOCAL);
        Partido p2 = partidoFinalizado(fecha1, esp, ale, base.plusHours(3), 1, 1, Resultado.EMPATE);
        Partido p3 = partidoFinalizado(fecha1, fra, bra, base.plusHours(6), 3, 1, Resultado.LOCAL);

        LocalDateTime base2 = base.plusDays(4);
        Partido p4 = partidoFinalizado(fecha2, por, uru, base2,              2, 0, Resultado.LOCAL);
        Partido p5 = partidoFinalizado(fecha2, arg, pol, base2.plusHours(3), 1, 0, Resultado.LOCAL);
        Partido p6 = partidoFinalizado(fecha2, ing, esp, base2.plusHours(6), 2, 1, Resultado.LOCAL);

        LocalDateTime base3 = base.plusDays(14);
        partido(fecha3, arg, fra, base3);
        partido(fecha3, bra, por, base3.plusHours(4));
        partido(fecha3, ing, ale, base3.plusHours(8));

        String[][] nombres = {
            {"Santiago","García"},    {"Lucas","Rodríguez"},
            {"Valentina","López"},    {"Martín","González"},
            {"Sofía","Martínez"},     {"Tomás","Hernández"},
            {"Camila","Díaz"},        {"Nicolás","Torres"},
            {"Lucía","Ramírez"},      {"Agustín","Flores"},
            {"Isabella","Sánchez"},   {"Mateo","Ruiz"},
            {"Valeria","Romero"},     {"Sebastián","Medina"},
            {"Florencia","Castro"}
        };
        List<Usuario> jugadores = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            jugadores.add(jugador(nombres[i][0], nombres[i][1], "jugador" + (i + 1) + "@prode.com"));
        }

        // Predicciones [golesLocal, golesVisitante] para cada jugador (0-14)
        // ARG 2-0 MEX: exacto=5, tendencia=5, sin acierto=5
        int[][] predP1 = {{2,0},{2,0},{2,0},{2,0},{2,0},{1,0},{3,0},{1,0},{3,1},{2,1},{0,1},{1,2},{0,2},{1,1},{0,0}};
        // ESP 1-1 ALE: exacto=3, tendencia=7, sin acierto=5
        int[][] predP2 = {{1,1},{1,1},{1,1},{2,2},{0,0},{1,1},{2,2},{1,1},{0,0},{2,2},{2,0},{1,0},{3,0},{2,1},{3,1}};
        // FRA 3-1 BRA: exacto=5, tendencia=5, sin acierto=5
        int[][] predP3 = {{3,1},{3,1},{3,1},{3,1},{3,1},{2,0},{2,1},{1,0},{4,2},{4,1},{1,3},{0,2},{1,2},{0,1},{2,3}};
        // POR 2-0 URU: exacto=5, tendencia=5, sin acierto=5
        int[][] predP4 = {{2,0},{2,0},{2,0},{2,0},{2,0},{1,0},{3,0},{3,1},{1,0},{3,0},{0,2},{1,2},{0,1},{1,1},{0,0}};
        // ARG 1-0 POL: exacto=5, tendencia=5, sin acierto=5
        int[][] predP5 = {{1,0},{1,0},{1,0},{1,0},{1,0},{2,0},{3,0},{2,1},{1,0},{2,0},{0,1},{0,2},{1,2},{0,0},{1,1}};
        // ING 2-1 ESP: exacto=5, tendencia=5, sin acierto=5
        int[][] predP6 = {{2,1},{2,1},{2,1},{2,1},{2,1},{3,1},{3,2},{1,0},{3,0},{3,1},{0,1},{1,2},{0,2},{1,1},{0,0}};

        Partido[] finalizados = {p1, p2, p3, p4, p5, p6};
        int[][][] todasPred   = {predP1, predP2, predP3, predP4, predP5, predP6};

        // Acumular puntos en memoria, guardar un solo save por jugador al final
        int[] puntosAcum   = new int[jugadores.size()];
        int[] exactosAcum  = new int[jugadores.size()];

        for (int pi = 0; pi < finalizados.length; pi++) {
            Partido partido = finalizados[pi];
            for (int ui = 0; ui < jugadores.size(); ui++) {
                int gl = todasPred[pi][ui][0];
                int gv = todasPred[pi][ui][1];
                ResultadoPronostico res = calcularResultadoPronostico(
                        gl, gv, partido.getGolesLocal(), partido.getGolesVisitante(), partido.getResultado());
                int puntos = switch (res) { case EXACTO -> 3; case TENDENCIA -> 1; default -> 0; };

                Pronostico pron = Pronostico.builder()
                        .usuario(jugadores.get(ui))
                        .partido(partido)
                        .golesLocal(gl)
                        .golesVisitante(gv)
                        .resultadoPronostico(res)
                        .build();
                pronosticoRepository.save(pron);

                puntosAcum[ui]  += puntos;
                if (res == ResultadoPronostico.EXACTO) exactosAcum[ui]++;
            }
        }

        // Actualizar puntos directamente en BD (evita problemas de entidades detachadas)
        for (int ui = 0; ui < jugadores.size(); ui++) {
            usuarioRepository.updatePuntosById(jugadores.get(ui).getId(), puntosAcum[ui], exactosAcum[ui]);
        }

        fechaService.recalcularEstado(fecha1.getId());
        fechaService.recalcularEstado(fecha2.getId());

        System.out.println("✅ Datos demo creados: 10 equipos, 3 fechas, 9 partidos, 15 jugadores, 90 pronósticos");
    }

    // Si los jugadores demo existen pero sus datos están incompletos o con 0 pts, limpia y recrea pronósticos
    @org.springframework.transaction.annotation.Transactional
    private void recalcularSiPuntosEnCero() {
        Usuario j1 = usuarioRepository.findByEmail("jugador1@prode.com").orElse(null);
        if (j1 == null || j1.getPuntos() > 0) return;

        System.out.println("🔄 Datos demo incompletos - limpiando y recreando pronósticos...");

        List<Usuario> jugadores = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            usuarioRepository.findByEmail("jugador" + i + "@prode.com").ifPresent(jugadores::add);
        }
        List<Long> jugadorIds = jugadores.stream().map(Usuario::getId).toList();

        // Borrar pronosticos previos incompletos
        pronosticoRepository.deleteByUsuarioIdIn(jugadorIds);

        // Recuperar los 6 partidos finalizados (en orden de creación)
        List<com.example.demo.models.Partido> finalizados = partidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPartido.FINALIZADO)
                .sorted(java.util.Comparator.comparing(com.example.demo.models.Partido::getId))
                .toList();

        if (finalizados.size() < 6) {
            System.out.println("⚠️  Faltan partidos finalizados en DB, no se pueden recrear pronósticos");
            return;
        }

        int[][] predP1 = {{2,0},{2,0},{2,0},{2,0},{2,0},{1,0},{3,0},{1,0},{3,1},{2,1},{0,1},{1,2},{0,2},{1,1},{0,0}};
        int[][] predP2 = {{1,1},{1,1},{1,1},{2,2},{0,0},{1,1},{2,2},{1,1},{0,0},{2,2},{2,0},{1,0},{3,0},{2,1},{3,1}};
        int[][] predP3 = {{3,1},{3,1},{3,1},{3,1},{3,1},{2,0},{2,1},{1,0},{4,2},{4,1},{1,3},{0,2},{1,2},{0,1},{2,3}};
        int[][] predP4 = {{2,0},{2,0},{2,0},{2,0},{2,0},{1,0},{3,0},{3,1},{1,0},{3,0},{0,2},{1,2},{0,1},{1,1},{0,0}};
        int[][] predP5 = {{1,0},{1,0},{1,0},{1,0},{1,0},{2,0},{3,0},{2,1},{1,0},{2,0},{0,1},{0,2},{1,2},{0,0},{1,1}};
        int[][] predP6 = {{2,1},{2,1},{2,1},{2,1},{2,1},{3,1},{3,2},{1,0},{3,0},{3,1},{0,1},{1,2},{0,2},{1,1},{0,0}};
        int[][][] todasPred = {predP1, predP2, predP3, predP4, predP5, predP6};

        int[] puntosAcum  = new int[jugadores.size()];
        int[] exactosAcum = new int[jugadores.size()];

        for (int pi = 0; pi < 6; pi++) {
            com.example.demo.models.Partido partido = finalizados.get(pi);
            for (int ui = 0; ui < jugadores.size(); ui++) {
                int gl = todasPred[pi][ui][0];
                int gv = todasPred[pi][ui][1];
                ResultadoPronostico res = calcularResultadoPronostico(
                        gl, gv, partido.getGolesLocal(), partido.getGolesVisitante(), partido.getResultado());
                int puntos = switch (res) { case EXACTO -> 3; case TENDENCIA -> 1; default -> 0; };

                pronosticoRepository.save(Pronostico.builder()
                        .usuario(jugadores.get(ui)).partido(partido)
                        .golesLocal(gl).golesVisitante(gv).resultadoPronostico(res)
                        .build());

                puntosAcum[ui]  += puntos;
                if (res == ResultadoPronostico.EXACTO) exactosAcum[ui]++;
            }
        }

        // Actualizar puntos directamente en BD (evita problemas de entidades detachadas)
        for (int ui = 0; ui < jugadores.size(); ui++) {
            usuarioRepository.updatePuntosById(jugadores.get(ui).getId(), puntosAcum[ui], exactosAcum[ui]);
        }
        System.out.println("✅ Pronósticos recreados y puntos asignados correctamente");
    }

    private ResultadoPronostico calcularResultadoPronostico(int gl, int gv, int rl, int rv, Resultado resPart) {
        if (gl == rl && gv == rv) return ResultadoPronostico.EXACTO;
        Resultado tendencia = gl > gv ? Resultado.LOCAL : gv > gl ? Resultado.VISITANTE : Resultado.EMPATE;
        return tendencia == resPart ? ResultadoPronostico.TENDENCIA : ResultadoPronostico.SIN_ACIERTO;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Equipo equipoOC(String nombre, String abrev) {
        return equipoRepository.findByNombre(nombre).orElseGet(() ->
            equipoRepository.save(Equipo.builder().nombre(nombre).abreviatura(abrev).build())
        );
    }

    private Fecha fechaOC(String nombre) {
        return fechaRepository.findByNombre(nombre).orElseGet(() ->
            fechaRepository.save(Fecha.builder().nombre(nombre).build())
        );
    }

    private Partido partidoFinalizado(Fecha fecha, Equipo local, Equipo vis,
                                       LocalDateTime inicio, int gl, int gv, Resultado res) {
        return partidoRepository.save(Partido.builder()
                .fecha(fecha).equipoLocal(local).equipoVisitante(vis)
                .fechaHoraInicio(inicio)
                .estado(EstadoPartido.FINALIZADO)
                .golesLocal(gl).golesVisitante(gv).resultado(res)
                .build());
    }

    private void partido(Fecha fecha, Equipo local, Equipo vis, LocalDateTime inicio) {
        partidoRepository.save(Partido.builder()
                .fecha(fecha).equipoLocal(local).equipoVisitante(vis)
                .fechaHoraInicio(inicio)
                .build());
    }

    private Usuario jugador(String nombre, String apellido, String email) {
        return usuarioRepository.findByEmail(email).orElseGet(() ->
            usuarioRepository.save(Usuario.builder()
                    .nombre(nombre).apellido(apellido).email(email)
                    .contrasena(passwordEncoder.encode("jugador123"))
                    .rol(Rol.JUGADOR)
                    .build())
        );
    }
}
