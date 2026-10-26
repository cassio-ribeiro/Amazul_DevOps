# Roteiro do vídeo - DevOps Sprint 1 (3 min)

Orientação do professor (Teams, 26/10): mostrar a solução funcionando **e** o código e a linha de raciocínio usados para construir.
Formato da CP2 (aulas 17/09 e 24/09): abrir o script, explicar a estrutura, **depois** executar. Nunca usar localhost; usuário da VM = RM; apagar o grupo de recursos no final.

> Dica para caber em 3 min: grave tudo e, na edição, acelere as esperas (az vm create, apt install, docker build) mostrando um texto como "2x" ou "acelerado". O que não pode faltar é o comando sendo digitado e o resultado aparecendo.

## Antes de gravar (fora do vídeo)

1. Repositório público no GitHub com `Dockerfile`, `.dockerignore`, `deploy-infra.sh`, `setup-app.sh`, `cleanup.sh`, `README.md` e a API.
2. Conferir que o `.env` **não** está no GitHub (`.gitignore` do Pedro já cobre).
3. Ensaiar uma vez inteira em um grupo de recursos de teste e apagar.
4. Separar o comando de POST com o JSON pronto num bloco de notas.

## Cena a cena

| Tempo | Cena | O que mostrar e falar |
|---|---|---|
| 0:00-0:15 | Abertura | Nome, RM, projeto Núcleo. Mostrar o diagrama de arquitetura: usuário → IP público → NSG → VM → Docker → container da API → Oracle FIAP. |
| 0:15-0:40 | Dockerfile | Abrir o `Dockerfile` e explicar: multi-stage (Maven compila, JRE executa), Java 25 do `pom.xml`, `USER appuser` (não-root), `EXPOSE 8080`, credenciais fora da imagem. |
| 0:40-1:05 | Azure CLI | Abrir o `deploy-infra.sh`, explicar as variáveis (RG, região, VM, imagem, size, RM). Rodar `./deploy-infra.sh`. Mostrar o IP público no final. |
| 1:05-1:45 | Container na VM | `ssh <RM>@<IP>` → `git clone ...` → `cd ...` → `cp .env.example .env` e `nano .env` (**não mostrar a senha**: corte ou desfoque) → abrir o `setup-app.sh` rapidamente → rodar `./setup-app.sh`. Mostrar `docker image ls`, `docker ps` e `docker logs` com "Started ChallengeApplication". |
| 1:45-2:30 | Testes pelo IP público | No terminal **da sua máquina** (não na VM): GET `/perguntas`, POST de uma pergunta, GET `/jogo/perguntas/aleatoria`. Abrir `http://<IP>:8080/swagger-ui` no navegador mostrando o IP na barra de endereço. |
| 2:30-2:55 | Limpeza | Rodar `./cleanup.sh` e mostrar o `false` do `az group exists` como comprovação. |
| 2:55-3:00 | Fechamento | Uma frase: Docker garante o mesmo ambiente em qualquer lugar; Azure dá infraestrutura sob demanda, criada e apagada por script. |

## Comandos na ordem (bloco de notas comentado)

```bash
# ===== 1. Na sua máquina (Azure Cloud Shell ou Git Bash) =====
az login                                   # login na conta da FIAP
chmod +x deploy-infra.sh cleanup.sh        # permissão de execução
./deploy-infra.sh                          # cria RG, VM Ubuntu e libera a porta 8080

# ===== 2. Dentro da VM =====
ssh <RM>@<IP_PUBLICO_DA_VM>                # acesso SSH (porta 22)
git clone https://github.com/cassio-ribeiro/Amazul_DevOps.git            # traz o código, o Dockerfile e os scripts
cd Amazul_DevOps
cp .env.example .env                       # modelo das variáveis do Oracle
nano .env                                  # preencher DB_URL, DB_USERNAME, DB_PASSWORD
chmod +x setup-app.sh
./setup-app.sh                             # instala Docker, docker build e docker run
sudo docker logs -f nucleo-api             # esperar "Started ChallengeApplication" (Ctrl+C para sair)
exit                                       # volta para a sua máquina

# ===== 3. Testes na sua máquina, pelo IP público =====
curl -X GET http://<IP_PUBLICO_DA_VM>:8080/perguntas

curl -X POST http://<IP_PUBLICO_DA_VM>:8080/perguntas \
  -H "Content-Type: application/json" \
  -d '{"enunciado":"A tecnologia nuclear possui aplicacoes na medicina.","resposta":"V","explicacao":"A medicina nuclear usa radiofarmacos em diagnosticos e tratamentos.","referencia":"AMAZUL - Kick-off FIAP - 13OUT26, p. 34"}'

curl -X GET http://<IP_PUBLICO_DA_VM>:8080/jogo/perguntas/aleatoria
# navegador: http://<IP_PUBLICO_DA_VM>:8080/swagger-ui

# ===== 4. Limpeza =====
./cleanup.sh                               # az group delete + az group exists (comprovação: false)
```

> No **PowerShell** o `curl` é outro comando: use `curl.exe` e o JSON em arquivo (`-d "@body.json"`), como no resumo de 22/10. No **Git Bash** e no **Cloud Shell** os comandos acima funcionam como estão.
