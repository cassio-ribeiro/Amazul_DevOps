#!/bin/bash
# deploy-infra.sh - Cria a infraestrutura na Azure (Grupo de Recursos, VM e porta)
# Executar no Azure Cloud Shell (Bash) ou no Git Bash com a Azure CLI instalada
# Uso (no Cloud Shell, depois do git clone do repositório):
#   chmod +x deploy-infra.sh
#   ./deploy-infra.sh
# Rodar como arquivo .sh evita o erro da CP2 de colar comandos com \ no Cloud Shell

# ===== Variáveis =====
RG="rg-nucleo-sprint1"
LOCATION="southafricanorth" # região que funcionou na cota de estudante no ensaio da CP2
VM="vm-nucleo-api"
IMAGE="Ubuntu2404"
SIZE="Standard_B2als_v2"   # 2 vCPU / 4 GB, tamanho que funcionou na CP2 (suficiente para o build Maven)
ADMIN_USER="rm567295"       # usuário da VM = RM
PORT=8080                  # server.port da API (application.properties)

echo "*************************************"
echo "Projeto Núcleo - Deploy da Infraestrutura"
echo "*************************************"
echo ""

# Senha da VM digitada na hora, para não ficar gravada no GitHub
# (mínimo 12 caracteres, com 3 de 4: maiúscula, minúscula, número, especial)
echo "Digite a senha do usuário $ADMIN_USER da VM:"
read ADMIN_PASSWORD
echo ""

echo "[1/5] Conferindo a assinatura ativa..."
az account show --query "{Sub:id, Nome:name}" -o jsonc
echo ""

echo "[2/5] Criando o Grupo de Recursos $RG em $LOCATION..."
az group create --name $RG --location $LOCATION
echo ""

echo "[3/5] Criando a VM $VM ($IMAGE, $SIZE)..."
IP=$(az vm create \
  --resource-group $RG \
  --name $VM \
  --location $LOCATION \
  --image $IMAGE \
  --size $SIZE \
  --authentication-type password \
  --admin-username $ADMIN_USER \
  --admin-password "$ADMIN_PASSWORD" \
  --vnet-name vnet-nucleo \
  --nsg nsg-nucleo \
  --public-ip-address pip-nucleo \
  --public-ip-sku Standard \
  --tags "owner=Nucleo" "purpose=Challenge" \
  --query publicIpAddress -o tsv)
echo "VM criada. IP público: $IP"
echo ""

echo "[4/5] Liberando a porta $PORT da API no NSG..."
az vm open-port --resource-group $RG --name $VM --port $PORT --priority 1010
echo ""

echo "[5/5] Infraestrutura pronta!"
echo "Próximo passo, conectar na VM:"
echo "  ssh $ADMIN_USER@$IP"
echo "Depois de subir o container, a API responde em:"
echo "  http://$IP:$PORT"
