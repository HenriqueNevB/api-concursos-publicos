package com.concursos.api_concursos.controller;

import com.concursos.api_concursos.dto.BancaRequestDTO;
import com.concursos.api_concursos.dto.BancaResponseDTO;
import com.concursos.api_concursos.service.BancaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bancas", description = "Endpoints para gerenciamento das bancas organizadoras")
public class BancaController {

    private final BancaService bancaService;

    @GetMapping("/api/bancas")
    @Operation(summary = "Listar todas as bancas")
    public ResponseEntity<List<BancaResponseDTO>> listar() {
        return ResponseEntity.ok(bancaService.listarTodas());
    }

    @GetMapping("/api/bancas/{id}")
    @Operation(summary = "Buscar uma banca pelo ID")
    public ResponseEntity<BancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bancaService.buscarPorId(id));
    }

    @PostMapping("/api/admin/bancas")
    @Operation(summary = "Cadastrar uma nova banca")
    public ResponseEntity<BancaResponseDTO> criar(@RequestBody @Valid BancaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bancaService.criar(dto));
    }

    @PutMapping("/api/admin/bancas/{id}")
    @Operation(summary = "Atualizar dados de uma banca")
    public ResponseEntity<BancaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid BancaRequestDTO dto) {
        return ResponseEntity.ok(bancaService.atualizar(id, dto));
    }

    @DeleteMapping("/api/admin/bancas/{id}")
    @Operation(summary = "Excluir uma banca do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bancaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}