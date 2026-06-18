package com.concursos.api_concursos.controller;

import com.concursos.api_concursos.dto.EditalResponseDTO;
import com.concursos.api_concursos.service.EditalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/admin/editais/{id}")
    @Operation(summary = "Buscar um edital detalhado pelo ID")
    public ResponseEntity<EditalResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(editalService.buscarPorId(id));
    }
}