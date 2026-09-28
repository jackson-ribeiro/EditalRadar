package br.com.editalradar.coleta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SincronizacaoExecutor implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoExecutor.class);

    private final SincronizacaoService servico;
    private final Executor executor;
    private final ExecutorService executorProprio;
    private final AtomicBoolean emExecucao = new AtomicBoolean(false);

    @Autowired
    public SincronizacaoExecutor(SincronizacaoService servico) {
        this.servico = servico;
        this.executorProprio = Executors.newSingleThreadExecutor(tarefa -> {
            Thread thread = new Thread(tarefa, "coleta-pci");
            thread.setDaemon(true);
            return thread;
        });
        this.executor = executorProprio;
    }

    SincronizacaoExecutor(SincronizacaoService servico, Executor executor) {
        this.servico = servico;
        this.executor = executor;
        this.executorProprio = null;
    }

    public Optional<Long> disparar(OrigemColeta origem) {
        if (!emExecucao.compareAndSet(false, true)) {
            log.info("Coleta {} ignorada: já existe uma em andamento", origem);
            return Optional.empty();
        }
        try {
            Long coletaId = servico.iniciar(origem).getId();
            executor.execute(() -> {
                try {
                    servico.executar(coletaId);
                } finally {
                    emExecucao.set(false);
                }
            });
            return Optional.of(coletaId);
        } catch (RuntimeException e) {
            emExecucao.set(false);
            throw e;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciarAplicacao() {
        servico.marcarInterrompidas();
    }

    @Override
    public void destroy() {
        if (executorProprio != null) {
            executorProprio.shutdownNow();
        }
    }
}
