param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) {
    $programas = Join-Path $env:LOCALAPPDATA 'Programs'
    $idea = Get-ChildItem -LiteralPath $programas -Directory -Filter 'IntelliJ IDEA*' |
        Sort-Object Name -Descending | Select-Object -First 1
    if ($idea) { $JavaHome = Join-Path $idea.FullName 'jbr' }
}
if (-not $JavaHome) { throw 'Configure JAVA_HOME para um JDK 25.' }
$raiz = Split-Path $PSScriptRoot -Parent
$versao = '1.37.0'
$nome = "google-java-format-$versao-all-deps.jar"
$pasta = Join-Path $raiz 'target\tools'
New-Item -ItemType Directory -Path $pasta -Force | Out-Null
$arquivo = Join-Path $pasta $nome
$url = "https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/$versao/$nome"
if (-not (Test-Path -LiteralPath $arquivo)) { Invoke-WebRequest -Uri $url -OutFile $arquivo }
$checksum = ((Invoke-WebRequest -Uri ($url + '.sha1')).Content.ToString().Trim() -split '\s+')[0]
if ((Get-FileHash -LiteralPath $arquivo -Algorithm SHA1).Hash -ine $checksum) {
    throw 'Checksum do formatador divergente.'
}
$fontes = Get-ChildItem -LiteralPath (Join-Path $raiz 'src') -Filter '*.java' -Recurse |
    Select-Object -ExpandProperty FullName
& (Join-Path $JavaHome 'bin\java.exe') -jar $arquivo --aosp --replace @fontes
if ($LASTEXITCODE -ne 0) { throw 'A formatacao Java falhou.' }
