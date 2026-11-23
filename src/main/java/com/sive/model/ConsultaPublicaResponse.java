package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resposta da consulta pública de posição na fila para o responsável autenticado.
 */
public class ConsultaPublicaResponse {

    private ResponsavelPublico responsavel;
    private String protocoloConsultado;
    private LocalDateTime atualizadoEm;
    private List<SolicitacaoPublica> solicitacoes;

    public ConsultaPublicaResponse() {
        this.solicitacoes = new ArrayList<>();
    }

    public ConsultaPublicaResponse(
            ResponsavelPublico responsavel,
            String protocoloConsultado,
            LocalDateTime atualizadoEm,
            List<SolicitacaoPublica> solicitacoes) {
        this.responsavel = responsavel;
        this.protocoloConsultado = protocoloConsultado;
        this.atualizadoEm = atualizadoEm;
        this.solicitacoes = solicitacoes != null ? solicitacoes : new ArrayList<>();
    }

    public ResponsavelPublico getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(ResponsavelPublico responsavel) {
        this.responsavel = responsavel;
    }

    public String getProtocoloConsultado() {
        return protocoloConsultado;
    }

    public void setProtocoloConsultado(String protocoloConsultado) {
        this.protocoloConsultado = protocoloConsultado;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public List<SolicitacaoPublica> getSolicitacoes() {
        return solicitacoes;
    }

    public void setSolicitacoes(List<SolicitacaoPublica> solicitacoes) {
        this.solicitacoes = solicitacoes != null ? solicitacoes : new ArrayList<>();
    }

    public static class ResponsavelPublico {
        private UUID id;
        private String nome;
        private String cpf;

        public ResponsavelPublico() {
        }

        public ResponsavelPublico(UUID id, String nome, String cpf) {
            this.id = id;
            this.nome = nome;
            this.cpf = cpf;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }
    }

    public static class SolicitacaoPublica {
        private UUID inscricaoId;
        private String protocolo;
        private String alunoNome;
        private Integer idadeAnos;
        private String escolaNome;
        private String tipoVaga;
        private String serieAno;
        private String turno;
        private StatusInscricao status;
        private BigDecimal pontuacaoTotal;
        private Integer posicaoFila;
        private Integer totalNaFila;
        private Integer percentualAtendidas;
        private LocalDateTime dataInscricao;
        private LocalDateTime dataChamada;
        private List<CriterioPontuacaoPublico> criterios;
        private List<VizinhoFilaPublico> vizinhos;
        private List<MovimentacaoPublica> historico;

        public SolicitacaoPublica() {
            this.criterios = new ArrayList<>();
            this.vizinhos = new ArrayList<>();
            this.historico = new ArrayList<>();
        }

        public UUID getInscricaoId() {
            return inscricaoId;
        }

        public void setInscricaoId(UUID inscricaoId) {
            this.inscricaoId = inscricaoId;
        }

        public String getProtocolo() {
            return protocolo;
        }

        public void setProtocolo(String protocolo) {
            this.protocolo = protocolo;
        }

        public String getAlunoNome() {
            return alunoNome;
        }

        public void setAlunoNome(String alunoNome) {
            this.alunoNome = alunoNome;
        }

        public Integer getIdadeAnos() {
            return idadeAnos;
        }

        public void setIdadeAnos(Integer idadeAnos) {
            this.idadeAnos = idadeAnos;
        }

        public String getEscolaNome() {
            return escolaNome;
        }

        public void setEscolaNome(String escolaNome) {
            this.escolaNome = escolaNome;
        }

        public String getTipoVaga() {
            return tipoVaga;
        }

        public void setTipoVaga(String tipoVaga) {
            this.tipoVaga = tipoVaga;
        }

        public String getSerieAno() {
            return serieAno;
        }

        public void setSerieAno(String serieAno) {
            this.serieAno = serieAno;
        }

        public String getTurno() {
            return turno;
        }

        public void setTurno(String turno) {
            this.turno = turno;
        }

        public StatusInscricao getStatus() {
            return status;
        }

        public void setStatus(StatusInscricao status) {
            this.status = status;
        }

        public BigDecimal getPontuacaoTotal() {
            return pontuacaoTotal;
        }

        public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
            this.pontuacaoTotal = pontuacaoTotal;
        }

        public Integer getPosicaoFila() {
            return posicaoFila;
        }

        public void setPosicaoFila(Integer posicaoFila) {
            this.posicaoFila = posicaoFila;
        }

        public Integer getTotalNaFila() {
            return totalNaFila;
        }

        public void setTotalNaFila(Integer totalNaFila) {
            this.totalNaFila = totalNaFila;
        }

        public Integer getPercentualAtendidas() {
            return percentualAtendidas;
        }

        public void setPercentualAtendidas(Integer percentualAtendidas) {
            this.percentualAtendidas = percentualAtendidas;
        }

        public LocalDateTime getDataInscricao() {
            return dataInscricao;
        }

        public void setDataInscricao(LocalDateTime dataInscricao) {
            this.dataInscricao = dataInscricao;
        }

        public LocalDateTime getDataChamada() {
            return dataChamada;
        }

        public void setDataChamada(LocalDateTime dataChamada) {
            this.dataChamada = dataChamada;
        }

        public List<CriterioPontuacaoPublico> getCriterios() {
            return criterios;
        }

        public void setCriterios(List<CriterioPontuacaoPublico> criterios) {
            this.criterios = criterios != null ? criterios : new ArrayList<>();
        }

        public List<VizinhoFilaPublico> getVizinhos() {
            return vizinhos;
        }

        public void setVizinhos(List<VizinhoFilaPublico> vizinhos) {
            this.vizinhos = vizinhos != null ? vizinhos : new ArrayList<>();
        }

        public List<MovimentacaoPublica> getHistorico() {
            return historico;
        }

        public void setHistorico(List<MovimentacaoPublica> historico) {
            this.historico = historico != null ? historico : new ArrayList<>();
        }
    }

