package com.example.demo.services;

import com.example.demo.models.Partido;
import com.example.demo.models.Pronostico;
import com.example.demo.models.Usuario;
import com.example.demo.models.enums.Resultado;
import com.example.demo.models.enums.ResultadoPronostico;
import com.example.demo.repositories.PronosticoRepository;
import com.example.demo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PuntuacionService {

    private final PronosticoRepository pronosticoRepository;
    private final UsuarioRepository usuarioRepository;

    public void calcularYAsignarPuntos(Partido partido) {
        List<Pronostico> pronosticos = pronosticoRepository.findByPartidoId(partido.getId());

        for (Pronostico pronostico : pronosticos) {
            ResultadoPronostico resultado = determinarResultado(pronostico, partido);
            int puntos = switch (resultado) {
                case EXACTO -> 3;
                case TENDENCIA -> 1;
                case SIN_ACIERTO -> 0;
            };

            pronostico.setResultadoPronostico(resultado);
            pronosticoRepository.save(pronostico);

            Usuario usuario = pronostico.getUsuario();
            usuario.setPuntos(usuario.getPuntos() + puntos);
            if (resultado == ResultadoPronostico.EXACTO) {
                usuario.setCantidadExactos(usuario.getCantidadExactos() + 1);
            }
            usuarioRepository.save(usuario);
        }
    }

    private ResultadoPronostico determinarResultado(Pronostico pronostico, Partido partido) {
        boolean golesLocalIguales = pronostico.getGolesLocal().equals(partido.getGolesLocal());
        boolean golesVisitanteIguales = pronostico.getGolesVisitante().equals(partido.getGolesVisitante());

        if (golesLocalIguales && golesVisitanteIguales) {
            return ResultadoPronostico.EXACTO;
        }

        Resultado tendenciaPronostico = calcularTendencia(pronostico.getGolesLocal(), pronostico.getGolesVisitante());
        if (tendenciaPronostico == partido.getResultado()) {
            return ResultadoPronostico.TENDENCIA;
        }

        return ResultadoPronostico.SIN_ACIERTO;
    }

    private Resultado calcularTendencia(int golesLocal, int golesVisitante) {
        if (golesLocal > golesVisitante) return Resultado.LOCAL;
        if (golesVisitante > golesLocal) return Resultado.VISITANTE;
        return Resultado.EMPATE;
    }
}
