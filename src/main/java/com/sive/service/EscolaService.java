package com.sive.service;

import com.sive.dao.EscolaDAO;
import com.sive.model.Escola;
import com.sive.security.PasswordHasher;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Camada de serviço responsável pelas regras de negócio e validações da entidade Escola.
 */
@Service
public class EscolaService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final EscolaDAO escolaDAO;

    public EscolaService() {
        this.escolaDAO = new EscolaDAO();
    }

    public EscolaService(EscolaDAO escolaDAO) {
        this.escolaDAO = escolaDAO;
    }

    /**
     * Cadastra uma nova escola após aplicar as validações de negócio e gerar o hash da senha.
     */
    public Escola cadastrar(Escola escola) throws SQLException {
        if (escola == null) {
            throw new IllegalArgumentException("Os dados da escola não podem ser nulos.");
        }
        if (escola.getCodigo() == null || escola.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("O código da escola é obrigatório.");
        }
        String codigoNormalizado = escola.getCodigo().trim().toUpperCase();
        escola.setCodigo(codigoNormalizado);

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
        if (escola.getSenha() == null || escola.getSenha().trim().length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                    "A senha de acesso é obrigatória e deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }

        if (escolaDAO.existePorCodigo(codigoNormalizado)) {
            throw new IllegalArgumentException("Já existe uma escola cadastrada com o código: " + codigoNormalizado);
        }

        escola.setSenhaHash(PasswordHasher.hash(escola.getSenha()));
        escola.setSenha(null);

        return escolaDAO.inserir(escola);
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
