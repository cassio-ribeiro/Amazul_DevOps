param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
$arquivo = Join-Path $PSScriptRoot '..\dados\perguntas-exemplo.json'
$perguntas = Get-Content -LiteralPath $arquivo -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($pergunta in $perguntas) {
    $json = $pergunta | ConvertTo-Json
    $registro = Invoke-RestMethod -Method Post -Uri ($BaseUrl.TrimEnd('/') + '/perguntas') -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
    Write-Output ('Criada pergunta ' + $registro.id)
}
# Cada execucao cria novos registros. Execute uma vez por acervo de demonstracao.
