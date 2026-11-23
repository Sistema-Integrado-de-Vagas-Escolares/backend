package com.sive.service;

import com.sive.dao.ResponsavelDAO;
import com.sive.model.Responsavel;
import com.sive.model.ResponsavelResumo;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
public class ResponsavelService {

    private final ResponsavelDAO responsavelDAO;

    public ResponsavelService() {
        this.responsavelDAO = new ResponsavelDAO();
    }

    public Responsavel cadastrar(Responsavel responsavel) throws SQLException {
        validar(responsavel);

        Responsavel existente = responsavelDAO.buscarPorCpf(responsavel.getCpf());
        if (existente != null) {
            throw new IllegalArgumentException("Já existe um responsável cadastrado com o CPF informado.");
        }

        return responsavelDAO.inserir(responsavel);
    }

    public Responsavel buscarPorId(UUID id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("O ID do responsável não pode ser nulo.");
        }
        return responsavelDAO.buscarPorId(id);
    }

    public Responsavel buscarPorCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do responsável não pode ser nulo ou vazio.");
        }
        return responsavelDAO.buscarPorCpf(cpf.trim());
    }

    public List<Responsavel> listarTodos() throws SQLException {
        return responsavelDAO.listar();
    }

    public List<ResponsavelResumo> listarComTotalFilhos(String busca, Boolean comFilhos) throws SQLException {
        return responsavelDAO.listarComTotalFilhos(busca, comFilhos);
    }

    private void validar(Responsavel responsavel) {
        if (responsavel == null) {
            throw new IllegalArgumentException("Os dados do responsável não podem ser nulos.");
        }
        if (responsavel.getNome() == null || responsavel.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do responsável é obrigatório.");
        }
        if (responsavel.getCpf() == null || responsavel.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do responsável é obrigatório.");
        }
        if (responsavel.getTelefone() == null || responsavel.getTelefone().trim().isEmpty()) {
            throw new IllegalArgumentException("O telefone do responsável é obrigatório.");
        }
    }
}
