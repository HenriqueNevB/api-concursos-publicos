package com.concursos.api_concursos.service;

import com.concursos.api_concursos.dto.EditalResponseDTO;
import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.repository.EditalRepository;
import com.concursos.api_concursos.repository.specification.EditalSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional(readOnly = true)
    public List<EditalResponseDTO> listarComFiltros(
            String titulo, String cargo, StatusEdital status, 
            String orgao, String bancaSigla, LocalDate dataInicio, LocalDate dataFim) {

        Specification<com.concursos.api_concursos.model.Edital> spec = EditalSpecification.filtrar(
                titulo, cargo, status, orgao, bancaSigla, dataInicio, dataFim
        );

        return editalRepository.findAll(spec).stream()
                .map(EditalResponseDTO::new)
                .toList();
    }

    @Transactional
    public void encerrarInscricoesVencidas() {
        log.info(">>>> [MANUTENÇÃO] Verificando datas limites de inscrições...");
        
        java.time.LocalDateTime hojeComHora = LocalDate.now().atStartOfDay();
        
        int atualizados = editalRepository.atualizarStatusEditaisVencidos(
                StatusEdital.INSCRICOES_ABERTAS, 
                StatusEdital.ENCERRADO, 
                hojeComHora
        );

        if (atualizados > 0) {
            log.info(">>>> [MANUTENÇÃO] Sucesso! {} editais obsoletos foram movidos para ENCERRADO.", atualizados);
        } else {
            log.info(">>>> [MANUTENÇÃO] Nenhum edital precisou ser modificado hoje.");
        }
    }
}