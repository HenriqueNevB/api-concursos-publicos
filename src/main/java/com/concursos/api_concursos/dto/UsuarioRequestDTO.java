package com.concursos.api_concursos.dto;

import com.concursos.api_concursos.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
    @NotBlank(message = "O nome não pode estar vazio")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String nome,

    @NotBlank(message = "O e-mail não pode estar vazio")
    @Email(message = "O formato do e-mail é inválido")
    @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres")
    String email,

    @NotBlank(message = "A senha não pode estar vazia")
    @Size(min = 6, max = 255, message = "A senha deve ter entre 6 e 255 caracteres")
    String senha,

    @NotNull(message = "A role do usuário deve ser informada")
    UserRole role
) {}