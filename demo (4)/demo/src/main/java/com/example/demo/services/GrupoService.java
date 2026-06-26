package com.example.demo.services;

import com.example.demo.dtos.GrupoRequestDTO;
import com.example.demo.dtos.GrupoResponseDTO;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.models.Grupo;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.GrupoRepository;
import com.example.demo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class GrupoService {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODIGO_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    public GrupoResponseDTO crearGrupo(GrupoRequestDTO dto, Usuario creador) {
        String codigo = generarCodigoUnico();

        Grupo grupo = Grupo.builder()
                .nombre(dto.getNombre())
                .codigoDeAcceso(codigo)
                .creador(creador)
                .integrantes(new ArrayList<>(java.util.List.of(creador)))
                .build();

        Grupo saved = grupoRepository.save(grupo);

        // Asignar como grupo personal si el usuario no tiene uno
        if (creador.getGrupoPersonal() == null) {
            creador.setGrupoPersonal(saved);
            usuarioRepository.save(creador);
        }

        return toDTO(saved);
    }

    public GrupoResponseDTO unirseAGrupo(String codigoDeAcceso, Usuario usuario) {
        Grupo grupo = grupoRepository.findByCodigoDeAcceso(codigoDeAcceso)
                .orElseThrow(() -> new ResourceNotFoundException("Código de invitación inválido"));

        boolean yaEsIntegrante = grupo.getIntegrantes().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));
        if (yaEsIntegrante) {
            throw new BusinessException("Ya sos integrante de este grupo");
        }

        grupo.getIntegrantes().add(usuario);
        return toDTO(grupoRepository.save(grupo));
    }

    public void abandonarGrupo(Long grupoId, Usuario usuario) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
        if (grupo.getFechaEliminacion() != null) {
            throw new BusinessException("El grupo ya no existe");
        }
        boolean esMiembro = grupo.getIntegrantes().stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));
        if (!esMiembro) {
            throw new BusinessException("No sos integrante de este grupo");
        }
        grupo.getIntegrantes().removeIf(u -> u.getId().equals(usuario.getId()));
        grupoRepository.save(grupo);
    }

    @Transactional(readOnly = true)
    public GrupoResponseDTO obtenerGrupo(Long id) {
        return toDTO(grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado")));
    }

    @Transactional(readOnly = true)
    public java.util.List<GrupoResponseDTO> listarTodos() {
        return grupoRepository.findAll().stream()
                .filter(g -> g.getFechaEliminacion() == null)
                .map(g -> toDTOPublico(g))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<GrupoResponseDTO> listarMisGrupos(Usuario usuario) {
        return grupoRepository.findAll().stream()
                .filter(g -> g.getFechaEliminacion() == null)
                .filter(g -> g.getIntegrantes().stream().anyMatch(u -> u.getId().equals(usuario.getId())))
                .map(this::toDTO)
                .toList();
    }

    public void eliminarGrupo(Long id) {
        Grupo grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
        if (grupo.getFechaEliminacion() != null) {
            throw new BusinessException("El grupo ya fue eliminado");
        }
        grupo.setFechaEliminacion(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        grupoRepository.save(grupo);
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODIGO_LENGTH; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            codigo = sb.toString();
        } while (grupoRepository.existsByCodigoDeAcceso(codigo));
        return codigo;
    }

    private GrupoResponseDTO toDTO(Grupo g) {
        return new GrupoResponseDTO(
                g.getId(), g.getNombre(), g.getCodigoDeAcceso(),
                g.getIntegrantes() != null ? g.getIntegrantes().size() : 0,
                g.getCreador().getNombre(), g.getCreador().getApellido()
        );
    }

    private GrupoResponseDTO toDTOPublico(Grupo g) {
        return new GrupoResponseDTO(
                g.getId(), g.getNombre(), null,
                g.getIntegrantes() != null ? g.getIntegrantes().size() : 0,
                g.getCreador().getNombre(), g.getCreador().getApellido()
        );
    }
}
