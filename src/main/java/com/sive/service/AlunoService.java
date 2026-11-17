package com.sive.service;

import com.sive.dao.AlunoDAO;
import com.sive.dao.AlunoResponsavelDAO;
import com.sive.dao.Conexao;
import com.sive.dao.ResponsavelDAO;
import com.sive.model.Aluno;
import com.sive.model.AlunoResponsavel;
import com.sive.model.Parentesco;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AlunoService {

    private final AlunoDAO alunoDAO;
    private final AlunoResponsavelDAO alunoResponsavelDAO;
    private final ResponsavelDAO responsavelDAO;

    public AlunoService() {
        this.alunoDAO = new AlunoDAO();
        this.alunoResponsavelDAO = new AlunoResponsavelDAO();
        this.responsavelDAO = new ResponsavelDAO();
    }

    public Aluno cadastrar(Aluno aluno, UUID responsavelId, Parentesco parentesco) throws SQLException {
        validar(aluno);

        if (responsavelId == null) {
            throw new IllegalArgumentException("O ID do responsável é obrigatório para cadastrar um aluno.");
        }
        if (parentesco == null) {
            throw new IllegalArgumentException("O parentesco entre aluno e responsável é obrigatório.");
        }
        if (responsavelDAO.buscarPorId(responsavelId) == null) {
            throw new IllegalArgumentException(
                    "Responsável não encontrado para o ID informado: " + responsavelId);
        }
        if (alunoDAO.buscarPorCertidao(aluno.getCertidaoNascimento()) != null) {
            throw new IllegalArgumentException(
                    "Já existe um aluno cadastrado com a certidão de nascimento informada: "
                            + aluno.getCertidaoNascimento());
        }

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                alunoDAO.inserir(aluno, conn);

                AlunoResponsavel vinculo = new AlunoResponsavel(
                        aluno.getId(), responsavelId, parentesco, true);
                alunoResponsavelDAO.vincular(vinculo, conn);

                conn.commit();
                return aluno;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Aluno buscarPorId(UUID id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("O ID do aluno não pode ser nulo.");
        }
        return alunoDAO.buscarPorId(id);
    }

    public List<Aluno> listarTodos() throws SQLException {
        return alunoDAO.listar();
    }

    public List<AlunoResponsavel> listarResponsaveis(UUID alunoId) throws SQLException {
        if (alunoId == null) {
            throw new IllegalArgumentException("O ID do aluno não pode ser nulo.");
        }
        if (alunoDAO.buscarPorId(alunoId) == null) {
            throw new IllegalArgumentException("Aluno não encontrado para o ID informado: " + alunoId);
        }
        return alunoResponsavelDAO.listarPorAluno(alunoId);
    }

    private void validar(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Os dados do aluno não podem ser nulos.");
        }
        if (aluno.getNome() == null || aluno.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
        if (aluno.getDataNascimento() == null) {
            throw new IllegalArgumentException("A data de nascimento do aluno é obrigatória.");
        }
        if (aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode ser uma data futura.");
        }
        if (aluno.getCertidaoNascimento() == null || aluno.getCertidaoNascimento().trim().isEmpty()) {
            throw new IllegalArgumentException("A certidão de nascimento do aluno é obrigatória.");
        }
    }
}
