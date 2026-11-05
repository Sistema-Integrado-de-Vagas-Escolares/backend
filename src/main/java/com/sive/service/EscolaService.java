package com.sive.service;

import com.sive.dao.EscolaDAO;
import com.sive.model.Escola;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Camada de servico responsavel pelas regras de negocio e validacoes da
 * entidade Escola.
 */
@Service
public class EscolaService {

    private final EscolaDAO escolaDAO;

    public EscolaService() {
        this.escolaDAO = new EscolaDAO();
    }

    /**
     * Cadastra uma nova escola apos aplicar as validacoes de negocio.
     */
    public Escola cadastrar(Escola escola) throws SQLException {
        if (escola == null) {
            throw new IllegalArgumentException("Os dados da escola não podem ser nulos.");
        }
        if (escola.getNome() == null || escola.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da escola é obrigatório.");
        }
        if (escola.getBairro() == null || escola.getBairro().trim().isEmpty()) {
            throw new IllegalArgumentException("O bairro da escola é obrigatório.");
        }
        if (escola.getCidade() == null || escola.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException("A cidade da escola é obrigatória.");
        }
        if (escola.getCapacidadeTotal() <= 0) {
            throw new IllegalArgumentException("A capacidade total deve ser maior que zero.");
        }

        return escolaDAO.inserir(escola);
    }

    /**
     * Lista todas as escolas cadastradas.
     */
    public List<Escola> listarTodas() throws SQLException {
        return escolaDAO.listar();
    }

    /**
     * Busca uma escola pelo seu identificador.
     */
    public Escola buscarPorId(UUID id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("O ID da escola não pode ser nulo.");
        }
        return escolaDAO.buscarPorId(id);
    }
}
