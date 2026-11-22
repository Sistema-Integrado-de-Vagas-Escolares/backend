package com.sive.model;

/**
 * DTO de resposta da autenticação contendo o token Bearer e os dados públicos da escola.
 */
public class LoginEscolaResponse {

    private String token;
    private String tipo;
    private long expiraEmSegundos;
    private Escola escola;

    public LoginEscolaResponse() {
    }

    public LoginEscolaResponse(String token, String tipo, long expiraEmSegundos, Escola escola) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEmSegundos = expiraEmSegundos;
        this.escola = escola;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getExpiraEmSegundos() {
        return expiraEmSegundos;
    }

    public void setExpiraEmSegundos(long expiraEmSegundos) {
        this.expiraEmSegundos = expiraEmSegundos;
    }

    public Escola getEscola() {
        return escola;
    }

    public void setEscola(Escola escola) {
        this.escola = escola;
    }
}
