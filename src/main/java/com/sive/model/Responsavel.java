package com.sive.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade de dominio que representa o Responsavel Legal de um aluno.
 * Extende {@link Pessoa}, herdando o identificador unico e o nome.
 *
 * <p>Mapeada para a tabela {@code responsaveis} no banco de dados.</p>
 */
public class Responsavel extends Pessoa {

    private String cpf;
    private String telefone;
    private BigDecimal rendaFamiliar;

    /**
     * Construtor padrao necessario para frameworks de serializacao (Jackson).
     */
    public Responsavel() {
        super();
    }

    /**
     * Construtor para novos responsaveis (sem ID gerado previamente).
     *
     * @param nome          Nome completo do responsavel.
     * @param cpf           CPF no formato padrao (ex: 123.456.789-00).
     * @param telefone      Numero de telefone de contato.
     * @param rendaFamiliar Renda familiar mensal bruta em reais.
     */
    public Responsavel(String nome, String cpf, String telefone, BigDecimal rendaFamiliar) {
        super(nome);
        this.cpf = cpf;
        this.telefone = telefone;
        this.rendaFamiliar = rendaFamiliar;
    }

    /**
     * Construtor completo para responsaveis ja persistidos no banco de dados.
     *
     * @param id            Identificador unico (UUID).
     * @param nome          Nome completo do responsavel.
     * @param cpf           CPF no formato padrao.
     * @param telefone      Numero de telefone de contato.
     * @param rendaFamiliar Renda familiar mensal bruta em reais.
     */
    public Responsavel(UUID id, String nome, String cpf, String telefone, BigDecimal rendaFamiliar) {
        super(id, nome);
        this.cpf = cpf;
        this.telefone = telefone;
        this.rendaFamiliar = rendaFamiliar;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

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

    public BigDecimal getRendaFamiliar() {
        return rendaFamiliar;
    }

    public void setRendaFamiliar(BigDecimal rendaFamiliar) {
        this.rendaFamiliar = rendaFamiliar;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Responsavel{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", cpf='" + cpf + '\'' +
                ", telefone='" + telefone + '\'' +
                ", rendaFamiliar=" + rendaFamiliar +
                '}';
    }
}

