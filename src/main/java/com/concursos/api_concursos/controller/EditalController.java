package com.concursos.api_concursos.controller;

import com.concursos.api_concursos.dto.EditalResponseDTO;
import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.service.EditalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Editais", description = "Endpoints para consulta e listagem de editais capturados")
public class EditalController {

    private final EditalService editalService;

    @GetMapping("/api/editais")
    @Operation(summary = "Listar todos os editais cadastrados")
    public ResponseEntity<List<EditalResponseDTO>> listar() {
        return ResponseEntity.ok(editalService.listarTodos());
    }

    @GetMapping("/api/editais/{id}")
    @Operation(summary = "Buscar um edital detalhado pelo ID")
    public ResponseEntity<EditalResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(editalService.buscarPorId(id));
    }

    @GetMapping("/api/editais/filtrar")
    @Operation(summary = "Filtrar editais dinamicamente por múltiplos critérios combinados")
    public ResponseEntity<List<EditalResponseDTO>> buscarComFiltros(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) StatusEdital status,
            @RequestParam(required = false) String orgao,
            @RequestParam(required = false) String banca,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInscricaoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInscricaoFim) {

        List<EditalResponseDTO> resultados = editalService.listarComFiltros(
                titulo, cargo, status, orgao, banca, dataInscricaoInicio, dataInscricaoFim
        );
        return ResponseEntity.ok(resultados);
    }
}