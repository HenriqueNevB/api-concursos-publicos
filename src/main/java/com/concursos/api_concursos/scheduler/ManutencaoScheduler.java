package com.concursos.api_concursos.scheduler;

import com.concursos.api_concursos.service.EditalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManutencaoScheduler {

    private final EditalService editalService;

    // Cron para rodar todos os dias à 00:01:00 da manhã
    @Scheduled(cron = "${bancas.manutencao.cron:0 1 0 * * *}")
    public void executarManutencaoDiaria() {
        try {
            editalService.encerrarInscricoesVencidas();
        } catch (Exception e) {
            log.error("[SCHEDULER MANUTENÇÃO] Erro ao rodar rotina diária: {}", e.getMessage());
        }
    }
}