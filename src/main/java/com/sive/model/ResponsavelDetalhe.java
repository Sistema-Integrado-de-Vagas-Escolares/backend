package com.sive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResponsavelDetalhe {

    public static class FilhoVinculadoResumo {
        private UUID id;
        private String nome;
        private String cpf;
        private LocalDate dataNascimento;
        private String etapaOuTurma;
        private String turmaNome;
        private String statusInscricao;
        private BigDecimal pontuacaoTotal;
        private String parentesco;
        private boolean contatoPrincipal;

        public FilhoVinculadoResumo() {
        }

        public FilhoVinculadoResumo(
                UUID id,
                String nome,
                String cpf,
                LocalDate dataNascimento,
                String etapaOuTurma,
                String turmaNome,
                String statusInscricao,
                BigDecimal pontuacaoTotal,
                String parentesco,
                boolean contatoPrincipal) {
            this.id = id;
            this.nome = nome;
            this.cpf = cpf;
            this.dataNascimento = dataNascimento;
            this.etapaOuTurma = etapaOuTurma;
            this.turmaNome = turmaNome;
            this.statusInscricao = statusInscricao;
            this.pontuacaoTotal = pontuacaoTotal;
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

        public LocalDate getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
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

        public String getStatusInscricao() {
            return statusInscricao;
        }

        public void setStatusInscricao(String statusInscricao) {
            this.statusInscricao = statusInscricao;
        }

        public BigDecimal getPontuacaoTotal() {
            return pontuacaoTotal;
        }

        public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
            this.pontuacaoTotal = pontuacaoTotal;
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
    private String telefone;
    private List<FilhoVinculadoResumo> filhos;

    public ResponsavelDetalhe() {
        this.filhos = new ArrayList<>();
    }

    public ResponsavelDetalhe(
            UUID id,
            String nome,
            String cpf,
            String telefone,
            List<FilhoVinculadoResumo> filhos) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.filhos = filhos != null ? filhos : new ArrayList<>();
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

    public List<FilhoVinculadoResumo> getFilhos() {
        return filhos;
    }

    public void setFilhos(List<FilhoVinculadoResumo> filhos) {
        this.filhos = filhos;
    }
}
