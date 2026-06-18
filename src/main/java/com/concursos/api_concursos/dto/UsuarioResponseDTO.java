package com.concursos.api_concursos.dto;

import com.concursos.api_concursos.enums.UserRole;
import com.concursos.api_concursos.model.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    UserRole role
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }
}