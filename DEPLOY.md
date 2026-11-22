# Guia de Deploy no Fly.io (macOS) — SIVE Backend

Este guia contém as instruções passo a passo para você publicar o backend do **SIVE** e o banco de dados **PostgreSQL** no [Fly.io](https://fly.io) diretamente do seu Mac em casa.

---

## 📋 Pré-requisitos no macOS

1. Ter o **Homebrew** instalado (gerenciador de pacotes padrão do macOS).
2. Ter uma conta criada no [fly.io](https://fly.io).

---

## 🚀 Passo a Passo de Instalação e Deploy

### 1. Instalar a CLI do Fly.io (`flyctl`)
Abra o **Terminal** do seu Mac e execute via Homebrew:

```bash
brew install flyctl
```

*(Ou se preferir via curl oficial: `curl -L https://fly.io/install.sh | sh`)*

---

### 2. Autenticar no Fly.io
No Terminal, execute:

```bash
fly auth login
```
> O comando abrirá automaticamente o navegador padrão do macOS para você confirmar o login na sua conta.

---

### 3. Criar o Banco de Dados PostgreSQL no Fly.io
Crie a máquina do banco de dados na região de São Paulo (`gru`), com tamanho mínimo (econômico para trabalho acadêmico):

```bash
fly postgres create \
  --name sive-postgres \
  --region gru \
  --initial-cluster-size 1 \
  --vm-size shared-cpu-1x \
  --volume-size 1
```

> **Dica:** O terminal exibirá o usuário e senha gerados. O Fly vai conectar tudo automaticamente no passo 5, mas você pode anotar se desejar.

Em seguida, execute estes comandos para aumentar a memória para 512MB (evitando que o Postgres caia por falta de memória) e desativar o auto-stop do banco para **garantir que ele fique sempre ligado**:

```bash
fly scale memory 512 -a sive-postgres
fly machine update $(fly machines list -a sive-postgres -q) -a sive-postgres --autostop=false
```

---

### 4. Configurar a Aplicação Backend
Navegue até a pasta `backend/` do projeto (onde estão o `Dockerfile` e o `fly.toml` pré-configurados):

```bash
cd caminho/para/o/projeto/sive/backend
```

Execute o comando para vincular a configuração existente:

```bash
fly launch --copy-config
```

- Quando perguntar se deseja fazer alterações nas configurações (*"Do you want to tweak these settings before proceeding?"*), digite **`N`** (Não).
- Quando perguntar se deseja fazer o deploy imediatamente (*"Would you like to deploy now?"*), responda **`N`** (Não), pois precisamos antes conectar o banco de dados no passo seguinte.

> **Nota sobre máquinas:** Por padrão, o `fly launch` provisiona **2 máquinas** para o backend para alta disponibilidade (redundância). Para economizar recursos no trabalho acadêmico e manter apenas **1 máquina**, execute:
> ```bash
> fly scale count 1 -a sive-backend
> ```

---

### 5. Conectar o Banco de Dados ao Backend
Vincule o cluster do Postgres à aplicação. Isso injeta automaticamente a variável de ambiente `DATABASE_URL` no seu backend:

```bash
fly postgres attach sive-postgres -a sive-backend
```

---

### 6. Executar o Script DDL no Banco de Dados
Crie as 8 tabelas e dados iniciais (seeds) no banco remoto executando o script `schema.sql`:

```bash
fly postgres connect -a sive-postgres < database/schema.sql
fly postgres connect -a sive-postgres < database/seeds/01_criterios.sql
fly postgres connect -a sive-postgres < database/seeds/02_usuario_escola.sql
```

---

### 7. Publicar a Aplicação (Deploy)
Agora dispare o deploy do backend:

```bash
fly deploy
```

O Fly.io construirá a imagem Docker na nuvem, iniciará os serviços e disponibilizará a URL pública com HTTPS (exemplo: `https://sive-backend.fly.dev`).

---

### 8. Testar a Aplicação (Health Check)
Você pode testar a integridade da aplicação e a conectividade com o banco PostgreSQL chamando a rota de verificação:

```bash
curl https://sive-backend.fly.dev/health-check
```

*(Ou localmente se estiver rodando via `./mvnw spring-boot:run`: `curl http://localhost:8080/health-check`)*

Exemplo de retorno esperado:
```json
{
  "status": "UP",
  "app": "sive-backend",
  "timestamp": "2026-11-20T...",
  "database": {
    "status": "UP",
    "mensagem": "Conexão com PostgreSQL ativa",
    "catalogo": "sive_backend"
  }
}
```

---

## 🔄 Fluxo de Atualização Diária (Sempre que alterar o código)

Sempre que você fizer alterações em classes Java, controladores, regras de negócio ou adicionar novas dependências, o fluxo para publicar a nova versão se resume a:

```bash
cd backend
fly deploy
```

> 💡 **Dica de Ouro:** Antes de rodar o `fly deploy`, você pode testar a compilação localmente em 2 segundos com:
> ```bash
> mvn test-compile
> ```
> Isso garante que o código não tem erros de sintaxe antes de enviar para o build na nuvem!

*(Caso você tenha alterado ou criado novas tabelas no `database/schema.sql`, aplique as alterações no banco com: `fly postgres connect -a sive-postgres < database/schema.sql`).*

---

## 🤖 Deploy Automático com GitHub Actions (CI/CD)

Criamos a automação no arquivo [deploy.yml](file:///Users/gisellehoekveldsilva/Development/www/sive/backend/.github/workflows/deploy.yml). Sempre que você fizer `git push` ou aprovar um **merge para a branch `main`**, o GitHub Actions compila, testa e faz o deploy automático para o Fly.io.

### Como ativar em 2 minutos:

1. **Gere o token de deploy do Fly.io no seu terminal:**
   ```bash
   fly tokens create deploy -x 999999h
   ```
   *(Copie o token longo que começará com `FlyV1...`)*.

2. **Adicione o token como Secret no GitHub:**
   - Acesse o repositório no GitHub: `Sistema-Integrado-de-Vagas-Escolares/backend`
   - Vá em **Settings** > **Secrets and variables** > **Actions**.
   - Clique em **New repository secret**.
   - **Name:** `FLY_API_TOKEN`
   - **Secret:** cole o token copiado no passo 1.
   - Clique em **Add secret**.

Pronto! A partir de agora, qualquer commit ou merge na `main` dispara o deploy automaticamente na nuvem sem precisar abrir o terminal.

---

## 🔍 Comandos Úteis para Monitorar e Controlar Custos

### Ver status e URL da aplicação:
```bash
fly status
```

### Visualizar logs do backend em tempo real:
```bash
fly logs
```

### Como economizar e não gastar nada:
1. **Desligamento Automático:** Nosso arquivo `fly.toml` já está configurado com `auto_stop_machines = "stop"`. Quando você não estiver enviando requisições, a máquina desliga sozinha em poucos minutos.
2. **Desligar manualmente quando não for testar:**
   - Acesse o painel web em [fly.io/dashboard](https://fly.io/dashboard).
   - Entre em `sive-backend` > **Machines** > clique nos três pontinhos `...` > **Stop**.
   - Faça o mesmo em `sive-postgres` > **Machines** > **Stop**.
3. **Destruir após a nota da faculdade (limpeza total):**
   ```bash
   fly apps destroy sive-backend -y
   fly apps destroy sive-postgres -y
   ```
