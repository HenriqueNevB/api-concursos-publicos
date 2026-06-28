package com.concursos.api_concursos.dto;

import com.concursos.api_concursos.enums.UserRole;

public record LoginResponseDTO(
    String token,
    String nome,
    String email,
    UserRole role
) {}