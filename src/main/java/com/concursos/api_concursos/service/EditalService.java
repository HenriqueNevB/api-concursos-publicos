package com.concursos.api_concursos.service;

import com.concursos.api_concursos.dto.EditalResponseDTO;
import com.concursos.api_concursos.repository.EditalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditalService {

    private final EditalRepository editalRepository;

    @Transactional(readOnly = true)
    public List<EditalResponseDTO> listarTodos() {
        return editalRepository.findAll().stream()
                .map(EditalResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public EditalResponseDTO buscarPorId(Long id) {
        return editalRepository.findById(id)
                .map(EditalResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("Edital com ID " + id + " não encontrado."));
    }
}