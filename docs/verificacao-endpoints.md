# Verificacao individual dos endpoints

Executada em 26/10/2026, com a API iniciada usando Oracle. Todas as verificacoes abaixo passaram. Foi criado e excluido somente um registro de teste; as 10 perguntas originais mantiveram seus IDs e conteudo.

| Verificacao | Metodo e caminho | HTTP | Resultado |
| --- | --- | --- | --- |
| Indice JSON sem Whitelabel | GET / | 200 | PASS |
| Cadastrar pergunta de teste | POST /perguntas | 201 | PASS |
| Listar com paginacao e ordenacao | GET /perguntas?page=0&size=10&sort=id,desc | 200 | PASS |
| Buscar enunciado por Query Method | GET /perguntas?texto={texto} | 200 | PASS |
| Consultar registro pelo ID | GET /perguntas/21 | 200 | PASS |
| Atualizar pergunta | PUT /perguntas/21 | 200 | PASS |
| Comprovar atualizacao persistida | GET /perguntas/21 | 200 | PASS |
| Sortear sem revelar gabarito | GET /jogo/perguntas/aleatoria | 200 | PASS |
| Conferir resposta correta | POST /jogo/perguntas/21/respostas | 200 | PASS |
| Conferir resposta incorreta | POST /jogo/perguntas/21/respostas | 200 | PASS |
| Rejeitar cadastro invalido | POST /perguntas | 400 | PASS |
| Rejeitar atualizacao invalida | PUT /perguntas/21 | 400 | PASS |
| Rejeitar resposta invalida | POST /jogo/perguntas/21/respostas | 400 | PASS |
| Rejeitar ID fora do formato | GET /perguntas/abc | 400 | PASS |
| Documento OpenAPI acessivel | GET /v3/api-docs | 200 | PASS |
| Configuracao do Swagger | GET /v3/api-docs/swagger-config | 200 | PASS |
| Swagger UI carrega | GET /swagger-ui/index.html | 200 | PASS |
| Atalho do Swagger carrega | GET /swagger-ui.html | 200 | PASS |
| Excluir somente o registro de teste | DELETE /perguntas/21 | 204 | PASS |
| Comprovar exclusao | GET /perguntas/21 | 404 | PASS |
