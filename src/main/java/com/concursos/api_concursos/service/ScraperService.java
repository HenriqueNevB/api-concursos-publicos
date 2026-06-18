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

    // Executa o processo de varredura para todas as bancas cadastradas no sistema.
    public void executarMecanismoCompleto() {
        List<Banca> bancas = bancaRepository.findAll();
        log.info("Iniciando varredura automatizada. Total de bancas encontradas: {}", bancas.size());

        for (Banca banca : bancas) {
            if (banca.getScraperBean() != null && !banca.getScraperBean().isEmpty()) {
                processarBanca(banca);
            }
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
        // Verifica se o edital já existe no banco de dados pela URL
        Edital edital = editalRepository.findByUrlEdital(dto.getUrlEdital())
                .orElse(new Edital());

        edital.setBanca(banca);
        edital.setUrlEdital(dto.getUrlEdital());
        edital.setTitulo(dto.getTitulo());
        edital.setOrgao(dto.getOrgao());
        edital.setDataPublicacao(dto.getDataPublicacao());
        edital.setDataInscricaoInicio(dto.getDataInscricaoInicio());
        edital.setDataInscricaoFim(dto.getDataInscricaoFim());
        
        if (edital.getId() == null) {
            edital.setStatus(StatusEdital.INSCRICOES_ABERTAS); // Default
        }

        // Converte a lista de objetos Cargo em uma string estruturada no formato JSONB do Postgres
        String jsonCargos = objectMapper.writeValueAsString(dto.getCargos());
        edital.setJsonCargos(jsonCargos);

        editalRepository.save(edital);
    }
}