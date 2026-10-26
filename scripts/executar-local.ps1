param([string]$JavaHome = $env:JAVA_HOME, [switch]$Oracle, [switch]$Testes)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) {
    $programas = Join-Path $env:LOCALAPPDATA 'Programs'
    $idea = Get-ChildItem -LiteralPath $programas -Directory -Filter 'IntelliJ IDEA*' | Sort-Object Name -Descending | Select-Object -First 1
    if ($idea) { $JavaHome = Join-Path $idea.FullName 'jbr' }
}
if (-not $JavaHome -or -not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin\java.exe'))) {
    throw 'Configure JAVA_HOME com um JDK 25 ou informe -JavaHome.'
}
$anterior = $env:JAVA_HOME
$opcoesAnteriores = $env:JAVA_TOOL_OPTIONS
$caminhoAnterior = $env:PATH
$raiz = Split-Path $PSScriptRoot -Parent
# Neste Windows, o runtime JBR falha em sockets Unix. Um caminho inexistente
# faz o runtime usar TCP local. A opcao vale somente para os processos deste script.
$socketIndisponivel = Join-Path $raiz 'target\socket-unix-indisponivel'
$env:JAVA_HOME = $JavaHome
# O wrapper oficial usa powershell.exe; o PATH de alguns terminais so tem pwsh.
if (-not (Get-Command powershell.exe -ErrorAction SilentlyContinue)) {
    $powershellWindows = Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0'
    if (Test-Path -LiteralPath (Join-Path $powershellWindows 'powershell.exe')) {
        $env:PATH = $powershellWindows + [IO.Path]::PathSeparator + $env:PATH
    }
}
$env:JAVA_TOOL_OPTIONS = ($opcoesAnteriores + ' -Djdk.net.unixdomain.tmpdir=' + ('"' + $socketIndisponivel + '"')).Trim()
Push-Location $raiz
try {
    if ($Testes) {
        if ($Oracle) { & '.\mvnw.cmd' -B verify -Poracle-check }
        else { & '.\mvnw.cmd' -B verify }
    } elseif ($Oracle) { & '.\mvnw.cmd' spring-boot:run '-Dspring-boot.run.profiles=oracle' }
    else { & '.\mvnw.cmd' spring-boot:run }
    if ($LASTEXITCODE -ne 0) { throw 'A execucao da API falhou. Confira as mensagens acima.' }
} finally {
    Pop-Location
    $env:JAVA_HOME = $anterior
    $env:JAVA_TOOL_OPTIONS = $opcoesAnteriores
    $env:PATH = $caminhoAnterior
}
