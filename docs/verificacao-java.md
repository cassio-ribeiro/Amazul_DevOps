# Verificacao final — Nucleo / Java Advanced

Revisao de **26/10/2026**. Escopo consolidado: **somente a entidade JPA Pergunta**, conforme as paginas 35–38 do PDF oficial e a secao Java do plano atualizado `Challenge 2026 - Plano de Sprint 1 (Mitos e Verdades Nucleares) (1).pdf`.

## Resultado

**BUILD SUCCESS. A suite HTTP/H2 atual passou com 14 testes, zero falhas, zero erros e zero ignorados. Os tres testes Oracle passaram na revisao integrada anterior; esta atualizacao altera somente o atalho web.** A verificacao individual da API em execucao tambem passou em **20 verificacoes HTTP**, detalhadas em [verificacao-endpoints.md](verificacao-endpoints.md).

| Suite | Testes | Resultado |
| --- | --- | --- |
| ChallengeApiTest, H2 | 11 | PASS |
| HomeApiTest, H2 | 3 | PASS |
| OracleApiIT, Oracle real | 3 | PASS |

O build foi executado com `clean verify` para remover classes compiladas da evolucao anterior. Um teste de metamodelo JPA confirma que a unica entidade ativa e Pergunta. Nao ha endpoints de usuarios, ranking ou simulacoes nesta versao.

## Aderencia ao Java da Sprint 1

| Exigencia | Evidencia | Situacao |
| --- | --- | --- |
| API REST Java + Spring educativa | Controllers, Pergunta e fluxo mito/verdade com explicacao/referencia | Atendido |
| CRUD de pelo menos uma entidade com Spring Data JPA | PerguntaRepository e PerguntaService; CRUD JPA no perfil local | Atendido |
| Busca textual por Query Method | findByEnunciadoContainingIgnoreCase | Atendido |
| Spring Validation | DTOs validados nos controllers | Atendido |
| Tratamento global de excecoes | GlobalExceptionHandler e ErroResponse | Atendido |
| Swagger: endpoints, parametros e respostas | /swagger-ui/index.html e /v3/api-docs, codigos/schemas testados | Atendido |
| Codigo, README, .gitignore, configuracoes e dados | Presentes; cinco integrantes identificados | Atendido |
| Link publico do repositorio | Repositorio ainda nao informado/criado | Pendente de publicacao |

O plano interno define Java sob responsabilidade de Pedro, com API e Swagger acessiveis ate **29/10/2026**. O PDF Java pede pelo menos uma entidade; as exigencias especificas de tres entidades da CP nao foram transferidas para esta entrega. As exigencias completas da disciplina de Banco permanecem separadas. Esta verificacao nao representa previsao de nota.

## Cobertura

- CRUD, Location, busca textual, curingas literais, paginacao, ordenacao e limite de pagina.
- Validacoes de campos, limites de tamanho, referencia opcional, JSON/ID invalidos e preservacao de dados apos PUT invalido.
- Sorteio com lacunas nos IDs e sem gabarito, acerto/erro e feedback.
- Contratos Swagger: parametros, respostas de sucesso/erro e schemas.
- Raiz GET / com indice JSON e sem Whitelabel; nenhuma pagina frontend foi criada.
- Oracle real: CRUD por procedures, leitura JPA, rollback controlado pela transacao Java e constraint V/F.

O teste individual criou e excluiu somente sua propria pergunta. Os IDs e o conteudo das **dez perguntas originais** foram comparados antes/depois e permaneceram iguais. O Swagger foi aberto no navegador e exibiu os grupos de administracao, jogo e indice. O JAR foi iniciado com Oracle na porta 8080.

## Reproduzir

```powershell
.\scripts\executar-local.ps1 -Testes
.\scripts\executar-local.ps1 -Testes -Oracle
.\scripts\executar-local.ps1 -Oracle
```

Depois da inicializacao, acesse http://localhost:8080/ e http://localhost:8080/swagger-ui. O primeiro endereco retorna JSON; o segundo abre a documentacao interativa.

O codigo Java foi formatado no estilo AOSP, com quatro espacos. Para manter o padrao: `scripts/formatar-java.ps1`.

## Entrega e evolucao

A evolucao com quatro entidades foi arquivada separadamente para retomada futura, sem fazer parte dos arquivos desta entrega. As estruturas adicionais ja criadas no Oracle foram preservadas; a API atual mapeia somente PERGUNTA.

Publicar o repositorio sem `.env`, dados locais, target ou logs e enviar o link publico. O pacote privado com credenciais e destinado somente ao compartilhamento autorizado, fora do repositorio publico.

## Atalho Swagger

GET `/swagger-ui` e GET `/swagger-ui/` retornam 302 para `/swagger-ui/index.html`. O teste automatizado confere ambos os caminhos; os enderecos anteriores `/swagger-ui.html` e `/swagger-ui/index.html` continuam disponiveis. O indice JSON aponta para o atalho.

## Organizacao dos endpoints

O OpenAPI da aplicacao em execucao confirma **8 operacoes REST**: 5 de CRUD de Pergunta, 2 do jogo e 1 de indice JSON. Cada combinacao de metodo e caminho conta como uma operacao. Busca, paginacao e ordenacao sao parametros da listagem; atalhos e arquivos do Swagger sao rotas de documentacao, fora dessa contagem. O acesso da interface e `http://localhost:8080/swagger-ui`.
