package com.sive.controller;

import com.sive.model.Aluno;
import com.sive.model.AlunoResponsavel;
import com.sive.model.Parentesco;
import com.sive.service.AlunoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, Object> payload) {
        try {
            Aluno aluno = extrairAluno(payload);
            UUID responsavelId = extrairResponsavelId(payload);
            Parentesco parentesco = extrairParentesco(payload);

            Aluno criado = alunoService.cadastrar(aluno, responsavelId, parentesco);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<Aluno> alunos = alunoService.listarTodos();
            return ResponseEntity.ok(alunos);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar alunos no banco de dados: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        try {
            Aluno aluno = alunoService.buscarPorId(id);
            if (aluno == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Aluno nao encontrado para o ID informado.");
            }
            return ResponseEntity.ok(aluno);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar aluno no banco de dados: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/responsaveis")
    public ResponseEntity<?> listarResponsaveis(@PathVariable UUID id) {
        try {
            List<AlunoResponsavel> vinculos = alunoService.listarResponsaveis(id);
            return ResponseEntity.ok(vinculos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar responsaveis do aluno: " + e.getMessage());
        }
    }

    private Aluno extrairAluno(Map<String, Object> payload) {
        Aluno aluno = new Aluno();

        Object nome = payload.get("nome");
        if (nome != null) {
            aluno.setNome(nome.toString());
        }

        Object dataNascimento = payload.get("dataNascimento");
        if (dataNascimento != null) {
            aluno.setDataNascimento(java.time.LocalDate.parse(dataNascimento.toString()));
        }

        Object certidao = payload.get("certidaoNascimento");
        if (certidao != null) {
            aluno.setCertidaoNascimento(certidao.toString());
        }

        Object necessidade = payload.get("temNecessidadeEspecial");
        if (necessidade instanceof Boolean) {
            aluno.setTemNecessidadeEspecial((Boolean) necessidade);
        }

        return aluno;
    }

    private UUID extrairResponsavelId(Map<String, Object> payload) {
        Object responsavelId = payload.get("responsavelId");
        if (responsavelId == null) {
            throw new IllegalArgumentException("O campo 'responsavelId' e obrigatorio.");
        }
        try {
            return UUID.fromString(responsavelId.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O campo 'responsavelId' deve ser um UUID valido.");
        }
    }

    private Parentesco extrairParentesco(Map<String, Object> payload) {
        Object parentesco = payload.get("parentesco");
        if (parentesco == null) {
            return Parentesco.MAE;
        }
        Parentesco resultado = Parentesco.fromString(parentesco.toString());
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "O campo 'parentesco' deve ser um dos valores: MAE, PAI, AVOS, TUTOR_LEGAL, OUTRO.");
        }
        return resultado;
    }
}
