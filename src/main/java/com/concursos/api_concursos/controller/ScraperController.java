package com.concursos.api_concursos.controller;

import com.concursos.api_concursos.model.Banca;
import com.concursos.api_concursos.service.BancaService;
import com.concursos.api_concursos.service.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/scrapers")
@RequiredArgsConstructor
@Tag(name = "Mecanismo Scraper", description = "Endpoints de execução manual e monitoramento do scraper")
public class ScraperController {

    private final ScraperService scraperService;
    private final BancaService bancaService;

    @PostMapping("/disparar-todos")
    @Operation(summary = "Forçar a execução do scraper para todas as bancas cadastradas")
    public ResponseEntity<String> dispararTodos() {
        scraperService.executarMecanismoCompleto();
        return ResponseEntity.ok("Varredura manual de todas as bancas concluída. Verifique os logs de histórico.");
    }

    @PostMapping("/disparar-banca/{id}")
    @Operation(summary = "Forçar a execução do scraper para uma banca específica por ID")
    public ResponseEntity<String> dispararPorBanca(@PathVariable Long id) {
        Banca banca = bancaService.buscarEntidadePorId(id);
        
        if (banca.getScraperBean() == null || banca.getScraperBean().isEmpty()) {
            return ResponseEntity.badRequest().body("A banca '" + banca.getSigla() + "' não possui um componente Scraper associado.");
        }

        scraperService.processarBanca(banca);
        return ResponseEntity.ok("Varredura manual da banca " + banca.getSigla() + " concluída.");
    }
}