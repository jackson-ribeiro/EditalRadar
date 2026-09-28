package br.com.editalradar.coleta;

import br.com.editalradar.concurso.Concurso;
import br.com.editalradar.concurso.ConcursoRepository;
import br.com.editalradar.concurso.OrigemTi;
import br.com.editalradar.concurso.StatusConcurso;
import br.com.editalradar.config.EditalRadarProperties;
import br.com.editalradar.scraper.ConcursoListado;
import br.com.editalradar.scraper.ConcursoMcp;
import br.com.editalradar.scraper.DatasTexto.SituacaoPrazo;
import br.com.editalradar.scraper.DetalheConcurso;
import br.com.editalradar.scraper.HttpColetaException;
import br.com.editalradar.scraper.LayoutInesperadoException;
import br.com.editalradar.scraper.PciConcursosScraper;
import br.com.editalradar.scraper.PrevistoListado;
import br.com.editalradar.scraper.ResultadoListagem;
import br.com.editalradar.suporte.PropriedadesTeste;
import br.com.editalradar.suporte.TransacaoFalsa;
import br.com.editalradar.ti.FiltroTi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SincronizacaoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-23T10:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneId.of("America/Sao_Paulo"));
    private static final String BASE = "https://www.pciconcursos.com.br/noticias/";

    @Mock
    private PciConcursosScraper scraper;
    @Mock
    private ConcursoRepository concursos;
    @Mock
    private TriagemRepository triagens;
    @Mock
    private ColetaRepository coletas;

    private final List<Concurso> concursosGravados = new ArrayList<>();
    private final List<Triagem> triagensGravadas = new ArrayList<>();
    private Coleta coleta;
    private SincronizacaoService servico;

    @BeforeEach
    void preparar() {
        coleta = new Coleta();
        coleta.setStatus(StatusColeta.EM_ANDAMENTO);
        when(coletas.findById(1L)).thenReturn(Optional.of(coleta));
        when(coletas.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(concursos.save(any())).thenAnswer(invocacao -> {
            concursosGravados.add(invocacao.getArgument(0));
            return invocacao.getArgument(0);
        });
        when(concursos.saveAll(any())).thenAnswer(invocacao -> {
            Iterable<Concurso> lista = invocacao.getArgument(0);
            lista.forEach(concursosGravados::add);
            return lista;
        });
        when(triagens.save(any())).thenAnswer(invocacao -> {
            triagensGravadas.add(invocacao.getArgument(0));
            return invocacao.getArgument(0);
        });
        when(triagens.saveAll(any())).thenAnswer(invocacao -> {
            Iterable<Triagem> lista = invocacao.getArgument(0);
            lista.forEach(triagensGravadas::add);
            return lista;
        });
        configurar(PropriedadesTeste.com(600, false, false));
    }

    @Test
    void falhaNaListagemRegistraFalhaSemAlterarDados() {
        when(scraper.listarAbertos()).thenThrow(new LayoutInesperadoException("Listagem sem o elemento #concursos"));

        servico.executar(1L);

        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.FALHA);
        assertThat(coleta.getMensagemErro()).contains("#concursos");
        assertThat(coleta.getFinalizadaEm()).isEqualTo(AGORA);
        verify(concursos, never()).save(any());
        verify(concursos, never()).saveAll(any());
        verify(triagens, never()).saveAll(any());
    }

    @Test
    void listagemMuitoMenorQueAAnteriorFalhaSemAlterarDados() {
        Coleta anterior = new Coleta();
        anterior.setStatus(StatusColeta.SUCESSO);
        anterior.setTotalListagem(457);
        when(coletas.findFirstByStatusOrderByIniciadaEmDesc(StatusColeta.SUCESSO)).thenReturn(Optional.of(anterior));
        when(scraper.listarAbertos()).thenReturn(listagem(
                item("analista-ti", "Analista de TI", "Concurso"), item("medico", "Médico", "Concurso")));

        servico.executar(1L);

        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.FALHA);
        assertThat(coleta.getMensagemErro()).contains("2 itens").contains("457");
        verify(concursos, never()).saveAll(any());
        verify(triagens, never()).saveAll(any());
        verify(triagens, never()).removerForaDe(any());
    }

    @Test
    void listagemComTamanhoNormalSegue() {
        Coleta anterior = new Coleta();
        anterior.setStatus(StatusColeta.SUCESSO);
        anterior.setTotalListagem(4);
        when(coletas.findFirstByStatusOrderByIniciadaEmDesc(StatusColeta.SUCESSO)).thenReturn(Optional.of(anterior));
        when(scraper.listarAbertos()).thenReturn(listagem(
                item("analista-ti", "Analista de TI", "Concurso"), item("medico", "Médico", "Concurso")));

        servico.executar(1L);

        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.SUCESSO);
    }

    @Test
    void triagemSeparaTiPendenteEDescartado() {
        configurar(PropriedadesTeste.com(0, false, false));
        when(scraper.listarAbertos()).thenReturn(listagem(
                item("analista-ti", "Analista de TI", "Prefeitura abre concurso"),
                item("varios", "Vários Cargos", "Prefeitura X abre concurso"),
                item("medico", "Médico", "Prefeitura Y abre concurso")));

        servico.executar(1L);

        assertThat(concursosGravados).singleElement().satisfies(concurso -> {
            assertThat(concurso.getUrlOrigem()).isEqualTo(BASE + "analista-ti");
            assertThat(concurso.getOrigemTi()).isEqualTo(OrigemTi.LISTAGEM);
            assertThat(concurso.getStatus()).isEqualTo(StatusConcurso.ABERTO);
            assertThat(concurso.getPrimeiraVezVistoEm()).isEqualTo(AGORA);
            assertThat(concurso.getUltimaVezVistoEm()).isEqualTo(AGORA);
        });
        assertThat(triagensGravadas).extracting(Triagem::getUrlOrigem, Triagem::getSituacao).containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(BASE + "varios", SituacaoTriagem.PENDENTE),
                org.assertj.core.groups.Tuple.tuple(BASE + "medico", SituacaoTriagem.DESCARTADO));
        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.SUCESSO);
        assertThat(coleta.getTotalListagem()).isEqualTo(3);
        assertThat(coleta.getNovos()).isEqualTo(1);
        assertThat(coleta.getDescartados()).isEqualTo(1);
    }

    @Test
    void jaTriadoNaoEhReclassificado() {
        configurar(PropriedadesTeste.com(0, false, false));
        when(scraper.listarAbertos()).thenReturn(listagem(item("medico", "Médico", "Prefeitura abre concurso")));
        when(triagens.findByUrlOrigemIn(any())).thenReturn(
                List.of(new Triagem(BASE + "medico", SituacaoTriagem.DESCARTADO, AGORA)));

        servico.executar(1L);

        assertThat(triagensGravadas).isEmpty();
        assertThat(concursosGravados).isEmpty();
    }

    @Test
    void sinalDoMcpClassificaConcursoComoTi() {
        configurar(PropriedadesTeste.com(0, true, false));
        when(scraper.listarAbertos()).thenReturn(listagem(
                item("camara", "Analista de Atividades da Secretaria", "Câmara abre concurso")));
        when(scraper.buscarNoMcp("analista de sistemas")).thenReturn(List.of(new ConcursoMcp(BASE + "camara",
                List.of("ANALISTA DE ATIVIDADES DA SECRETARIA - ANALISTA DE SISTEMAS", "CONTADOR"))));

        servico.executar(1L);

        assertThat(concursosGravados).singleElement().satisfies(concurso -> {
            assertThat(concurso.getOrigemTi()).isEqualTo(OrigemTi.MCP);
            assertThat(concurso.getCargos()).isEqualTo("ANALISTA DE ATIVIDADES DA SECRETARIA - ANALISTA DE SISTEMAS\nCONTADOR");
        });
    }

    @Test
    void mcpForaDoArGeraAvisoEContinua() {
        configurar(PropriedadesTeste.com(0, true, false));
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso")));
        when(scraper.buscarNoMcp(any())).thenThrow(new HttpColetaException("HTTP 503 em mcp"));

        servico.executar(1L);

        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.SUCESSO);
        assertThat(coleta.getAvisos()).contains("MCP indisponível");
        assertThat(concursosGravados).hasSize(1);
        verify(scraper, times(1)).buscarNoMcp(any());
    }

    @Test
    void pendenteComCargoDeTiViraConcursoPeloDetalhe() {
        Triagem pendente = new Triagem(BASE + "varios", SituacaoTriagem.PENDENTE, AGORA);
        when(scraper.listarAbertos()).thenReturn(listagem(item("varios", "Vários Cargos", "Prefeitura abre concurso")));
        when(triagens.findByUrlOrigemIn(any())).thenReturn(List.of(pendente));
        when(triagens.findBySituacao(SituacaoTriagem.PENDENTE)).thenReturn(List.of(pendente));
        when(scraper.detalhar(BASE + "varios")).thenReturn(new DetalheConcurso(
                List.of("Analista de TI (1 vaga)", "Médico (2 vagas)"),
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 6), "Quadrix", null, null, null, null, null));

        servico.executar(1L);

        assertThat(concursosGravados).singleElement().satisfies(concurso -> {
            assertThat(concurso.getOrigemTi()).isEqualTo(OrigemTi.DETALHE);
            assertThat(concurso.getBanca()).isEqualTo("Quadrix");
            assertThat(concurso.getDataProva()).isEqualTo(LocalDate.of(2026, 12, 6));
            assertThat(concurso.getCargos()).isEqualTo("Analista de TI (1 vaga)\nMédico (2 vagas)");
            assertThat(concurso.getDetalheColetadoEm()).isEqualTo(AGORA);
        });
        verify(triagens).delete(pendente);
        assertThat(coleta.getNovos()).isEqualTo(1);
        assertThat(coleta.getDetalhesBaixados()).isEqualTo(1);
    }

    @Test
    void pendenteSemTiViraDescartado() {
        Triagem pendente = new Triagem(BASE + "varios", SituacaoTriagem.PENDENTE, AGORA);
        when(scraper.listarAbertos()).thenReturn(listagem(item("varios", "Vários Cargos", "Prefeitura abre concurso")));
        when(triagens.findByUrlOrigemIn(any())).thenReturn(List.of(pendente));
        when(triagens.findBySituacao(SituacaoTriagem.PENDENTE)).thenReturn(List.of(pendente));
        when(scraper.detalhar(BASE + "varios")).thenReturn(
                new DetalheConcurso(List.of("Médico (2 vagas)"), null, null, null, null, null, null, null, null));

        servico.executar(1L);

        assertThat(concursosGravados).isEmpty();
        assertThat(pendente.getSituacao()).isEqualTo(SituacaoTriagem.DESCARTADO);
        assertThat(triagensGravadas).contains(pendente);
        assertThat(coleta.getDescartados()).isEqualTo(1);
    }

    @Test
    void concursoExistenteAtualizaListagemSemApagarDetalhe() {
        Concurso existente = concurso("analista-ti", StatusConcurso.ABERTO);
        existente.setBanca("Quadrix");
        existente.setSalarioMin(new BigDecimal("3000.00"));
        existente.setInicioInscricao(LocalDate.of(2026, 10, 1));
        existente.setDetalheColetadoEm(Instant.parse("2026-09-01T10:00:00Z"));
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(existente));
        ConcursoListado atualizado = new ConcursoListado(BASE + "analista-ti", "Órgão analista-ti", "Concurso",
                "SP", false, "Analista de TI", "Superior", 3, false, null, new BigDecimal("5000.00"),
                null, LocalDate.of(2026, 10, 30), SituacaoPrazo.NORMAL);
        when(scraper.listarAbertos()).thenReturn(listagem(atualizado));

        servico.executar(1L);

        assertThat(existente.getVagas()).isEqualTo(3);
        assertThat(existente.getBanca()).isEqualTo("Quadrix");
        assertThat(existente.getSalarioMin()).isEqualByComparingTo("3000.00");
        assertThat(existente.getInicioInscricao()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(existente.getPrimeiraVezVistoEm()).isEqualTo(Instant.parse("2026-09-01T10:00:00Z"));
        assertThat(existente.getUltimaVezVistoEm()).isEqualTo(AGORA);
        assertThat(existente.getAtualizadoEm()).isEqualTo(AGORA);
        assertThat(coleta.getAtualizados()).isEqualTo(1);
        assertThat(coleta.getNovos()).isZero();
    }

    @Test
    void concursoAusenteDaListagemEhEncerrado() {
        Concurso sumiu = concurso("sumiu", StatusConcurso.ABERTO);
        when(scraper.listarAbertos()).thenReturn(listagem(item("medico", "Médico", "Concurso")));
        when(concursos.findByStatus(StatusConcurso.ABERTO)).thenReturn(List.of(sumiu));

        servico.executar(1L);

        assertThat(sumiu.getStatus()).isEqualTo(StatusConcurso.ENCERRADO);
        assertThat(sumiu.getAtualizadoEm()).isEqualTo(AGORA);
        assertThat(coleta.getEncerrados()).isEqualTo(1);
        verify(triagens).removerForaDe(Set.of(BASE + "medico"));
    }

    @Test
    void reaberturaVoltaParaAberto() {
        Concurso encerrado = concurso("analista-ti", StatusConcurso.ENCERRADO);
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(encerrado));
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso reaberto")));

        servico.executar(1L);

        assertThat(encerrado.getStatus()).isEqualTo(StatusConcurso.ABERTO);
    }

    @Test
    void concursoCanceladoEntraEncerrado() {
        configurar(PropriedadesTeste.com(0, false, false));
        ConcursoListado cancelado = new ConcursoListado(BASE + "cancelado", "Órgão", "Concurso cancelado",
                "MT", false, "Analista de TI", "Superior", 1, false, null, null, null, null, SituacaoPrazo.CANCELADO);
        when(scraper.listarAbertos()).thenReturn(listagem(cancelado));

        servico.executar(1L);

        assertThat(concursosGravados).singleElement()
                .extracting(Concurso::getStatus).isEqualTo(StatusConcurso.ENCERRADO);
    }

    @Test
    void falhaEmUmDetalheMantemPendenteEAvisa() {
        Concurso semDetalhe = concurso("analista-ti", StatusConcurso.ABERTO);
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso")));
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(semDetalhe));
        when(concursos.findByStatusAndDetalheColetadoEmIsNull(StatusConcurso.ABERTO)).thenReturn(List.of(semDetalhe));
        when(scraper.detalhar(BASE + "analista-ti")).thenThrow(new HttpColetaException("HTTP 500 em detalhe"));

        servico.executar(1L);

        assertThat(semDetalhe.getDetalheColetadoEm()).isNull();
        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.SUCESSO);
        assertThat(coleta.getAvisos()).contains("Falha no detalhe de " + BASE + "analista-ti");
    }

    @Test
    void urlDeDetalheRejeitadaViraAvisoSemDerrubarColeta() {
        Concurso semDetalhe = concurso("analista-ti", StatusConcurso.ABERTO);
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso")));
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(semDetalhe));
        when(concursos.findByStatusAndDetalheColetadoEmIsNull(StatusConcurso.ABERTO)).thenReturn(List.of(semDetalhe));
        when(scraper.detalhar(BASE + "analista-ti"))
                .thenThrow(new IllegalArgumentException("URL fora do PCI Concursos: x"));

        servico.executar(1L);

        assertThat(coleta.getStatus()).isEqualTo(StatusColeta.SUCESSO);
        assertThat(coleta.getAvisos()).contains("Falha no detalhe de " + BASE + "analista-ti");
    }

    @Test
    void detalheAplicaTrechosSemApagarOQueJaExiste() {
        Concurso semDetalhe = concurso("analista-ti", StatusConcurso.ABERTO);
        semDetalhe.setTrechoTaxa("A taxa de inscrição é de R$ 90,00.");
        semDetalhe.setTaxaInscricao(new BigDecimal("90.00"));
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso")));
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(semDetalhe));
        when(concursos.findByStatusAndDetalheColetadoEmIsNull(StatusConcurso.ABERTO)).thenReturn(List.of(semDetalhe));
        when(scraper.detalhar(BASE + "analista-ti")).thenReturn(new DetalheConcurso(
                List.of("Analista de TI (1 vaga)"), null, null, null, null, null,
                "O salário é de R$ 5.100,00 por mês.", null, null));

        servico.executar(1L);

        assertThat(semDetalhe.getTrechoRemuneracao()).isEqualTo("O salário é de R$ 5.100,00 por mês.");
        assertThat(semDetalhe.getTrechoTaxa()).isEqualTo("A taxa de inscrição é de R$ 90,00.");
        assertThat(semDetalhe.getTaxaInscricao()).isEqualByComparingTo("90.00");
        assertThat(semDetalhe.getDetalheColetadoEm()).isEqualTo(AGORA);
    }

    @Test
    void novoTrechoDeTaxaSubstituiATaxaEmValorAnterior() {
        Concurso concurso = concurso("analista-ti", StatusConcurso.ABERTO);
        concurso.setTrechoTaxa("A taxa de inscrição é de R$ 90,00.");
        concurso.setTaxaInscricao(new BigDecimal("90.00"));
        when(scraper.listarAbertos()).thenReturn(listagem(item("analista-ti", "Analista de TI", "Concurso")));
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(concurso));
        when(concursos.findByStatusAndDetalheColetadoEmIsNull(StatusConcurso.ABERTO)).thenReturn(List.of(concurso));
        when(scraper.detalhar(BASE + "analista-ti")).thenReturn(new DetalheConcurso(
                List.of("Analista de TI (1 vaga)"), null, null, null, null, null, null,
                "A taxa é de R$ 100,00 para nível médio e R$ 120,00 para superior.", null));

        servico.executar(1L);

        assertThat(concurso.getTrechoTaxa()).isEqualTo("A taxa é de R$ 100,00 para nível médio e R$ 120,00 para superior.");
        assertThat(concurso.getTaxaInscricao()).isNull();
    }

    @Test
    void existenteQueDeixouDeSerTiEhRemovidoEDescartado() {
        Concurso professor = concurso("professor", StatusConcurso.ABERTO);
        professor.setCargo("Vários Cargos");
        professor.setTitulo("Prefeitura abre concurso para diversas áreas");
        professor.setCargos("Professor - Informática\nMédico (2 vagas)");
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(professor));
        when(scraper.listarAbertos()).thenReturn(listagem(item("professor", "Vários Cargos", "Prefeitura abre concurso para diversas áreas")));

        servico.executar(1L);

        verify(concursos).deleteAll(List.of(professor));
        assertThat(concursosGravados).doesNotContain(professor);
        assertThat(triagensGravadas).extracting(Triagem::getUrlOrigem, Triagem::getSituacao)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(BASE + "professor", SituacaoTriagem.DESCARTADO));
        assertThat(coleta.getDescartados()).isEqualTo(1);
    }

    @Test
    void variosCargosSemDetalheNaoEhReavaliado() {
        Concurso semDetalhe = concurso("varios", StatusConcurso.ABERTO);
        semDetalhe.setCargo("Vários Cargos");
        when(concursos.findByUrlOrigemIn(any())).thenReturn(List.of(semDetalhe));
        when(scraper.listarAbertos()).thenReturn(listagem(item("varios", "Vários Cargos", "Prefeitura abre concurso")));

        servico.executar(1L);

        verify(concursos, never()).deleteAll(any());
        assertThat(concursosGravados).contains(semDetalhe);
    }

    @Test
    void respeitaLimiteDeDetalhesPorExecucao() {
        configurar(PropriedadesTeste.com(1, false, false));
        Triagem primeira = new Triagem(BASE + "varios-1", SituacaoTriagem.PENDENTE, AGORA);
        Triagem segunda = new Triagem(BASE + "varios-2", SituacaoTriagem.PENDENTE, AGORA);
        when(scraper.listarAbertos()).thenReturn(listagem(
                item("varios-1", "Vários Cargos", "Concurso 1"), item("varios-2", "Vários Cargos", "Concurso 2")));
        when(triagens.findByUrlOrigemIn(any())).thenReturn(List.of(primeira, segunda));
        when(triagens.findBySituacao(SituacaoTriagem.PENDENTE)).thenReturn(List.of(primeira, segunda));
        when(scraper.detalhar(any())).thenReturn(new DetalheConcurso(List.of("Médico"), null, null, null, null, null, null, null, null));

        servico.executar(1L);

        verify(scraper, times(1)).detalhar(any());
    }

    @Test
    void previstosDeTiRecentesSaoGravadosEOsQueSairamSaoEncerrados() {
        configurar(PropriedadesTeste.com(0, false, true));
        when(scraper.listarAbertos()).thenReturn(listagem(item("medico", "Médico", "Concurso")));
        String urlPrevistoTi = "https://www.pciconcursos.com.br/previstos/tce-analista-ti";
        when(scraper.listarPrevistos()).thenReturn(List.of(
                new PrevistoListado(urlPrevistoTi, "TCE prevê concurso para analista de TI", LocalDate.of(2026, 9, 1)),
                new PrevistoListado("https://www.pciconcursos.com.br/previstos/antigo-ti",
                        "Concurso antigo para analista de TI", LocalDate.of(2026, 1, 10)),
                new PrevistoListado("https://www.pciconcursos.com.br/previstos/pm",
                        "PM prevê concurso para soldado", LocalDate.of(2026, 9, 10))));
        Concurso previstoVelho = concurso("velho", StatusConcurso.PREVISTO);
        when(concursos.findByStatus(StatusConcurso.PREVISTO)).thenReturn(List.of(previstoVelho));

        servico.executar(1L);

        assertThat(concursosGravados).anySatisfy(concurso -> {
            assertThat(concurso.getUrlOrigem()).isEqualTo(urlPrevistoTi);
            assertThat(concurso.getStatus()).isEqualTo(StatusConcurso.PREVISTO);
            assertThat(concurso.getOrgao()).isEqualTo("TCE prevê concurso para analista de TI");
        });
        assertThat(concursosGravados).noneSatisfy(concurso ->
                assertThat(concurso.getUrlOrigem()).endsWith("antigo-ti"));
        assertThat(previstoVelho.getStatus()).isEqualTo(StatusConcurso.ENCERRADO);
    }

    @Test
    void iniciarRegistraColetaEmAndamento() {
        Coleta iniciada = servico.iniciar(OrigemColeta.MANUAL);

        assertThat(iniciada.getStatus()).isEqualTo(StatusColeta.EM_ANDAMENTO);
        assertThat(iniciada.getOrigem()).isEqualTo(OrigemColeta.MANUAL);
        assertThat(iniciada.getIniciadaEm()).isEqualTo(AGORA);
    }

    @Test
    void marcarInterrompidasFechaColetasEmAndamento() {
        Coleta travada = new Coleta();
        travada.setStatus(StatusColeta.EM_ANDAMENTO);
        when(coletas.findByStatus(StatusColeta.EM_ANDAMENTO)).thenReturn(List.of(travada));

        servico.marcarInterrompidas();

        assertThat(travada.getStatus()).isEqualTo(StatusColeta.FALHA);
        assertThat(travada.getMensagemErro()).contains("interrompida");
        assertThat(travada.getFinalizadaEm()).isEqualTo(AGORA);
    }

    private void configurar(EditalRadarProperties propriedades) {
        servico = new SincronizacaoService(scraper,
                new FiltroTi(PropriedadesTeste.PALAVRAS, PropriedadesTeste.EXCLUSOES, PropriedadesTeste.ENSINO),
                concursos, triagens, coletas, propriedades, RELOGIO, new TransacaoFalsa());
    }

    private static ConcursoListado item(String slug, String cargo, String titulo) {
        return new ConcursoListado(BASE + slug, "Órgão " + slug, titulo, "SP", false, cargo, "Superior", 1, false,
                null, new BigDecimal("5000.00"), null, LocalDate.of(2026, 10, 30), SituacaoPrazo.NORMAL);
    }

    private static ResultadoListagem listagem(ConcursoListado... itens) {
        return new ResultadoListagem(List.of(itens), List.of());
    }

    private static Concurso concurso(String slug, StatusConcurso status) {
        Concurso concurso = new Concurso();
        concurso.setUrlOrigem(slug.startsWith("velho") ? "https://www.pciconcursos.com.br/previstos/" + slug : BASE + slug);
        concurso.setOrgao("Órgão " + slug);
        concurso.setStatus(status);
        concurso.setOrigemTi(OrigemTi.LISTAGEM);
        concurso.setVagas(1);
        concurso.setPrimeiraVezVistoEm(Instant.parse("2026-09-01T10:00:00Z"));
        concurso.setUltimaVezVistoEm(Instant.parse("2026-09-01T10:00:00Z"));
        concurso.setAtualizadoEm(Instant.parse("2026-09-01T10:00:00Z"));
        return concurso;
    }
}
