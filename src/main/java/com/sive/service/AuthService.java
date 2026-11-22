package com.sive.service;

import com.sive.dao.EscolaDAO;
import com.sive.exception.AutenticacaoException;
import com.sive.model.Escola;
import com.sive.model.LoginEscolaRequest;
import com.sive.model.LoginEscolaResponse;
import com.sive.security.PasswordHasher;
import com.sive.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * Serviço responsável pela autenticação de escolas por código e senha.
 */
@Service
public class AuthService {

    private final EscolaDAO escolaDAO;
    private final TokenService tokenService;

    @Autowired
    public AuthService(TokenService tokenService) {
        this(new EscolaDAO(), tokenService);
    }

    public AuthService(EscolaDAO escolaDAO, TokenService tokenService) {
        this.escolaDAO = escolaDAO;
        this.tokenService = tokenService;
    }

    /**
     * Autentica uma escola pelo código e senha e retorna um token Bearer assinado.
     */
    public LoginEscolaResponse autenticar(LoginEscolaRequest request) throws SQLException {
        if (request == null
                || request.getCodigo() == null || request.getCodigo().trim().isEmpty()
                || request.getSenha() == null || request.getSenha().isEmpty()) {
            throw new IllegalArgumentException("Código da escola e senha são obrigatórios.");
        }

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        Escola escola = escolaDAO.buscarPorCodigo(codigoNormalizado);

        if (escola == null || !PasswordHasher.verificar(request.getSenha(), escola.getSenhaHash())) {
            throw new AutenticacaoException("Código ou senha inválidos.");
        }

        escola.setSenhaHash(null);
        String token = tokenService.gerarToken(escola.getId(), escola.getCodigo());

        return new LoginEscolaResponse(
                token,
                "Bearer",
                tokenService.getExpirationSeconds(),
                escola);
    }
}
