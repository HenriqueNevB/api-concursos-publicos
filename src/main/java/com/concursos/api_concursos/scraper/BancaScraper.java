package com.concursos.api_concursos.scraper;

import com.concursos.api_concursos.dto.EditalScrapedDataDTO;

import java.util.List;

public interface BancaScraper {
    // Executa a varredura na URL informada e retorna a lista de editais e cargos encontrados.
    List<EditalScrapedDataDTO> executarScraping(String urlAlvo) throws Exception;
}