    public static class CriterioPontuacaoPublico {
        private String nome;
        private BigDecimal pesoPercent;
        private BigDecimal pontosObtidos;
        private BigDecimal pontosBase;
        private String detalhe;

        public CriterioPontuacaoPublico() {
        }

        public CriterioPontuacaoPublico(
                String nome,
                BigDecimal pesoPercent,
                BigDecimal pontosObtidos,
                BigDecimal pontosBase,
                String detalhe) {
            this.nome = nome;
            this.pesoPercent = pesoPercent;
            this.pontosObtidos = pontosObtidos;
            this.pontosBase = pontosBase;
            this.detalhe = detalhe;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public BigDecimal getPesoPercent() {
            return pesoPercent;
        }

        public void setPesoPercent(BigDecimal pesoPercent) {
            this.pesoPercent = pesoPercent;
        }

        public BigDecimal getPontosObtidos() {
            return pontosObtidos;
        }

        public void setPontosObtidos(BigDecimal pontosObtidos) {
            this.pontosObtidos = pontosObtidos;
        }

        public BigDecimal getPontosBase() {
            return pontosBase;
        }

        public void setPontosBase(BigDecimal pontosBase) {
            this.pontosBase = pontosBase;
        }

        public String getDetalhe() {
            return detalhe;
        }

        public void setDetalhe(String detalhe) {
            this.detalhe = detalhe;
        }
    }

    public static class VizinhoFilaPublico {
        private int posicao;
        private String rotulo;
        private String descricao;
        private boolean atual;

        public VizinhoFilaPublico() {
        }

        public VizinhoFilaPublico(int posicao, String rotulo, String descricao, boolean atual) {
            this.posicao = posicao;
            this.rotulo = rotulo;
            this.descricao = descricao;
            this.atual = atual;
        }

        public int getPosicao() {
            return posicao;
        }

        public void setPosicao(int posicao) {
            this.posicao = posicao;
        }

        public String getRotulo() {
            return rotulo;
        }

        public void setRotulo(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public boolean isAtual() {
            return atual;
        }

        public void setAtual(boolean atual) {
            this.atual = atual;
        }
    }

    public static class MovimentacaoPublica {
        private String tipo;
        private String titulo;
        private LocalDateTime data;

        public MovimentacaoPublica() {
        }

        public MovimentacaoPublica(String tipo, String titulo, LocalDateTime data) {
            this.tipo = tipo;
            this.titulo = titulo;
            this.data = data;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public LocalDateTime getData() {
            return data;
        }

        public void setData(LocalDateTime data) {
            this.data = data;
        }
    }
}
