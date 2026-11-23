package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlunoDetalhe {

    public static class ResponsavelVinculadoDetalhe {
        private UUID id;
        private String nome;
        private String cpf;
        private String telefone;
        private String parentesco;
        private boolean contatoPrincipal;

        public ResponsavelVinculadoDetalhe() {
        }

        public ResponsavelVinculadoDetalhe(
                UUID id,
                String nome,
                String cpf,
                String telefone,
                String parentesco,
                boolean contatoPrincipal) {
            this.id = id;
            this.nome = nome;
            this.cpf = cpf;
            this.telefone = telefone;
            this.parentesco = parentesco;
            this.contatoPrincipal = contatoPrincipal;
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

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getParentesco() {
            return parentesco;
        }

        public void setParentesco(String parentesco) {
            this.parentesco = parentesco;
        }

        public boolean isContatoPrincipal() {
            return contatoPrincipal;
        }

        public void setContatoPrincipal(boolean contatoPrincipal) {
            this.contatoPrincipal = contatoPrincipal;
        }
    }

    private UUID id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private Endereco endereco;
    private BigDecimal rendaPerCapita;
    private boolean temNecessidadeEspecial;
    private boolean vulnerabilidadeSocioeconomica;
    private boolean responsavelSolo;
    private boolean moraDeAluguel;
    private boolean bolsaFamilia;
    private boolean bpcLoas;
    private boolean irmaoMatriculado;
    private String protocolo;
    private String etapaOuTurma;
    private String serieAno;
    private String turmaNome;
    private String tipoVaga;
    private String turno;
    private BigDecimal pontuacaoTotal;
    private String statusInscricao;
    private LocalDateTime dataInscricao;
    private LocalDateTime dataChamada;
    private List<ResponsavelVinculadoDetalhe> responsaveis;

    public AlunoDetalhe() {
        this.responsaveis = new ArrayList<>();
    }

    public AlunoDetalhe(
            UUID id,
            String nome,
            String cpf,
            LocalDate dataNascimento,
            Endereco endereco,
            BigDecimal rendaPerCapita,
            boolean temNecessidadeEspecial,
            String protocolo,
            String etapaOuTurma,
            String turmaNome,
            String tipoVaga,
            String turno,
            BigDecimal pontuacaoTotal,
            String statusInscricao,
            LocalDateTime dataInscricao,
            LocalDateTime dataChamada,
            List<ResponsavelVinculadoDetalhe> responsaveis) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.rendaPerCapita = rendaPerCapita;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
        this.protocolo = protocolo;
        this.etapaOuTurma = etapaOuTurma;
        this.turmaNome = turmaNome;
        this.tipoVaga = tipoVaga;
        this.turno = turno;
        this.pontuacaoTotal = pontuacaoTotal;
        this.statusInscricao = statusInscricao;
        this.dataInscricao = dataInscricao;
        this.dataChamada = dataChamada;
        this.responsaveis = responsaveis != null ? responsaveis : new ArrayList<>();
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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public BigDecimal getRendaPerCapita() {
        return rendaPerCapita;
    }

    public void setRendaPerCapita(BigDecimal rendaPerCapita) {
        this.rendaPerCapita = rendaPerCapita;
    }

    public boolean isTemNecessidadeEspecial() {
        return temNecessidadeEspecial;
    }

    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) {
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public boolean isVulnerabilidadeSocioeconomica() {
        return vulnerabilidadeSocioeconomica;
    }

    public void setVulnerabilidadeSocioeconomica(boolean vulnerabilidadeSocioeconomica) {
        this.vulnerabilidadeSocioeconomica = vulnerabilidadeSocioeconomica;
    }

    public boolean isResponsavelSolo() {
        return responsavelSolo;
    }

    public void setResponsavelSolo(boolean responsavelSolo) {
        this.responsavelSolo = responsavelSolo;
    }

    public boolean isMoraDeAluguel() {
        return moraDeAluguel;
    }

    public void setMoraDeAluguel(boolean moraDeAluguel) {
        this.moraDeAluguel = moraDeAluguel;
    }

    public boolean isBolsaFamilia() {
        return bolsaFamilia;
    }

    public void setBolsaFamilia(boolean bolsaFamilia) {
        this.bolsaFamilia = bolsaFamilia;
    }

    public boolean isBpcLoas() {
        return bpcLoas;
    }

    public void setBpcLoas(boolean bpcLoas) {
        this.bpcLoas = bpcLoas;
    }

    public boolean isIrmaoMatriculado() {
        return irmaoMatriculado;
    }

    public void setIrmaoMatriculado(boolean irmaoMatriculado) {
        this.irmaoMatriculado = irmaoMatriculado;
    }

    public String getSerieAno() {
        return serieAno;
    }

    public void setSerieAno(String serieAno) {
        this.serieAno = serieAno;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getEtapaOuTurma() {
        return etapaOuTurma;
    }

    public void setEtapaOuTurma(String etapaOuTurma) {
        this.etapaOuTurma = etapaOuTurma;
    }

    public String getTurmaNome() {
        return turmaNome;
    }

    public void setTurmaNome(String turmaNome) {
        this.turmaNome = turmaNome;
    }

    public String getTipoVaga() {
        return tipoVaga;
    }

    public void setTipoVaga(String tipoVaga) {
        this.tipoVaga = tipoVaga;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public BigDecimal getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public String getStatusInscricao() {
        return statusInscricao;
    }

    public void setStatusInscricao(String statusInscricao) {
        this.statusInscricao = statusInscricao;
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

    public List<ResponsavelVinculadoDetalhe> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<ResponsavelVinculadoDetalhe> responsaveis) {
        this.responsaveis = responsaveis;
    }
}
