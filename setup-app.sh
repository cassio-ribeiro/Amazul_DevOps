#!/bin/bash
# setup-app.sh - Instala o Docker na VM, gera a imagem da API e sobe o container
# Uso:
#   chmod +x setup-app.sh
#   ./setup-app.sh

# ===== Variáveis =====
IMAGE_NAME="nucleo-api"
CONTAINER_NAME="nucleo-api"
PORT=8080

echo "*************************************"
echo "Projeto Núcleo - Setup da Aplicação na VM"
echo "*************************************"
echo ""

echo "[1/6] Atualizando os pacotes do Ubuntu..."
sudo apt update -y
echo ""

echo "[2/6] Instalando e habilitando o Docker..."
sudo apt install -y docker.io
sudo systemctl enable --now docker
sudo docker --version
echo ""

echo "[3/6] Conferindo o arquivo .env com as credenciais do Oracle..."
# Mostra só a URL do banco (a senha não aparece). O esperado é oracle.fiap.com.br
grep DB_URL .env || exit 1
# Se ainda estiver com o valor do .env.example, para o script aqui
grep -q "@HOST:" .env && echo "ERRO: o .env ainda está com os valores de exemplo. Edite com: nano .env" && exit 1
echo ""

echo "[4/6] Gerando a imagem $IMAGE_NAME a partir do Dockerfile..."
sudo docker build -t $IMAGE_NAME .
sudo docker image ls
echo ""

echo "[5/6] Subindo o container $CONTAINER_NAME (host $PORT -> container $PORT)..."
# --env-file: carrega DB_URL, DB_USERNAME e DB_PASSWORD do .env
# --restart unless-stopped: o container volta sozinho se a VM reiniciar
sudo docker run -d --name $CONTAINER_NAME \
  -p $PORT:$PORT \
  --env-file .env \
  --restart unless-stopped \
  $IMAGE_NAME
sudo docker ps
echo ""

echo "[6/6] Aguarde o Spring Boot iniciar e confira os logs com:"
echo "  sudo docker logs -f $CONTAINER_NAME"
echo "Teste pela sua máquina (IP público, nunca localhost):"
echo "  curl -X GET http://IP_DA_VM:$PORT/perguntas"
echo "Swagger no navegador:"
echo "  http://IP_DA_VM:$PORT/swagger-ui"
