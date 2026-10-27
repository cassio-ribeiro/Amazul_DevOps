#!/bin/bash
# cleanup.sh - Apaga o Grupo de Recursos e comprova a exclusão
# Executar no Azure Cloud Shell (Bash) ou no Git Bash, depois da demonstração

RG="rg-nucleo-sprint1"

echo "[1/2] Apagando o Grupo de Recursos $RG (VM, IP, VNet, NSG e discos)..."
# Sem --no-wait de propósito: o terminal espera a exclusão terminar,
# assim a comprovação aparece no vídeo
az group delete --name $RG --yes
echo ""

echo "[2/2] Comprovando a exclusão (o esperado é: false):"
az group exists --name $RG
