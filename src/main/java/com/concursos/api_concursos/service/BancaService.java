package com.concursos.api_concursos.service;

import com.concursos.api_concursos.dto.BancaRequestDTO;
import com.concursos.api_concursos.dto.BancaResponseDTO;
import com.concursos.api_concursos.model.Banca;
import com.concursos.api_concursos.repository.BancaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BancaService {

    private final BancaRepository bancaRepository;

    @Transactional(readOnly = true)
    public List<BancaResponseDTO> listarTodas() {
        return bancaRepository.findAll().stream()
                .map(BancaResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public BancaResponseDTO buscarPorId(Long id) {
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banca com ID " + id + " não encontrada."));
        return new BancaResponseDTO(banca);
    }

    @Transactional
    // para o ScraperService
    public Banca buscarEntidadePorId(Long id) {
        return bancaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banca com ID " + id + " não encontrada."));
    }

    @Transactional
    public BancaResponseDTO criar(BancaRequestDTO dto) {
        if (bancaRepository.existsBySigla(dto.sigla())) {
            throw new RuntimeException("Já existe uma banca cadastrada com a sigla: " + dto.sigla());
        }

        Banca banca = new Banca();
        banca.setNome(dto.nome());
        banca.setSigla(dto.sigla());
        banca.setSiteOficial(dto.siteOficial());
        banca.setScraperBean(dto.scraperBean());

        return new BancaResponseDTO(bancaRepository.save(banca));
    }

    @Transactional
    public BancaResponseDTO atualizar(Long id, BancaRequestDTO dto) {
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banca não encontrada para atualização."));

        if (!banca.getSigla().equalsIgnoreCase(dto.sigla()) && bancaRepository.existsBySigla(dto.sigla())) {
            throw new RuntimeException("A sigla '" + dto.sigla() + "' já está sendo usada por outra banca.");
        }

        banca.setNome(dto.nome());
        banca.setSigla(dto.sigla());
        banca.setSiteOficial(dto.siteOficial());
        banca.setScraperBean(dto.scraperBean());

        return new BancaResponseDTO(bancaRepository.save(banca));
    }

    @Transactional
    public void deletar(Long id) {
        if (!bancaRepository.existsById(id)) {
            throw new RuntimeException("Impossível deletar. Banca não encontrada.");
        }
        bancaRepository.deleteById(id);
    }
}