package com.concursos.api_concursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BancaRequestDTO(
    @NotBlank(message = "O nome não pode estar em branco")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String nome,

    @NotBlank(message = "A sigla não pode estar em branco")
    @Size(max = 20, message = "A sigla deve ter no máximo 20 caracteres")
    String sigla,

    @Size(max = 255, message = "O site oficial deve ter no máximo 255 caracteres")
    String siteOficial,

    @Size(max = 100, message = "O nome do bean do scraper deve ter no máximo 100 caracteres")
    String scraperBean
) {}