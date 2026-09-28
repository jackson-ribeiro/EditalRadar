package br.com.editalradar.coleta;

import br.com.editalradar.comum.Textos;
import br.com.editalradar.concurso.Concurso;
import br.com.editalradar.concurso.ConcursoRepository;
import br.com.editalradar.concurso.OrigemTi;
import br.com.editalradar.concurso.StatusConcurso;
import br.com.editalradar.config.EditalRadarProperties;
import br.com.editalradar.scraper.ConcursoListado;
import br.com.editalradar.scraper.ConcursoMcp;
import br.com.editalradar.scraper.DetalheConcurso;
import br.com.editalradar.scraper.HttpColetaException;
import br.com.editalradar.scraper.LayoutInesperadoException;
import br.com.editalradar.scraper.McpException;
import br.com.editalradar.scraper.PciConcursosScraper;
import br.com.editalradar.scraper.PrevistoListado;
import br.com.editalradar.scraper.ResultadoListagem;
import br.com.editalradar.ti.FiltroTi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SincronizacaoService {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoService.class);
    private static final double FRACAO_MINIMA_DA_LISTAGEM = 0.5;

    private final PciConcursosScraper scraper;
    private final FiltroTi filtroTi;
    private final ConcursoRepository concursos;
    private final TriagemRepository triagens;
    private final ColetaRepository coletas;
    private final EditalRadarProperties.Coleta config;
    private final Clock clock;
    private final TransactionTemplate transacao;

    public SincronizacaoService(PciConcursosScraper scraper, FiltroTi filtroTi, ConcursoRepository concursos,
                                TriagemRepository triagens, ColetaRepository coletas,
                                EditalRadarProperties propriedades, Clock clock,
                                PlatformTransactionManager gerenciadorTransacao) {
        this.scraper = scraper;
        this.filtroTi = filtroTi;
        this.concursos = concursos;
        this.triagens = triagens;
        this.coletas = coletas;
        this.config = propriedades.coleta();
        this.clock = clock;
        this.transacao = new TransactionTemplate(gerenciadorTransacao);
    }

    public Coleta iniciar(OrigemColeta origem) {
        Coleta coleta = new Coleta();
        coleta.setOrigem(origem);
        coleta.setStatus(StatusColeta.EM_ANDAMENTO);
        coleta.setIniciadaEm(Instant.now(clock));
        return coletas.save(coleta);
    }

    public void marcarInterrompidas() {
        transacao.executeWithoutResult(status -> {
            List<Coleta> abertas = coletas.findByStatus(StatusColeta.EM_ANDAMENTO);
            for (Coleta coleta : abertas) {
                coleta.setStatus(StatusColeta.FALHA);
                coleta.setFinalizadaEm(Instant.now(clock));
                coleta.setMensagemErro("Coleta interrompida: a aplicação foi encerrada antes do fim.");
            }
            coletas.saveAll(abertas);
        });
    }

    public void executar(Long coletaId) {
        RelatorioColeta relatorio = new RelatorioColeta();
        try {
            ResultadoListagem listagem = scraper.listarAbertos();
            relatorio.totalListagem = listagem.itens().size();
            verificarTamanhoDaListagem(relatorio.totalListagem);
            listagem.avisos().forEach(relatorio::avisar);

            Map<String, ConcursoMcp> sinaisMcp = buscarSinaisMcp(relatorio);
            Map<String, ConcursoListado> porUrl = listagem.itens().stream()
                    .collect(Collectors.toMap(ConcursoListado::urlOrigem, Function.identity(),
                            (primeiro, segundo) -> primeiro, LinkedHashMap::new));

            transacao.executeWithoutResult(status -> triar(porUrl, sinaisMcp, relatorio));
            baixarDetalhes(porUrl, relatorio);
            transacao.executeWithoutResult(status -> encerrarAusentes(porUrl.keySet(), relatorio));
            coletarPrevistos(relatorio);
            relatorio.pendentes = (int) triagens.countBySituacao(SituacaoTriagem.PENDENTE);

            finalizar(coletaId, StatusColeta.SUCESSO, relatorio, null);
            log.info("Coleta {} concluída: {} na listagem, {} novos, {} atualizados, {} encerrados",
                    coletaId, relatorio.totalListagem, relatorio.novos, relatorio.atualizados, relatorio.encerrados);
        } catch (RuntimeException e) {
            log.error("Coleta {} falhou", coletaId, e);
            finalizar(coletaId, StatusColeta.FALHA, relatorio, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void verificarTamanhoDaListagem(int total) {
        coletas.findFirstByStatusOrderByIniciadaEmDesc(StatusColeta.SUCESSO)
                .map(Coleta::getTotalListagem)
                .filter(anterior -> total < anterior * FRACAO_MINIMA_DA_LISTAGEM)
                .ifPresent(anterior -> {
                    throw new IllegalStateException("Listagem com " + total + " itens, menos da metade dos "
                            + anterior + " da última coleta bem-sucedida; possível página parcial, nada foi alterado.");
                });
    }

    private Map<String, ConcursoMcp> buscarSinaisMcp(RelatorioColeta relatorio) {
        Map<String, ConcursoMcp> sinais = new HashMap<>();
        if (!config.mcp().habilitado()) {
            return sinais;
        }
        for (String termo : config.mcp().termos()) {
            try {
                for (ConcursoMcp concurso : scraper.buscarNoMcp(termo)) {
                    if (filtroTi.ehTi(concurso.cargos())) {
                        sinais.putIfAbsent(concurso.link(), concurso);
                    }
                }
            } catch (HttpColetaException | McpException e) {
                relatorio.avisar("MCP indisponível (termo '" + termo + "'); consultas ao MCP suspensas nesta coleta: "
                        + e.getMessage());
                break;
            }
        }
        return sinais;
    }

    private void triar(Map<String, ConcursoListado> porUrl, Map<String, ConcursoMcp> sinaisMcp,
                       RelatorioColeta relatorio) {
        Instant agora = Instant.now(clock);
        LocalDate hoje = LocalDate.now(clock);
        Map<String, Concurso> existentes = porUrlOrigem(concursos.findByUrlOrigemIn(porUrl.keySet()));
        Set<String> triados = triagens.findByUrlOrigemIn(porUrl.keySet()).stream()
                .map(Triagem::getUrlOrigem)
                .collect(Collectors.toSet());

        List<Concurso> gravar = new ArrayList<>();
        List<Concurso> remover = new ArrayList<>();
        List<Triagem> novasTriagens = new ArrayList<>();
        for (ConcursoListado item : porUrl.values()) {
            Concurso existente = existentes.get(item.urlOrigem());
            if (existente != null) {
                if (deixouDeSerTi(existente, item)) {
                    remover.add(existente);
                    novasTriagens.add(new Triagem(item.urlOrigem(), SituacaoTriagem.DESCARTADO, agora));
                    relatorio.descartados++;
                    continue;
                }
                if (MapeamentoConcurso.aplicarListagem(existente, item, hoje)) {
                    existente.setAtualizadoEm(agora);
                    relatorio.atualizados++;
                }
                existente.setUltimaVezVistoEm(agora);
                gravar.add(existente);
                continue;
            }
            if (triados.contains(item.urlOrigem())) {
                continue;
            }
            OrigemTi origem = classificar(item, sinaisMcp);
            if (origem != null) {
                Concurso novo = MapeamentoConcurso.novo(item, origem, agora, hoje);
                if (origem == OrigemTi.MCP) {
                    novo.setCargos(String.join("\n", sinaisMcp.get(item.urlOrigem()).cargos()));
                }
                gravar.add(novo);
                relatorio.novos++;
            } else if (item.variosCargos()) {
                novasTriagens.add(new Triagem(item.urlOrigem(), SituacaoTriagem.PENDENTE, agora));
            } else {
                novasTriagens.add(new Triagem(item.urlOrigem(), SituacaoTriagem.DESCARTADO, agora));
                relatorio.descartados++;
            }
        }
        if (!remover.isEmpty()) {
            concursos.deleteAll(remover);
        }
        concursos.saveAll(gravar);
        triagens.saveAll(novasTriagens);
    }

    /**
     * Reavalia um concurso já guardado com as regras atuais do FiltroTi. Sem lista de cargos e com cargo
     * genérico ("Vários Cargos") não há evidência suficiente, então o concurso é mantido até o detalhe chegar.
     */
    private boolean deixouDeSerTi(Concurso concurso, ConcursoListado item) {
        List<String> cargos = concurso.getCargos() == null
                ? List.of()
                : Arrays.stream(concurso.getCargos().split("\n")).filter(cargo -> !cargo.isBlank()).toList();
        String cargo = item.cargo() != null ? item.cargo() : concurso.getCargo();
        boolean generico = cargo == null || Textos.normalizar(cargo).startsWith("varios cargos");
        if (cargos.isEmpty() && generico) {
            return false;
        }
        List<String> sinais = new ArrayList<>(cargos);
        sinais.add(generico ? null : cargo);
        sinais.add(item.titulo() != null ? item.titulo() : concurso.getTitulo());
        return !filtroTi.ehTi(sinais);
    }

    private OrigemTi classificar(ConcursoListado item, Map<String, ConcursoMcp> sinaisMcp) {
        if (filtroTi.ehTi(item.cargo(), item.titulo())) {
            return OrigemTi.LISTAGEM;
        }
        if (sinaisMcp.containsKey(item.urlOrigem())) {
            return OrigemTi.MCP;
        }
        return null;
    }

    private void baixarDetalhes(Map<String, ConcursoListado> porUrl, RelatorioColeta relatorio) {
        List<Concurso> semDetalhe = concursos.findByStatusAndDetalheColetadoEmIsNull(StatusConcurso.ABERTO);
        List<Triagem> pendentes = triagens.findBySituacao(SituacaoTriagem.PENDENTE).stream()
                .filter(triagem -> porUrl.containsKey(triagem.getUrlOrigem()))
                .toList();
        int restantes = config.detalhesPorExecucao();

        for (Concurso concurso : semDetalhe) {
            if (restantes-- <= 0) {
                return;
            }
            Optional<DetalheConcurso> detalhe = baixar(concurso.getUrlOrigem(), relatorio);
            if (detalhe.isEmpty()) {
                continue;
            }
            transacao.executeWithoutResult(status -> {
                Instant agora = Instant.now(clock);
                if (MapeamentoConcurso.aplicarDetalhe(concurso, detalhe.get())) {
                    concurso.setAtualizadoEm(agora);
                }
                concurso.setDetalheColetadoEm(agora);
                concursos.save(concurso);
            });
        }
        for (Triagem pendente : pendentes) {
            if (restantes-- <= 0) {
                return;
            }
            Optional<DetalheConcurso> detalhe = baixar(pendente.getUrlOrigem(), relatorio);
            if (detalhe.isEmpty()) {
                continue;
            }
            transacao.executeWithoutResult(status ->
                    resolverPendente(pendente, porUrl.get(pendente.getUrlOrigem()), detalhe.get(), relatorio));
        }
    }

    private Optional<DetalheConcurso> baixar(String url, RelatorioColeta relatorio) {
        try {
            DetalheConcurso detalhe = scraper.detalhar(url);
            relatorio.detalhesBaixados++;
            return Optional.of(detalhe);
        } catch (HttpColetaException | LayoutInesperadoException | IllegalArgumentException e) {
            relatorio.avisar("Falha no detalhe de " + url + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private void resolverPendente(Triagem pendente, ConcursoListado item, DetalheConcurso detalhe,
                                  RelatorioColeta relatorio) {
        Instant agora = Instant.now(clock);
        if (filtroTi.ehTi(detalhe.cargos())) {
            Concurso novo = MapeamentoConcurso.novo(item, OrigemTi.DETALHE, agora, LocalDate.now(clock));
            MapeamentoConcurso.aplicarDetalhe(novo, detalhe);
            novo.setDetalheColetadoEm(agora);
            concursos.save(novo);
            triagens.delete(pendente);
            relatorio.novos++;
        } else {
            pendente.setSituacao(SituacaoTriagem.DESCARTADO);
            pendente.setVerificadoEm(agora);
            triagens.save(pendente);
            relatorio.descartados++;
        }
    }

    private void encerrarAusentes(Set<String> urlsListagem, RelatorioColeta relatorio) {
        Instant agora = Instant.now(clock);
        LocalDate hoje = LocalDate.now(clock);
        List<Concurso> encerrados = new ArrayList<>();
        for (Concurso concurso : concursos.findByStatus(StatusConcurso.ABERTO)) {
            boolean ausente = !urlsListagem.contains(concurso.getUrlOrigem());
            boolean vencido = concurso.getFimInscricao() != null && concurso.getFimInscricao().isBefore(hoje);
            if (ausente || vencido) {
                concurso.setStatus(StatusConcurso.ENCERRADO);
                concurso.setAtualizadoEm(agora);
                encerrados.add(concurso);
            }
        }
        concursos.saveAll(encerrados);
        relatorio.encerrados += encerrados.size();
        triagens.removerForaDe(urlsListagem);
    }

    private void coletarPrevistos(RelatorioColeta relatorio) {
        if (!config.previstos().habilitado()) {
            return;
        }
        List<PrevistoListado> previstos;
        try {
            previstos = scraper.listarPrevistos();
        } catch (HttpColetaException | LayoutInesperadoException e) {
            relatorio.avisar("Falha ao coletar previstos: " + e.getMessage());
            return;
        }
        LocalDate limite = LocalDate.now(clock).minusDays(config.previstos().maxIdadeDias());
        Map<String, PrevistoListado> recentesTi = previstos.stream()
                .filter(previsto -> !previsto.dataPublicacao().isBefore(limite))
                .filter(previsto -> filtroTi.ehTi(previsto.titulo()))
                .collect(Collectors.toMap(PrevistoListado::url, Function.identity(),
                        (primeiro, segundo) -> primeiro, LinkedHashMap::new));

        transacao.executeWithoutResult(status -> {
            Instant agora = Instant.now(clock);
            Map<String, Concurso> existentes = recentesTi.isEmpty()
                    ? Map.of()
                    : porUrlOrigem(concursos.findByUrlOrigemIn(recentesTi.keySet()));
            List<Concurso> gravar = new ArrayList<>();
            for (PrevistoListado previsto : recentesTi.values()) {
                Concurso concurso = existentes.get(previsto.url());
                if (concurso == null) {
                    concurso = new Concurso();
                    concurso.setUrlOrigem(previsto.url());
                    concurso.setOrgao(previsto.titulo());
                    concurso.setTitulo(previsto.titulo());
                    concurso.setOrigemTi(OrigemTi.LISTAGEM);
                    concurso.setPrimeiraVezVistoEm(agora);
                    concurso.setAtualizadoEm(agora);
                    relatorio.novos++;
                }
                concurso.setStatus(StatusConcurso.PREVISTO);
                concurso.setUltimaVezVistoEm(agora);
                gravar.add(concurso);
            }
            for (Concurso concurso : concursos.findByStatus(StatusConcurso.PREVISTO)) {
                if (!recentesTi.containsKey(concurso.getUrlOrigem())) {
                    concurso.setStatus(StatusConcurso.ENCERRADO);
                    concurso.setAtualizadoEm(agora);
                    gravar.add(concurso);
                    relatorio.encerrados++;
                }
            }
            concursos.saveAll(gravar);
        });
    }

    private void finalizar(Long coletaId, StatusColeta status, RelatorioColeta relatorio, String erro) {
        transacao.executeWithoutResult(transacaoAtual -> {
            Coleta coleta = coletas.findById(coletaId)
                    .orElseThrow(() -> new IllegalStateException("Coleta " + coletaId + " não encontrada"));
            coleta.setStatus(status);
            coleta.setFinalizadaEm(Instant.now(clock));
            coleta.setTotalListagem(relatorio.totalListagem);
            coleta.setNovos(relatorio.novos);
            coleta.setAtualizados(relatorio.atualizados);
            coleta.setEncerrados(relatorio.encerrados);
            coleta.setDescartados(relatorio.descartados);
            coleta.setDetalhesBaixados(relatorio.detalhesBaixados);
            coleta.setPendentes(relatorio.pendentes);
            coleta.setAvisos(relatorio.avisosComoTexto());
            coleta.setMensagemErro(erro);
            coletas.save(coleta);
        });
    }

    private static Map<String, Concurso> porUrlOrigem(List<Concurso> lista) {
        return lista.stream().collect(Collectors.toMap(Concurso::getUrlOrigem, Function.identity(), (a, b) -> a));
    }
}
