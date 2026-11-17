package com.sive.model;

import java.util.UUID;

/**
 * Entidade de dominio que representa a associacao N:N entre um {@link Aluno}
 * e um {@link Responsavel} legal.
 *
 * <p>Mapeada para a tabela {@code aluno_responsaveis} no banco de dados.
 * Registra o grau de parentesco e indica qual responsavel e o contato
 * prioritario para o aluno.</p>
 */
public class AlunoResponsavel {

    private UUID id;
    private UUID alunoId;
    private UUID responsavelId;
    private Parentesco parentesco;
    private boolean contatoPrincipal;

    /**
     * Construtor padrao necessario para frameworks de serializacao (Jackson).
     */
    public AlunoResponsavel() {
    }

    /**
     * Construtor para novos vinculos (sem ID gerado previamente).
     *
     * @param alunoId          UUID do aluno vinculado.
     * @param responsavelId    UUID do responsavel legal vinculado.
     * @param parentesco       Grau de parentesco entre aluno e responsavel.
     * @param contatoPrincipal Indica se este responsavel e o contato prioritario.
     */
    public AlunoResponsavel(UUID alunoId, UUID responsavelId, Parentesco parentesco,
            boolean contatoPrincipal) {
        this.alunoId = alunoId;
        this.responsavelId = responsavelId;
        this.parentesco = parentesco;
        this.contatoPrincipal = contatoPrincipal;
    }

    /**
     * Construtor completo para vinculos ja persistidos no banco de dados.
     *
     * @param id               Identificador unico (UUID) do vinculo.
     * @param alunoId          UUID do aluno vinculado.
     * @param responsavelId    UUID do responsavel legal vinculado.
     * @param parentesco       Grau de parentesco entre aluno e responsavel.
     * @param contatoPrincipal Indica se este responsavel e o contato prioritario.
     */
    public AlunoResponsavel(UUID id, UUID alunoId, UUID responsavelId, Parentesco parentesco,
            boolean contatoPrincipal) {
        this.id = id;
        this.alunoId = alunoId;
        this.responsavelId = responsavelId;
        this.parentesco = parentesco;
        this.contatoPrincipal = contatoPrincipal;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(UUID alunoId) {
        this.alunoId = alunoId;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(UUID responsavelId) {
        this.responsavelId = responsavelId;
    }

    public Parentesco getParentesco() {
        return parentesco;
    }

    public void setParentesco(Parentesco parentesco) {
        this.parentesco = parentesco;
    }

    public boolean isContatoPrincipal() {
        return contatoPrincipal;
    }

    public void setContatoPrincipal(boolean contatoPrincipal) {
        this.contatoPrincipal = contatoPrincipal;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "AlunoResponsavel{" +
                "id=" + id +
                ", alunoId=" + alunoId +
                ", responsavelId=" + responsavelId +
                ", parentesco=" + parentesco +
                ", contatoPrincipal=" + contatoPrincipal +
                '}';
    }
}

