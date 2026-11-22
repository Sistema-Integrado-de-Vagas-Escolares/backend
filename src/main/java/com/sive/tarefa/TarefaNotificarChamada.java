package com.sive.tarefa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.sive.dao.Conexao;

/**
 * Tarefa concorrente (Runnable) para simular o envio assíncrono de aviso/notificação
 * de convocação para o responsável legal do aluno chamado.
 */
public class TarefaNotificarChamada implements Runnable {

    private final UUID inscricaoId;
    private boolean sucesso;
    private String mensagemLog;
    private Exception erro;

    public TarefaNotificarChamada(UUID inscricaoId) {
        if (inscricaoId == null) {
            throw new IllegalArgumentException("O ID da inscrição é obrigatório para notificação.");
        }
        this.inscricaoId = inscricaoId;
    }

    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();
        System.out.println("[" + nomeThread + "] Iniciando processo de notificação para inscrição: " + inscricaoId);

        String sql = "SELECT i.protocolo, a.nome AS aluno_nome, r.nome AS responsavel_nome, r.telefone "
                + "FROM inscricoes i "
                + "JOIN alunos a ON i.aluno_id = a.id "
                + "JOIN aluno_responsaveis ar ON a.id = ar.aluno_id "
                + "JOIN responsaveis r ON ar.responsavel_id = r.id "
                + "WHERE i.id = ? "
                + "ORDER BY ar.is_contato_principal DESC LIMIT 1";

        try {
            // Simulação de envio externo de mensagem/e-mail com I/O
            Thread.sleep(150);

            try (Connection conn = Conexao.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setObject(1, inscricaoId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String protocolo = rs.getString("protocolo");
                        String aluno = rs.getString("aluno_nome");
                        String responsavel = rs.getString("responsavel_nome");
                        String telefone = rs.getString("telefone");

                        this.mensagemLog = String.format(
                                "Notificação enviada com SUCESSO! Responsável: %s, Telefone: %s, Aluno: %s, Protocolo: %s",
                                responsavel, telefone, aluno, protocolo);
                        this.sucesso = true;
                    } else {
                        this.mensagemLog = "Inscrição ou responsável não localizado para envio de notificação.";
                        this.sucesso = false;
                    }
                }
            }

            System.out.println("[" + nomeThread + "] " + mensagemLog);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.erro = e;
            System.err.println("[" + nomeThread + "] Thread de notificação foi interrompida.");
        } catch (SQLException e) {
            this.erro = e;
            System.err.println("[" + nomeThread + "] Erro ao consultar dados para notificação: " + e.getMessage());
        }
    }

    public UUID getInscricaoId() {
        return inscricaoId;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagemLog() {
        return mensagemLog;
    }

    public Exception getErro() {
        return erro;
    }
}
