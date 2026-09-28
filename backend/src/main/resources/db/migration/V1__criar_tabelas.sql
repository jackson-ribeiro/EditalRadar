CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE concurso (
    id                    BIGSERIAL PRIMARY KEY,
    url_origem            VARCHAR(512)  NOT NULL,
    titulo                VARCHAR(500),
    orgao                 VARCHAR(500)  NOT NULL,
    cargo                 VARCHAR(500),
    cargos                TEXT,
    uf                    VARCHAR(2),
    nacional              BOOLEAN       NOT NULL DEFAULT FALSE,
    escolaridade          VARCHAR(120),
    vagas                 INTEGER,
    cadastro_reserva      BOOLEAN       NOT NULL DEFAULT FALSE,
    salario_min           NUMERIC(12, 2),
    salario_max           NUMERIC(12, 2),
    banca                 VARCHAR(120),
    status                VARCHAR(20)   NOT NULL,
    inicio_inscricao      DATE,
    fim_inscricao         DATE,
    data_prova            DATE,
    origem_ti             VARCHAR(20)   NOT NULL,
    primeira_vez_visto_em TIMESTAMPTZ   NOT NULL,
    ultima_vez_visto_em   TIMESTAMPTZ   NOT NULL,
    atualizado_em         TIMESTAMPTZ   NOT NULL,
    detalhe_coletado_em   TIMESTAMPTZ,
    trecho_remuneracao    TEXT,
    trecho_taxa           TEXT,
    taxa_inscricao        NUMERIC(10, 2),
    CONSTRAINT uk_concurso_url_origem UNIQUE (url_origem),
    CONSTRAINT ck_concurso_status CHECK (status IN ('ABERTO', 'PREVISTO', 'ENCERRADO')),
    CONSTRAINT ck_concurso_origem_ti CHECK (origem_ti IN ('LISTAGEM', 'MCP', 'DETALHE'))
);

CREATE INDEX idx_concurso_status ON concurso (status);
CREATE INDEX idx_concurso_uf ON concurso (uf);
CREATE INDEX idx_concurso_fim_inscricao ON concurso (fim_inscricao);
CREATE INDEX idx_concurso_primeira_vez_visto_em ON concurso (primeira_vez_visto_em);
CREATE INDEX idx_concurso_banca ON concurso (banca);

CREATE TABLE triagem (
    url_origem    VARCHAR(512) PRIMARY KEY,
    situacao      VARCHAR(20)  NOT NULL,
    verificado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_triagem_situacao CHECK (situacao IN ('PENDENTE', 'DESCARTADO'))
);

CREATE INDEX idx_triagem_situacao ON triagem (situacao);

CREATE TABLE coleta (
    id                BIGSERIAL PRIMARY KEY,
    iniciada_em       TIMESTAMPTZ  NOT NULL,
    finalizada_em     TIMESTAMPTZ,
    status            VARCHAR(20)  NOT NULL,
    origem            VARCHAR(20)  NOT NULL,
    total_listagem    INTEGER      NOT NULL DEFAULT 0,
    novos             INTEGER      NOT NULL DEFAULT 0,
    atualizados       INTEGER      NOT NULL DEFAULT 0,
    encerrados        INTEGER      NOT NULL DEFAULT 0,
    descartados       INTEGER      NOT NULL DEFAULT 0,
    detalhes_baixados INTEGER      NOT NULL DEFAULT 0,
    pendentes         INTEGER      NOT NULL DEFAULT 0,
    avisos            TEXT,
    mensagem_erro     TEXT,
    CONSTRAINT ck_coleta_status CHECK (status IN ('EM_ANDAMENTO', 'SUCESSO', 'FALHA')),
    CONSTRAINT ck_coleta_origem CHECK (origem IN ('AGENDADA', 'MANUAL'))
);

CREATE INDEX idx_coleta_iniciada_em ON coleta (iniciada_em DESC);
