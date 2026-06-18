package com.concursos.api_concursos.dto;

import com.concursos.api_concursos.model.Banca;

public record BancaResponseDTO(
    Long id,
    String nome,
    String sigla,
    String siteOficial,
    String scraperBean
) {
    public BancaResponseDTO(Banca banca) {
        this(banca.getId(), banca.getNome(), banca.getSigla(), banca.getSiteOficial(), banca.getScraperBean());
    }
}