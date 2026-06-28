package com.concursos.api_concursos.service;

import com.concursos.api_concursos.dto.EditalScrapedDataDTO;
import com.concursos.api_concursos.enums.LogStatus;
import com.concursos.api_concursos.enums.StatusEdital;
import com.concursos.api_concursos.model.Banca;
import com.concursos.api_concursos.model.Edital;
import com.concursos.api_concursos.model.LogScraper;
import com.concursos.api_concursos.repository.BancaRepository;
import com.concursos.api_concursos.repository.EditalRepository;
import com.concursos.api_concursos.repository.LogScraperRepository;
import com.concursos.api_concursos.scraper.BancaScraper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperService {

    private final BancaRepository bancaRepository;
    private final EditalRepository editalRepository;
    private final LogScraperRepository logScraperRepository;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final EditalService editalService;

    // Executa o processo de varredura para todas as bancas cadastradas no sistema.
    public void executarMecanismoCompleto() {
        List<Banca> bancas = bancaRepository.findAll();
        log.info("Iniciando varredura automatizada. Total de bancas encontradas: {}", bancas.size());

        for (Banca banca : bancas) {
            if (banca.getScraperBean() != null && !banca.getScraperBean().isEmpty()) {
                processarBanca(banca);
            }
        }

        // Atualiza status de editais antigos quando a varredura acaba
        try {
            editalService.encerrarInscricoesVencidas();
        } catch (Exception e) {
            log.error("Falha ao rodar rotina de manutenção automática pós-scraping: {}", e.getMessage());
        }
    }
    
    // Executa a varredura de uma única banca (para o Admin disparar manualmente)
    @Transactional
    public void processarBanca(Banca banca) {
        LocalDateTime inicio = LocalDateTime.now();
        LogScraper logScraper = new LogScraper();
        logScraper.setBanca(banca);
        logScraper.setDataInicio(inicio);

        try {
            log.info("Processando a banca: {} utilizando o bean: {}", banca.getSigla(), banca.getScraperBean());

            // Localiza o componente correto do Jsoup
            BancaScraper scraper = (BancaScraper) applicationContext.getBean(banca.getScraperBean());
            
            // Executa a extração HTML
            List<EditalScrapedDataDTO> dadosColetados = scraper.executarScraping(banca.getSiteOficial());

            // Processa e persiste cada edital retornado
            for (EditalScrapedDataDTO dto : dadosColetados) {
                salvarOuAtualizarEdital(banca, dto);
            }

            logScraper.setStatus(LogStatus.SUCESSO);
            logScraper.setMensagem("Sucesso! Total de editais processados: " + dadosColetados.size());

        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            log.error("Erro: Bean do scraper '{}' não foi encontrado no contexto do Spring.", banca.getScraperBean());
            logScraper.setStatus(LogStatus.ERRO_LAYOUT);
            logScraper.setMensagem("Componente Scraper não implementado ou nome incorreto: " + e.getMessage());
        } catch (Exception e) {
            log.error("Falha ao executar scraping da banca {}: {}", banca.getSigla(), e.getMessage());
            logScraper.setStatus(LogStatus.FALHA_CONEXAO);
            logScraper.setMensagem("Erro durante a execução: " + e.getMessage());
        } finally {
            logScraper.setDataFim(LocalDateTime.now());
            logScraperRepository.save(logScraper);
        }
    }

    private void salvarOuAtualizarEdital(Banca banca, EditalScrapedDataDTO dto) throws Exception {
        // Busca se o edital já existe pela URL
        java.util.Optional<Edital> editalExistenteOpt = editalRepository.findByUrlEdital(dto.getUrlEdital());
        
        // Converte a lista de cargos para string JSONB
        String novoJsonCargos = objectMapper.writeValueAsString(dto.getCargos());
        
        // Determina o status do DTO com base no dia atual
        StatusEdital statusCorreto = calcularStatus(dto.getDataInscricaoFim() != null ? dto.getDataInscricaoFim().toLocalDate() : null);

        if (editalExistenteOpt.isPresent()) {
            Edital editalExistente = editalExistenteOpt.get();

            // Compara se algum campo ou o status sofreu alteração
            boolean houveMudanca = !equalsGarantido(editalExistente.getTitulo(), dto.getTitulo()) ||
                    !equalsGarantido(editalExistente.getOrgao(), dto.getOrgao()) ||
                    !equalsGarantido(editalExistente.getDataPublicacao(), dto.getDataPublicacao()) ||
                    !equalsGarantido(editalExistente.getDataInscricaoInicio(), dto.getDataInscricaoInicio()) ||
                    !equalsGarantido(editalExistente.getDataInscricaoFim(), dto.getDataInscricaoFim()) ||
                    !equalsGarantido(editalExistente.getStatus(), statusCorreto) || // Valida mudança de status
                    !editalExistente.getJsonCargos().equals(novoJsonCargos);

            // Só faz o update se houver alteração nos dados
            if (houveMudanca) {
                editalExistente.setTitulo(dto.getTitulo());
                editalExistente.setOrgao(dto.getOrgao());
                editalExistente.setDataPublicacao(dto.getDataPublicacao());
                editalExistente.setDataInscricaoInicio(dto.getDataInscricaoInicio());
                editalExistente.setDataInscricaoFim(dto.getDataInscricaoFim());
                editalExistente.setStatus(statusCorreto); // Atualiza com o status calculado
                editalExistente.setJsonCargos(novoJsonCargos);
                
                editalRepository.save(editalExistente);
                log.info("Edital atualizado devido a mudanças detectadas: {}", dto.getTitulo());
            } else {
                log.debug("Edital mantido sem alterações (pulando escrita): {}", dto.getTitulo());
            }
        } else {
            // Se não existir monta um registro novo
            Edital novoEdital = new Edital();
            novoEdital.setBanca(banca);
            novoEdital.setUrlEdital(dto.getUrlEdital());
            novoEdital.setTitulo(dto.getTitulo());
            novoEdital.setOrgao(dto.getOrgao());
            novoEdital.setDataPublicacao(dto.getDataPublicacao());
            novoEdital.setDataInscricaoInicio(dto.getDataInscricaoInicio());
            novoEdital.setDataInscricaoFim(dto.getDataInscricaoFim());
            novoEdital.setStatus(statusCorreto);
            novoEdital.setJsonCargos(novoJsonCargos);

            editalRepository.save(novoEdital);
            log.info("Novo edital cadastrado com sucesso: {}", dto.getTitulo());
        }
    }

    // Define inteligentemente se as inscrições ainda estão válidas ou já encerraram.
     
    private StatusEdital calcularStatus(LocalDate dataInscricaoFim) {
        if (dataInscricaoFim == null) {
            return StatusEdital.AGUARDANDO_CRONOGRAMA; // Caso a banca não informe a data limite imediatamente
        }
        
        // Se a data final for anterior a hoje, já insere como ENCERRADO
        if (dataInscricaoFim.isBefore(LocalDate.now())) {
            return StatusEdital.ENCERRADO;
        }
        
        return StatusEdital.INSCRICOES_ABERTAS;
    }

    // Método para evitar NullPointerException nas checagens
    private boolean equalsGarantido(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        return obj1.equals(obj2);
    }
}