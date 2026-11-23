package com.sive.strategy;

import com.sive.dao.CriterioDAO;
import com.sive.model.Aluno;
import com.sive.model.Criterio;
import com.sive.service.CriterioService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriterioPontuacaoTest {

    @Test
    void deveCalcularPontuacaoDetalhadaPorCriterioAtivo() {
        CriterioService service = new CriterioService(new CriterioDAO());

        Criterio renda = criarCriterio("RENDA", 25.0, true);
        Criterio pcd = criarCriterio("PCD", 20.0, true);
        Criterio irmao = criarCriterio("IRMAO", 15.0, true);
        Criterio inativo = criarCriterio("MORA_ALUGUEL", 4.0, false);

        Aluno aluno = new Aluno();
        aluno.setRendaPerCapita(new BigDecimal("1100.00")); // 60% de 25.00 = 15.00
        aluno.setTemNecessidadeEspecial(true);              // 20.00
        aluno.setIrmaoMatriculado(false);                   // 0.00
        aluno.setMoraDeAluguel(true);                       // critério inativo -> ignorado

        CriterioService.ResultadoPontuacao resultado = service.avaliarCriterios(
                aluno,
                List.of(renda, pcd, irmao, inativo));

        assertEquals(new BigDecimal("35.00"), resultado.getPontuacaoTotal());
        assertEquals(2, resultado.getItens().size());
        assertEquals(15.0, resultado.getItens().get(0).getPontosObtidos());
        assertEquals(20.0, resultado.getItens().get(1).getPontosObtidos());
    }

    private Criterio criarCriterio(String codigo, double pontosBase, boolean ativo) {
        Criterio c = new Criterio(codigo, pontosBase, "SOCIAL");
        c.setId(UUID.randomUUID());
        c.setCodigo(codigo);
        c.setAtivo(ativo);
        return c;
    }
}
