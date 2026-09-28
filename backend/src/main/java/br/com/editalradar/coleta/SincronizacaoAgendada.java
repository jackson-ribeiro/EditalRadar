package br.com.editalradar.coleta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "editalradar.coleta", name = "habilitada", havingValue = "true", matchIfMissing = true)
public class SincronizacaoAgendada {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoAgendada.class);

    private final SincronizacaoExecutor executor;

    public SincronizacaoAgendada(SincronizacaoExecutor executor) {
        this.executor = executor;
    }

    @Scheduled(cron = "${editalradar.coleta.cron}", zone = "${editalradar.coleta.zona}")
    public void executar() {
        executor.disparar(OrigemColeta.AGENDADA).ifPresentOrElse(
                id -> log.info("Coleta agendada {} iniciada", id),
                () -> log.warn("Coleta agendada não iniciada: já existe uma em andamento"));
    }
}
