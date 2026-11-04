package com.sive.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerenciador de conexao JDBC com o banco de dados PostgreSQL.
 */
public final class Conexao {

    private static final String URL = System.getenv("DB_URL") != null 
            ? System.getenv("DB_URL") 
            : "jdbc:postgresql://localhost:5432/sive_db";

    private static final String USUARIO = System.getenv("DB_USER") != null 
            ? System.getenv("DB_USER") 
            : "sive_user";

    private static final String SENHA = System.getenv("DB_PASSWORD") != null 
            ? System.getenv("DB_PASSWORD") 
            : "sive_password";

    static {
        try {
            // Carrega explicitamente o driver do PostgreSQL
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do PostgreSQL nao encontrado no classpath!");
        }
    }

    private Conexao() {
        // Construtor privado para evitar instanciacao
    }

    /**
     * Obtem uma conexao ativa com o banco PostgreSQL.
     * @return Connection aberta
     * @throws SQLException caso ocorra falha de autenticacao ou conectividade
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /**
     * Metodo utilitario para testar a conectividade com o banco de dados.
     */
    public static void main(String[] args) {
        System.out.println("Tentando conectar ao PostgreSQL em: " + URL);
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
