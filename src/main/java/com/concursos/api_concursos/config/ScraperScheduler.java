package com.concursos.api_concursos.config;

import com.concursos.api_concursos.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScraperScheduler {

    private final ScraperService scraperService;

    /**
     * Dispara automaticamente a rotina do robô.
     * Expressão cron configurada para: "A cada hora, no minuto 0".
     * Pode ser customizada no application.properties adicionando a chave 'bancas.scraper.cron'
     */
    @Scheduled(cron = "${bancas.scraper.cron:0 0 * * * *}")
    public void executarRotinaAgendada() {
        log.info(">>>> [SCHEDULER] Despertador acionado! Iniciando processamento automático...");
        
        try {
            scraperService.executarMecanismoCompleto();
            log.info(">>>> [SCHEDULER] Varredura automatizada finalizada com sucesso.");
        } catch (Exception e) {
            log.error(">>>> [SCHEDULER] Falha crítica na execução do agendador: {}", e.getMessage());
        }
    }
}