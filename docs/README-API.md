# Nucleo — Mitos e Verdades Nucleares

API Java do Challenge FIAP / AMAZUL, turma **2TDSPR**. A solucao propoe um jogo educativo de mito ou verdade sobre tecnologia nuclear, com explicacoes e referencias. Nesta Sprint 1, a API e responsavel pelas perguntas.

## Equipe

| Integrante | RM |
| --- | --- |
| Pedro Gonçalves Rodrigues | 567651 |
| Davi Isac Colin Pereira | 567265 |
| Hugo Souza de Jesus | 568542 |
| Matheus Peres Moraes de Jesus | 567300 |
| Cassio Ribeiro | 567295 |

## Escopo Java da Sprint 1

A unica entidade JPA desta versao e **Pergunta**, com enunciado, resposta V/F, explicacao e referencia. Ela oferece CRUD completo, busca por texto, sorteio e conferencia da resposta. O escopo segue as paginas 35–38 do documento oficial e a secao Java do plano de sprint atualizado.

Usuarios, historico persistido, ranking e simulacoes da cidade nao fazem parte desta entrega Java. As outras entidades do modelo geral de Banco sao responsabilidades que precisam ser coordenadas na entrega daquela disciplina.

## Tecnologias e estrutura

Java 25, Spring Boot 4.1.1, Maven Wrapper, Spring Web MVC, Spring Data JPA, Spring Validation, H2, Oracle JDBC e Swagger/OpenAPI com springdoc 3.1.1.

```text
controller -> service -> repository -> banco
    DTOs      regras       JPA       H2/Oracle
```

Os controllers recebem requisicoes, os DTOs validam entradas, o service aplica regras, o repository persiste e consulta, e o tratamento global padroniza erros. A injecao de dependencias usa construtores.

## Como executar

Pre-requisito: **JDK 25**. O Maven Wrapper baixa suas dependencias na primeira execucao.

Neste computador Windows:

```powershell
# H2 em arquivo, sem configurar Oracle
.\scripts\executar-local.ps1

# Oracle configurado pelo .env
.\scripts\executar-local.ps1 -Oracle
```

O iniciador localiza o JDK do IntelliJ quando JAVA_HOME nao esta definido, resolve o PATH do PowerShell e aplica compatibilidade de rede ao runtime JBR deste Windows. As variaveis do computador nao sao alteradas permanentemente.

Em um ambiente com JAVA_HOME e PowerShell configurados:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=oracle'
```

Linux/macOS: `sh mvnw spring-boot:run`, usando JDK 25.

Quando o console informar que o servidor iniciou, acesse:

- **Indice JSON:** <http://localhost:8080/>
- **Swagger UI:** <http://localhost:8080/swagger-ui>
- Endereco completo: <http://localhost:8080/swagger-ui/index.html>
- Atalho Swagger: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

A raiz retorna um indice JSON com os caminhos da API; nao existe frontend. O Swagger precisa do servidor em execucao. Se nao abrir, confira a inicializacao no console e se a porta 8080 esta disponivel.

## Banco de dados

No perfil padrao `local`, o H2 grava em `data/challenge.mv.db` e preserva registros apos reiniciar. O acervo comeca vazio. Console H2 local: `/h2-console`, usuario `sa`, senha vazia, JDBC URL `jdbc:h2:file:./data/challenge;DB_CLOSE_ON_EXIT=FALSE;WRITE_DELAY=0`.

O perfil `oracle` usa `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, carregados do `.env` local ou do ambiente. O arquivo `.env.example` contem somente placeholders. Nao publique `.env` no repositorio.

Scripts para um schema novo:

1. `database/01-pergunta.sql`: tabela PERGUNTA, constraints e sequence `seq_pergunta`.
2. `database/02-pergunta-procedures.sql`: INSERT, UPDATE e DELETE de perguntas com tratamento de excecoes.

Execute o DDL uma vez; ele nao deve sobrescrever objetos existentes. `ddl-auto=validate` no Oracle valida a tabela mapeada, sem criar ou remover objetos automaticamente.

O CRUD usa Spring Data JPA no perfil local. No Oracle, as consultas usam JPA e as escritas chamam as procedures da integracao existente. As procedures nao fazem COMMIT proprio; a transacao e controlada pela API.

### Modelo de Pergunta

| Campo | Regra |
| --- | --- |
| id_pergunta | PK NUMBER(10), gerada por seq_pergunta |
| enunciado | Obrigatorio, ate 500 caracteres |
| resposta | Obrigatoria: V para verdade, F para mito |
| explicacao | Obrigatoria, ate 1000 caracteres |
| referencia | Opcional, ate 300 caracteres |

