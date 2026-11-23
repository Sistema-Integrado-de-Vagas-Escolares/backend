package com.sive.service;

import com.sive.dao.ConsultaPublicaDAO;
import com.sive.exception.AutenticacaoException;
import com.sive.model.ConsultaPublicaRequest;
import com.sive.model.ConsultaPublicaResponse;
import com.sive.model.ConsultaPublicaResponse.ResponsavelPublico;
import com.sive.model.ConsultaPublicaResponse.SolicitacaoPublica;
import com.sive.security.CpfCryptoService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaPublicaService {

    private final ConsultaPublicaDAO consultaPublicaDAO;

    public ConsultaPublicaService() {
        this.consultaPublicaDAO = new ConsultaPublicaDAO();
    }

    public ConsultaPublicaResponse consultar(ConsultaPublicaRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Informe o CPF do responsável e o protocolo do chamado.");
        }

        String cpfDigits = CpfCryptoService.normalizarDigitos(request.getCpf());
        if (cpfDigits.length() != 11) {
            throw new IllegalArgumentException("Informe um CPF válido com 11 dígitos.");
        }

        String protocolo = request.getProtocolo() != null ? request.getProtocolo().trim().toUpperCase() : "";
        if (protocolo.isEmpty()) {
            throw new IllegalArgumentException("Informe o protocolo do chamado.");
        }

        ResponsavelPublico responsavel = consultaPublicaDAO.autenticarPorCpfEProtocolo(cpfDigits, protocolo);
        if (responsavel == null) {
            throw new AutenticacaoException("CPF do responsável ou protocolo do chamado não encontrado.");
        }

        List<SolicitacaoPublica> solicitacoes = consultaPublicaDAO.listarSolicitacoesDoResponsavel(responsavel.getId());
        return new ConsultaPublicaResponse(
                responsavel,
                protocolo,
                LocalDateTime.now(),
                solicitacoes);
    }

    public ConsultaPublicaResponse consultarPorProtocolo(String protocolo) throws SQLException {
        String protocoloNormalizado = protocolo != null ? protocolo.trim().toUpperCase() : "";
        if (protocoloNormalizado.isEmpty()) {
            throw new IllegalArgumentException("Informe o protocolo do chamado.");
        }

        SolicitacaoPublica solicitacao = consultaPublicaDAO.buscarSolicitacaoPorProtocolo(protocoloNormalizado);
        if (solicitacao == null) {
            throw new IllegalArgumentException("Protocolo não encontrado.");
        }

        return new ConsultaPublicaResponse(
                null,
                protocoloNormalizado,
                LocalDateTime.now(),
                List.of(solicitacao));
    }
}
