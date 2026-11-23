package com.sive.service;

import com.sive.dao.ResponsavelDAO;
import com.sive.model.Responsavel;
import com.sive.model.ResponsavelDetalhe;
import com.sive.model.ResponsavelResumo;
import com.sive.security.CpfCryptoService;
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

    public ResponsavelDetalhe buscarDetalhePorEscola(UUID escolaId, UUID responsavelId) throws SQLException {
        return buscarDetalhePorEscola(escolaId, responsavelId, false);
    }

    public ResponsavelDetalhe buscarDetalhePorEscola(
            UUID escolaId,
            UUID responsavelId,
            boolean incluirCpfCompleto) throws SQLException {
        if (escolaId == null || responsavelId == null) {
            throw new IllegalArgumentException("Os identificadores da escola e do responsável são obrigatórios.");
        }
        ResponsavelDetalhe detalhe = responsavelDAO.buscarDetalhePorEscola(escolaId, responsavelId, incluirCpfCompleto);
        if (detalhe == null) {
            throw new IllegalArgumentException("Responsável não encontrado.");
        }
        return detalhe;
    }

    public ResponsavelDetalhe atualizarParaEscola(
            UUID escolaId,
            UUID responsavelId,
            Responsavel dados) throws SQLException {
        if (escolaId == null || responsavelId == null) {
            throw new IllegalArgumentException("Os identificadores da escola e do responsável são obrigatórios.");
        }
        if (dados == null) {
            throw new IllegalArgumentException("Os dados do responsável não podem ser nulos.");
        }

        buscarDetalhePorEscola(escolaId, responsavelId);

        String nome = dados.getNome() != null ? dados.getNome().trim() : "";
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do responsável é obrigatório.");
        }

        String telefone = dados.getTelefone() != null ? dados.getTelefone().trim() : "";
        if (telefone.isEmpty()) {
            throw new IllegalArgumentException("O telefone do responsável é obrigatório.");
        }

        String cpfRaw = dados.getCpf() != null ? dados.getCpf().trim() : "";
        boolean atualizarCpf = !cpfRaw.isEmpty() && !cpfRaw.contains("*");
        if (atualizarCpf) {
            String cpfDigits = CpfCryptoService.normalizarDigitos(cpfRaw);
            if (cpfDigits.length() != 11) {
                throw new IllegalArgumentException("Informe um CPF válido com 11 dígitos para o responsável.");
            }
            Responsavel existente = responsavelDAO.buscarPorCpf(cpfRaw);
            if (existente != null && !responsavelId.equals(existente.getId())) {
                throw new IllegalArgumentException("Já existe outro responsável cadastrado com o CPF informado.");
            }
        }

        Responsavel atualizado = new Responsavel();
        atualizado.setId(responsavelId);
        atualizado.setNome(nome);
        atualizado.setTelefone(telefone);
        atualizado.setCpf(atualizarCpf ? CpfCryptoService.formatar(cpfRaw) : null);

        responsavelDAO.atualizar(atualizado, atualizarCpf);
        return buscarDetalhePorEscola(escolaId, responsavelId);
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
