package com.sive.service;

import com.sive.dao.AlunoDAO;
import com.sive.dao.AlunoResponsavelDAO;
import com.sive.dao.Conexao;
import com.sive.dao.CriterioDAO;
import com.sive.dao.InscricaoCriterioDAO;
import com.sive.dao.ResponsavelDAO;
import com.sive.model.Aluno;
import com.sive.model.AlunoDetalhe;
import com.sive.model.AlunoResponsavel;
import com.sive.model.AlunoResumo;
import com.sive.model.CadastrarAlunoInscricaoRequest;
import com.sive.model.Criterio;
import com.sive.model.Endereco;
import com.sive.model.Parentesco;
import com.sive.security.CpfCryptoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AlunoService {

    private final AlunoDAO alunoDAO;
    private final AlunoResponsavelDAO alunoResponsavelDAO;
    private final ResponsavelDAO responsavelDAO;
    private final CriterioDAO criterioDAO;
    private final InscricaoCriterioDAO inscricaoCriterioDAO;
    private final CriterioService criterioService;

    public AlunoService() {
        this.alunoDAO = new AlunoDAO();
        this.alunoResponsavelDAO = new AlunoResponsavelDAO();
        this.responsavelDAO = new ResponsavelDAO();
        this.criterioDAO = new CriterioDAO();
        this.inscricaoCriterioDAO = new InscricaoCriterioDAO();
        this.criterioService = new CriterioService(this.criterioDAO);
    }

    public Aluno cadastrar(Aluno aluno, UUID responsavelId, Parentesco parentesco) throws SQLException {
        validar(aluno);

        if (responsavelId == null) {
            throw new IllegalArgumentException("O ID do responsável é obrigatório para cadastrar um aluno.");
        }
        if (parentesco == null) {
            throw new IllegalArgumentException("O parentesco entre aluno e responsável é obrigatório.");
        }
        if (responsavelDAO.buscarPorId(responsavelId) == null) {
            throw new IllegalArgumentException(
                    "Responsável não encontrado para o ID informado: " + responsavelId);
        }
        if (alunoDAO.buscarPorCpf(aluno.getCpf()) != null) {
            throw new IllegalArgumentException("Já existe um aluno cadastrado com o CPF informado.");
        }

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                alunoDAO.inserir(aluno, conn);

                AlunoResponsavel vinculo = new AlunoResponsavel(
                        aluno.getId(), responsavelId, parentesco, true);
                alunoResponsavelDAO.vincular(vinculo, conn);

                conn.commit();
                return aluno;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Cadastra o aluno, vincula os responsáveis e cria a inscrição na lista de espera da escola.
     */
    public AlunoResumo cadastrarComInscricao(
            UUID escolaId,
            CadastrarAlunoInscricaoRequest request) throws SQLException {
        if (escolaId == null) {
            throw new IllegalArgumentException("O ID da escola é obrigatório.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Os dados da inscrição não podem ser nulos.");
        }

        String tipoVaga = normalizarTipoVaga(request.getTipoVaga());
        if (request.getSerieAno() == null || request.getSerieAno().trim().isEmpty()) {
            throw new IllegalArgumentException("O nível escolar pretendido é obrigatório.");
        }
        String serieAno = request.getSerieAno().trim();
        String turno = normalizarTurno(request.getTurno());
        Endereco enderecoNormalizado = validarENormalizarEndereco(request.getEndereco());

        BigDecimal rendaPerCapita = request.getRendaPerCapita() != null
                ? request.getRendaPerCapita()
                : BigDecimal.ZERO;
        if (rendaPerCapita.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A renda familiar per capita não pode ser negativa.");
        }

        List<CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem> responsaveis = request.getResponsaveis();
        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new IllegalArgumentException("Vincule pelo menos um responsável ao aluno.");
        }

        Aluno aluno = new Aluno();
        aluno.setNome(request.getNome() != null ? request.getNome().trim() : null);
        aluno.setCpf(request.getCpf() != null ? request.getCpf().trim() : null);
        aluno.setDataNascimento(request.getDataNascimento());
        aluno.setEndereco(enderecoNormalizado);
        aluno.setRendaPerCapita(rendaPerCapita);
        aluno.setTemNecessidadeEspecial(request.isTemNecessidadeEspecial());
        aluno.setVulnerabilidadeSocioeconomica(request.isVulnerabilidadeSocioeconomica());
        aluno.setResponsavelSolo(request.isResponsavelSolo());
        aluno.setMoraDeAluguel(request.isMoraDeAluguel());
        aluno.setBolsaFamilia(request.isBolsaFamilia());
        aluno.setBpcLoas(request.isBpcLoas());
        aluno.setIrmaoMatriculado(request.isIrmaoMatriculado());
        validar(aluno);

        if (alunoDAO.buscarPorCpf(aluno.getCpf()) != null) {
            throw new IllegalArgumentException("Já existe um aluno cadastrado com o CPF informado.");
        }

        for (CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem item : responsaveis) {
            if (item == null || item.getResponsavelId() == null) {
                throw new IllegalArgumentException("Todos os responsáveis vinculados devem possuir um ID válido.");
            }
            if (responsavelDAO.buscarPorId(item.getResponsavelId()) == null) {
                throw new IllegalArgumentException(
                        "Responsável não encontrado para o ID informado: " + item.getResponsavelId());
            }
        }

        List<Criterio> criteriosAtivos = criterioDAO.listarTodos();
        CriterioService.ResultadoPontuacao resultadoPontuacao = criterioService.avaliarCriterios(aluno, criteriosAtivos);
        BigDecimal pontuacaoTotal = resultadoPontuacao.getPontuacaoTotal();
        String protocolo = "SIVE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                alunoDAO.inserir(aluno, conn);

                for (int i = 0; i < responsaveis.size(); i++) {
                    CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem item = responsaveis.get(i);
                    Parentesco parentesco = Parentesco.fromString(item.getParentesco());
                    if (parentesco == null) {
                        parentesco = Parentesco.OUTRO;
                    }
                    AlunoResponsavel vinculo = new AlunoResponsavel(
                            aluno.getId(),
                            item.getResponsavelId(),
                            parentesco,
                            i == 0);
                    alunoResponsavelDAO.vincular(vinculo, conn);
                }

                UUID inscricaoId = alunoDAO.inserirInscricaoFila(
                        conn,
                        escolaId,
                        aluno.getId(),
                        tipoVaga,
                        serieAno,
                        turno,
                        protocolo,
                        pontuacaoTotal);

                inscricaoCriterioDAO.substituirPorInscricao(conn, inscricaoId, resultadoPontuacao.getItens());

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        return new AlunoResumo(
                aluno.getId(),
                aluno.getNome(),
                aluno.getCpf(),
                aluno.getDataNascimento(),
                aluno.getEndereco(),
                aluno.getRendaPerCapita(),
                aluno.isTemNecessidadeEspecial(),
                serieAno,
                tipoVaga,
                turno,
                pontuacaoTotal,
                "EM_ESPERA");
    }

    public Aluno buscarPorId(UUID id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("O ID do aluno não pode ser nulo.");
        }
        return alunoDAO.buscarPorId(id);
    }

    public List<Aluno> listarTodos() throws SQLException {
        return alunoDAO.listar();
    }

    public List<AlunoResumo> listarPorEscola(UUID escolaId, String busca, String serieAno) throws SQLException {
        if (escolaId == null) {
            throw new IllegalArgumentException("O ID da escola não pode ser nulo.");
        }
        return alunoDAO.listarPorEscola(escolaId, busca, serieAno);
    }

    public AlunoDetalhe buscarDetalhePorEscola(UUID escolaId, UUID alunoId) throws SQLException {
        if (escolaId == null || alunoId == null) {
            throw new IllegalArgumentException("Os identificadores da escola e do aluno são obrigatórios.");
        }
        AlunoDetalhe detalhe = alunoDAO.buscarDetalhePorEscola(escolaId, alunoId);
        if (detalhe == null) {
            throw new IllegalArgumentException("Aluno não encontrado para esta escola.");
        }
        return detalhe;
    }

    public AlunoDetalhe atualizarParaEscola(
            UUID escolaId,
            UUID alunoId,
            CadastrarAlunoInscricaoRequest request) throws SQLException {
        if (escolaId == null || alunoId == null) {
            throw new IllegalArgumentException("Os identificadores da escola e do aluno são obrigatórios.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Os dados do aluno não podem ser nulos.");
        }

        buscarDetalhePorEscola(escolaId, alunoId);

        String tipoVaga = normalizarTipoVaga(request.getTipoVaga());
        if (request.getSerieAno() == null || request.getSerieAno().trim().isEmpty()) {
            throw new IllegalArgumentException("O nível escolar pretendido é obrigatório.");
        }
        String serieAno = request.getSerieAno().trim();
        String turno = normalizarTurno(request.getTurno());
        Endereco enderecoNormalizado = validarENormalizarEndereco(request.getEndereco());

        BigDecimal rendaPerCapita = request.getRendaPerCapita() != null
                ? request.getRendaPerCapita()
                : BigDecimal.ZERO;
        if (rendaPerCapita.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A renda familiar per capita não pode ser negativa.");
        }

        List<CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem> responsaveis = request.getResponsaveis();
        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new IllegalArgumentException("Vincule pelo menos um responsável ao aluno.");
        }

        String nome = request.getNome() != null ? request.getNome().trim() : "";
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
        if (request.getDataNascimento() == null || request.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento do aluno é inválida.");
        }

        String cpfRaw = request.getCpf() != null ? request.getCpf().trim() : "";
        boolean atualizarCpf = !cpfRaw.isEmpty() && !cpfRaw.contains("*");
        if (atualizarCpf) {
            String cpfDigits = CpfCryptoService.normalizarDigitos(cpfRaw);
            if (cpfDigits.length() != 11) {
                throw new IllegalArgumentException("Informe um CPF válido com 11 dígitos para o aluno.");
            }
            Aluno existenteCpf = alunoDAO.buscarPorCpf(cpfRaw);
            if (existenteCpf != null && !alunoId.equals(existenteCpf.getId())) {
                throw new IllegalArgumentException("Já existe outro aluno cadastrado com o CPF informado.");
            }
        }

        for (CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem item : responsaveis) {
            if (item == null || item.getResponsavelId() == null) {
                throw new IllegalArgumentException("Todos os responsáveis vinculados devem possuir um ID válido.");
            }
            if (responsavelDAO.buscarPorId(item.getResponsavelId()) == null) {
                throw new IllegalArgumentException(
                        "Responsável não encontrado para o ID informado: " + item.getResponsavelId());
            }
        }

        Aluno aluno = new Aluno();
        aluno.setId(alunoId);
        aluno.setNome(nome);
        aluno.setCpf(cpfRaw);
        aluno.setDataNascimento(request.getDataNascimento());
        aluno.setEndereco(enderecoNormalizado);
        aluno.setRendaPerCapita(rendaPerCapita);
        aluno.setTemNecessidadeEspecial(request.isTemNecessidadeEspecial());
        aluno.setVulnerabilidadeSocioeconomica(request.isVulnerabilidadeSocioeconomica());
        aluno.setResponsavelSolo(request.isResponsavelSolo());
        aluno.setMoraDeAluguel(request.isMoraDeAluguel());
        aluno.setBolsaFamilia(request.isBolsaFamilia());
        aluno.setBpcLoas(request.isBpcLoas());
        aluno.setIrmaoMatriculado(request.isIrmaoMatriculado());

        List<Criterio> criteriosAtivos = criterioDAO.listarTodos();
        CriterioService.ResultadoPontuacao resultadoPontuacao = criterioService.avaliarCriterios(aluno, criteriosAtivos);
        BigDecimal pontuacaoTotal = resultadoPontuacao.getPontuacaoTotal();

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try {
                alunoDAO.atualizar(aluno, atualizarCpf, conn);
                alunoDAO.removerResponsaveis(alunoId, conn);

                for (int i = 0; i < responsaveis.size(); i++) {
                    CadastrarAlunoInscricaoRequest.ResponsavelVinculoItem item = responsaveis.get(i);
                    Parentesco parentesco = Parentesco.fromString(item.getParentesco());
                    if (parentesco == null) {
                        parentesco = Parentesco.OUTRO;
                    }
                    AlunoResponsavel vinculo = new AlunoResponsavel(
                            alunoId,
                            item.getResponsavelId(),
                            parentesco,
                            i == 0);
                    alunoResponsavelDAO.vincular(vinculo, conn);
                }

                UUID inscricaoId = alunoDAO.atualizarInscricaoEscola(
                        conn,
                        escolaId,
                        alunoId,
                        tipoVaga,
                        serieAno,
                        turno,
                        pontuacaoTotal);

                if (inscricaoId != null) {
                    inscricaoCriterioDAO.substituirPorInscricao(conn, inscricaoId, resultadoPontuacao.getItens());
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        return buscarDetalhePorEscola(escolaId, alunoId);
    }

    public List<AlunoResponsavel> listarResponsaveis(UUID alunoId) throws SQLException {
        if (alunoId == null) {
            throw new IllegalArgumentException("O ID do aluno não pode ser nulo.");
        }
        if (alunoDAO.buscarPorId(alunoId) == null) {
            throw new IllegalArgumentException("Aluno não encontrado para o ID informado: " + alunoId);
        }
        return alunoResponsavelDAO.listarPorAluno(alunoId);
    }

    private void validar(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Os dados do aluno não podem ser nulos.");
        }
        if (aluno.getNome() == null || aluno.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do aluno é obrigatório.");
        }
        String cpfDigits = CpfCryptoService.normalizarDigitos(aluno.getCpf());
        if (cpfDigits.length() != 11) {
            throw new IllegalArgumentException("Informe um CPF válido com 11 dígitos para o aluno.");
        }
        if (aluno.getDataNascimento() == null) {
            throw new IllegalArgumentException("A data de nascimento do aluno é obrigatória.");
        }
        if (aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode ser uma data futura.");
        }
    }

    private String normalizarTipoVaga(String tipoVaga) {
        if (tipoVaga == null) {
            throw new IllegalArgumentException("O tipo de vaga é obrigatório.");
        }
        String valor = tipoVaga.trim().toUpperCase();
        if (!"CRECHE".equals(valor) && !"FUNDAMENTAL".equals(valor)) {
            throw new IllegalArgumentException("O tipo de vaga deve ser CRECHE ou FUNDAMENTAL.");
        }
        return valor;
    }

    private String normalizarTurno(String turno) {
        if (turno == null) {
            throw new IllegalArgumentException("O período solicitado é obrigatório.");
        }
        String valor = turno.trim().toUpperCase();
        if (!"INTEGRAL".equals(valor) && !"MANHA".equals(valor) && !"TARDE".equals(valor)) {
            throw new IllegalArgumentException("O período solicitado deve ser INTEGRAL, MANHA ou TARDE.");
        }
        return valor;
    }

    private Endereco validarENormalizarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("O endereço de residência é obrigatório.");
        }
        String cepDigits = endereco.getCep() != null ? endereco.getCep().replaceAll("\\D", "") : "";
        if (cepDigits.length() != 8) {
            throw new IllegalArgumentException("Informe um CEP válido com 8 dígitos.");
        }
        if (endereco.getRua() == null || endereco.getRua().trim().isEmpty()) {
            throw new IllegalArgumentException("A rua do endereço é obrigatória.");
        }
        if (endereco.getNumero() == null || endereco.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("O número do endereço é obrigatório.");
        }
        if (endereco.getBairro() == null || endereco.getBairro().trim().isEmpty()) {
            throw new IllegalArgumentException("O bairro do endereço é obrigatório.");
        }
        if (endereco.getCidade() == null || endereco.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException("A cidade do endereço é obrigatória.");
        }
        String uf = endereco.getEstado() != null ? endereco.getEstado().trim().toUpperCase() : "";
        if (!uf.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Informe uma UF válida com 2 letras.");
        }

        String complemento = endereco.getComplemento() != null && !endereco.getComplemento().trim().isEmpty()
                ? endereco.getComplemento().trim()
                : null;
        String cepFormatado = cepDigits.substring(0, 5) + "-" + cepDigits.substring(5, 8);

        return new Endereco(
                cepFormatado,
                endereco.getRua().trim(),
                endereco.getNumero().trim(),
                complemento,
                endereco.getBairro().trim(),
                endereco.getCidade().trim(),
                uf);
    }
}
