package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Payload completo para cadastro de aluno e inscrição na lista de espera.
 */
public class CadastrarAlunoInscricaoRequest {

    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String tipoVaga;
    private String serieAno;
    private String turno;
    private Endereco endereco;
    private BigDecimal rendaPerCapita;
    private boolean vulnerabilidadeSocioeconomica;
    private boolean responsavelSolo;
    private boolean moraDeAluguel;
    private boolean bolsaFamilia;
    private boolean bpcLoas;
    private boolean temNecessidadeEspecial;
    private boolean irmaoMatriculado;
    private List<ResponsavelVinculoItem> responsaveis;

    public CadastrarAlunoInscricaoRequest() {
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

    public boolean isTemNecessidadeEspecial() {
        return temNecessidadeEspecial;
    }

    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) {
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public boolean isIrmaoMatriculado() {
        return irmaoMatriculado;
    }

    public void setIrmaoMatriculado(boolean irmaoMatriculado) {
        this.irmaoMatriculado = irmaoMatriculado;
    }

    public List<ResponsavelVinculoItem> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<ResponsavelVinculoItem> responsaveis) {
        this.responsaveis = responsaveis;
    }

    public static class ResponsavelVinculoItem {

        private UUID responsavelId;
        private String parentesco;

        public ResponsavelVinculoItem() {
        }

        public UUID getResponsavelId() {
            return responsavelId;
        }

        public void setResponsavelId(UUID responsavelId) {
            this.responsavelId = responsavelId;
        }

        public String getParentesco() {
            return parentesco;
        }

        public void setParentesco(String parentesco) {
            this.parentesco = parentesco;
        }
    }
}
