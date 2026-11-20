package com.sive.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerenciador de conexão JDBC com o banco de dados PostgreSQL.
 * Suporta configurações locais e variáveis de ambiente de nuvem (Fly.io).
 */
public final class Conexao {

    private static String url;
    private static String usuario;
    private static String senha;

    static {
        configurarParametrosConexao();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do PostgreSQL não encontrado no classpath!");
        }
    }

    private Conexao() {
    }

    private static void configurarParametrosConexao() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            try {
                if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
                    URI dbUri = new URI(databaseUrl);
                    String userInfo = dbUri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        usuario = parts[0];
                        senha = parts[1];
                    } else {
                        usuario = userInfo != null ? userInfo : "sive_user";
                        senha = "";
                    }

                    int port = dbUri.getPort() > 0 ? dbUri.getPort() : 5432;
                    String path = dbUri.getPath();
                    if (path != null && path.startsWith("/")) {
                        path = path.substring(1);
                    }

                    String query = dbUri.getQuery();
                    StringBuilder queryParams = new StringBuilder();
                    if (query != null && !query.isEmpty()) {
                        queryParams.append("?").append(query);
                        if (!query.contains("sslmode")) {
                            queryParams.append("&sslmode=disable");
                        }
                    } else {
                        queryParams.append("?sslmode=disable");
                    }

                    url = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + "/" + path + queryParams.toString();
                } else {
                    url = databaseUrl;
                    usuario = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "sive_user";
                    senha = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "sive_password";
                }
            } catch (URISyntaxException e) {
                System.err.println("Erro ao interpretar DATABASE_URL, aplicando fallback: " + e.getMessage());
                url = "jdbc:postgresql://localhost:5432/sive_db";
                usuario = "sive_user";
                senha = "sive_password";
            }
        } else {
            url = System.getenv("DB_URL") != null
                    ? System.getenv("DB_URL")
                    : "jdbc:postgresql://localhost:5432/sive_db";

            usuario = System.getenv("DB_USER") != null
                    ? System.getenv("DB_USER")
                    : "sive_user";

            senha = System.getenv("DB_PASSWORD") != null
                    ? System.getenv("DB_PASSWORD")
                    : "sive_password";
        }
    }

    /**
     * Obtém uma conexãoo ativa com o banco PostgreSQL.
     * 
     * @return Connection aberta
     * @throws SQLException caso ocorra falha de autenticacao ou conectividade
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

    /**
     * Testa a conectividade com o banco de dados.
     */
    public static void main(String[] args) {
        System.out.println("Tentando conectar ao PostgreSQL em: " + url);
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexao com o PostgreSQL realizada com SUCESSO!");
                System.out.println("Banco: " + conn.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println("Falha ao conectar ao banco de dados: " + e.getMessage());
            System.err.println("Certifique-se de que o container Docker esta rodando (docker compose up -d).");
        }
    }
}
