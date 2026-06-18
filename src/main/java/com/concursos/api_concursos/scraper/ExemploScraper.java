package com.concursos.api_concursos.scraper;

import com.concursos.api_concursos.dto.CargoDTO; 
import com.concursos.api_concursos.dto.EditalScrapedDataDTO;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("exemploScraper")
public class ExemploScraper implements BancaScraper {

    @Override
    public List<EditalScrapedDataDTO> executarScraping(String urlAlvo) throws Exception {
        List<EditalScrapedDataDTO> editaisColetados = new ArrayList<>();

        // Edital Simulado 1
        EditalScrapedDataDTO edital1 = new EditalScrapedDataDTO();
        edital1.setTitulo("Edital de Concurso Público nº 01/2026");
        edital1.setOrgao("Tribunal Regional Eleitoral (TRE)");
        edital1.setUrlEdital("https://site-da-banca.com.br/concursos/tre-2026");
        edital1.setDataPublicacao(LocalDate.of(2026, 1, 15));
        edital1.setDataInscricaoInicio(LocalDateTime.of(2026, 1, 20, 10, 0));
        edital1.setDataInscricaoFim(LocalDateTime.of(2026, 2, 20, 18, 0));
        
        List<CargoDTO> cargosEdital1 = new ArrayList<>();
        cargosEdital1.add(new CargoDTO("Técnico Judiciário", 15, "R$ 8.529,65", "Médio"));
        cargosEdital1.add(new CargoDTO("Analista Judiciário - TI", 3, "R$ 13.994,78", "Superior"));
        edital1.setCargos(cargosEdital1);

        // Edital Simulado 2
        EditalScrapedDataDTO edital2 = new EditalScrapedDataDTO();
        edital2.setTitulo("Processo Seletivo Simplificado 02/2026");
        edital2.setOrgao("Secretaria de Saúde do Estado");
        edital2.setUrlEdital("https://site-da-banca.com.br/concursos/saude-2026");
        edital2.setDataPublicacao(LocalDate.of(2026, 2, 1));
        edital2.setDataInscricaoInicio(LocalDateTime.of(2026, 2, 5, 8, 0));
        edital2.setDataInscricaoFim(LocalDateTime.of(2026, 2, 15, 23, 59));
        
        List<CargoDTO> cargosEdital2 = new ArrayList<>();
        cargosEdital2.add(new CargoDTO("Médico Plantonista", 10, "R$ 12.000,00", "Superior"));
        edital2.setCargos(cargosEdital2);

        editaisColetados.add(edital1);
        editaisColetados.add(edital2);

        return editaisColetados;
    }
}