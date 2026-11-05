---
name: sive-backend
description: >-
  Diretrizes de arquitetura, padrão Strategy, restrições de bibliotecas, procedimentos
  de verificação de sintaxe e compilação, e políticas estritas de Git para o backend Java do SIVE.
  Ative esta skill ao desenvolver, refatorar, testar ou implementar qualquer funcionalidade no backend.
---

# Diretrizes de Desenvolvimento — SIVE Backend

Este documento estabelece as regras de arquitetura, padrões de projeto, convenções de código, procedimentos de checagem de integridade e políticas de controle de versão para o backend do **SIVE (Sistema Integrado de Vagas Escolares)**.

---

## 1. Stack Tecnológica e Restrições de Bibliotecas

Para garantir a clareza didática e o alinhamento com os objetivos acadêmicos de Orientação a Objetos e Banco de Dados, a stack do backend possui regras rígidas sobre quais bibliotecas são permitidas e quais são estritamente proibidas.

### ✅ Bibliotecas Permitidas e Adotadas
- **Java:** Versão 17 LTS.
- **Spring Boot 3.3+:** Utilizado **exclusivamente** para subir o servidor HTTP embutido e expor controladores REST (`spring-boot-starter-web` com `@RestController`, `@GetMapping`, `@PostMapping`, etc.).
- **Driver Oficial PostgreSQL:** `org.postgresql:postgresql` (v42.7.4+).
- **Persistência de Dados:** **JDBC Nativo (`java.sql.*`)** clássico via `DriverManager`, `Connection`, `PreparedStatement` e `ResultSet`.
- **Testes:** JUnit 5 (`spring-boot-starter-test`).

### ❌ Bibliotecas Proibidas (NÃO UTILIZAR)
- **NÃO usar Lombok:** É estritamente proibido o uso de `@Data`, `@Getter`, `@Setter`, `@AllArgsConstructor` ou qualquer outra anotação do Lombok. Toda classe de modelo deve ter atributos privados, construtores explícitos, métodos getters e setters tradicionais e método `toString()` escritos manualmente.
- **NÃO usar Spring Data JPA / Hibernate:** Não utilizar `JpaRepository`, `@Entity`, `@Table`, `@Id` ou qualquer abstração de ORM. Todo o acesso a dados deve ser manual e transparente na camada `dao`.
- **NÃO usar Bancos em Memória (H2):** O banco oficial é o PostgreSQL 16 rodando no container Docker (`backend/docker-compose.yml`).

---

## 2. Diretrizes do Padrão de Projeto Strategy

O cálculo da pontuação de cada inscrição na fila de espera depende de múltiplos critérios (socioeconômicos, geográficos, prioridades legais). Toda lógica de cálculo deve seguir rigorosamente o padrão **GOF Strategy**.

### Estrutura de Pacotes: `com.sive.strategy`

1. **Interface Strategy (`CriterioPontuacaoStrategy`):**
   ```java
   package com.sive.strategy;

   import com.sive.model.Inscricao;

   public interface CriterioPontuacaoStrategy {
       double calcularPontos(Inscricao inscricao);
       String getNomeCriterio();
   }
   ```

2. **Implementações Concretas:**
   Cada critério deve ser uma classe independente que implementa a interface:
   - `CriterioRendaStrategy`: calcula pontos com base na renda per capita da família do aluno.
   - `CriterioProximidadeStrategy`: calcula pontos pela proximidade física entre a residência e a escola.
   - `CriterioIrmaoStrategy`: concede bonificação caso o aluno possua irmão matriculado na mesma escola.
   - `CriterioPcdStrategy`: pontua a prioridade legal para alunos com necessidades especiais/deficiência.

3. **Factory com Switch Expressions (`CriterioStrategyFactory`):**
   Uma fábrica centralizada para instanciar a estratégia correta a partir do identificador ou nome do critério:
   ```java
   package com.sive.strategy;

   public final class CriterioStrategyFactory {
       private CriterioStrategyFactory() {}

       public static CriterioPontuacaoStrategy obterEstrategia(String codigoCriterio) {
           return switch (codigoCriterio.toUpperCase()) {
               case "RENDA" -> new CriterioRendaStrategy();
               case "PROXIMIDADE" -> new CriterioProximidadeStrategy();
               case "IRMAO" -> new CriterioIrmaoStrategy();
               case "PCD" -> new CriterioPcdStrategy();
               default -> throw new IllegalArgumentException("Critério desconhecido: " + codigoCriterio);
           };
       }
   }
   ```

4. **Orquestração na Camada Service:**
   O serviço responsável pelo cálculo da fila deve iterar sobre a lista de critérios ativos, instanciando cada estratégia através da factory e acumulando os pontos polimorficamente via `strategy.calcularPontos(inscricao)`.

---

## 3. Procedimento Obrigatório de Verificação de Bugs e Sintaxe

Após **qualquer** modificação, criação de arquivo ou refatoração no código do backend, o agente deve obrigatoriamente executar os seguintes passos de validação antes de dar a resposta como concluída:

### Checklist Pós-Implementação:
1. **Compilação e Verificação de Sintaxe:**
   Executar o comando Maven para garantir que não há erros de tipagem, imports ausentes ou incompatibilidades de sintaxe:
   ```bash
   cd backend && mvn test-compile
   ```
2. **Execução de Testes Automatizados:**
   Se existirem testes unitários ou de integração implementados no projeto:
   ```bash
   cd backend && mvn test
   ```
3. **Validação de Práticas JDBC:**
   - Garantir que toda abertura de `Connection`, `PreparedStatement` e `ResultSet` utilize o padrão **`try-with-resources`** para evitar vazamento de conexões (connection leak).
   - Verificar se as exceções `SQLException` são capturadas ou propagadas adequadamente na assinatura dos métodos.
   - Garantir que transações que alteram múltiplos registros utilizem `conn.setAutoCommit(false)`, `conn.commit()` e `conn.rollback()` no bloco `catch`.
4. **Regra de Término de Turno:**
   O agente nunca deve concluir uma resposta dizendo que a tarefa foi finalizada se a compilação do Maven falhar com erros (`BUILD FAILURE`). Os erros devem ser corrigidos imediatamente.

---

## 4. Política Estrita de Governança de Git (Restrição do Agente)

Para garantir o controle absoluto e a segurança do repositório por parte do desenvolvedor humano, o agente opera sob a seguinte restrição irrevogável:

### 🚫 Proibições Absolutas:
- **O agente NUNCA deve executar `git commit`**.
- **O agente NUNCA deve executar `git push`** para qualquer repositório remoto.
- **O agente NUNCA deve alterar remotes ou credenciais de Git**.
- **O agente NUNCA deve publicar branches remotas**.

### 🔍 Permissões Permitidas (Apenas Consulta e Inspeção):
O agente possui autorização apenas para comandos de leitura e consulta local/remota quando fizer sentido para diagnóstico:
- `git status` (verificar arquivos modificados ou untracked).
- `git diff` (inspecionar alterações feitas nos arquivos locais).
- `git log` (consultar histórico recente de commits).
- `git branch -a` ou `git remote -v` (consultar nomes de branches e remotos configurados).
- `git fetch` (apenas para verificar se há novidades no remoto, sem mesclar nem fazer checkout destrutivo).

> **Aviso:** A decisão de realizar commits e enviar códigos para o GitHub é **exclusiva do usuário**. O agente deve limitar-se a sugerir a mensagem de commit e os comandos para que o usuário os execute, se assim desejar.