O acervo educativo deve trazer fontes rastreaveis. A referencia permanece opcional no banco para respeitar o modelo recebido. A [pesquisa de apoio](docs/pesquisa.md) reune fontes oficiais e cuidados com o contexto das afirmacoes.

## Endpoints

A API disponibiliza **8 endpoints documentados no Swagger**, considerando cada combinacao de metodo HTTP e caminho como uma operacao:

- **5 endpoints para o CRUD de Pergunta:** cadastrar, listar, consultar por ID, atualizar e excluir.
- **2 endpoints para o jogo Mito ou Verdade:** sortear um cartao e conferir a resposta do jogador com explicacao e referencia.
- **1 endpoint de indice:** apresentar a API e organizar os caminhos disponiveis em JSON.

A busca por texto, a paginacao e a ordenacao sao parametros de `GET /perguntas`; por isso, nao sao contados como endpoints adicionais.

Utilizamos **Swagger/OpenAPI** para descrever e testar essas operacoes, incluindo parametros, corpos JSON e codigos de resposta. A interface pode ser aberta em **http://localhost:8080/swagger-ui** com a API em execucao. Esse atalho redireciona para `/swagger-ui/index.html`; as rotas da documentacao nao entram na contagem dos oito endpoints da aplicacao.

| Metodo | Caminho | Funcao |
| --- | --- | --- |
| GET | / | Indice JSON e links da documentacao |
| POST | /perguntas | Cadastrar; retorna 201 e Location |
| GET | /perguntas | Listar com texto, page, size e sort |
| GET | /perguntas/{id} | Consultar pergunta |
| PUT | /perguntas/{id} | Atualizar campos editaveis |
| DELETE | /perguntas/{id} | Excluir; retorna 204 |
| GET | /jogo/perguntas/aleatoria | Sortear ID e enunciado, sem gabarito |
| POST | /jogo/perguntas/{id}/respostas | Conferir V/F e retornar explicacao/referencia |

Cadastro de exemplo:

```json
{"enunciado":"A tecnologia nuclear possui aplicacoes na medicina.","resposta":"V","explicacao":"O kickoff apresenta medicina nuclear e producao de radiofarmacos.","referencia":"AMAZUL - Kick-off FIAP - 13OUT26.pdf, p. 34"}
```

Resposta do jogador:

```json
{"resposta":"V"}
```

Buscas: `/perguntas?texto=medicina&page=0&size=10&sort=id,desc`. A pesquisa usa `findByEnunciadoContainingIgnoreCase`, sem diferenciar maiusculas e minusculas. A API retorna 400 para dados invalidos e 404 para pergunta inexistente ou sorteio sem acervo. Sorteios independentes podem repetir perguntas e continuam funcionando com lacunas nos IDs.

A conferencia de resposta nao grava historico nem concede pontos. O CRUD administrativo expõe o gabarito e ainda nao possui autenticacao; o DTO do sorteio apenas evita enviar a resposta antecipadamente no fluxo do jogo.

## Dados e testes

Com a API em execucao:

```powershell
.\scripts\carregar-perguntas.ps1
```

O carregador insere dez perguntas pela API; cada execucao cria novos registros. O JSON de origem esta em `dados/perguntas-exemplo.json`. A colecao `postman/Challenge-Java.postman_collection.json` demonstra cadastro, consulta, atualizacao, exclusao, sorteio, feedback e validacao.

```powershell
# Testes H2 e geracao do JAR
.\scripts\executar-local.ps1 -Testes

# H2 + verificacao no Oracle real
.\scripts\executar-local.ps1 -Testes -Oracle

# Formatacao uniforme de quatro espacos
.\scripts\formatar-java.ps1
```

O perfil Maven `oracle-check` e opt-in. Os testes comuns nao acessam Oracle; os testes Oracle criam e removem somente seus proprios registros. O [relatorio de verificacao](docs/verificacao-java.md) registra os resultados e a matriz de requisitos.

## Entrega

O PDF oficial pede **link publico do repositorio** com codigo, README, .gitignore, configuracoes, Swagger e dados de teste. O repositorio ainda precisa ser criado/publicado. Nao publique credenciais, logs, `data/` ou `target/`.

O plano atualizado define como prazo interno de Java **29/10/2026**, com a API rodando e o Swagger acessivel. A data oficial de submissao deve ser confirmada no portal/professor, pois os materiais divergem entre 01/11 e 02/11.
