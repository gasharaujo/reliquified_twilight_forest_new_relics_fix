param(
    [switch]$Install
)

$ErrorActionPreference = 'Stop'

$projectDir = (Resolve-Path $PSScriptRoot).Path
$meusModsDir = (Resolve-Path (Join-Path $projectDir '..')).Path
$instanceDir = 'C:\Users\pichau\curseforge\minecraft\Instances\21'
$installDir = 'C:\Users\pichau\curseforge\minecraft\Install'
$javaHome = 'C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot'
$javac = Join-Path $javaHome 'bin\javac.exe'
$jarTool = Join-Path $javaHome 'bin\jar.exe'

$minecraftJar = Join-Path $installDir 'libraries\net\minecraft\client\1.21.1\client-1.21.1-official.jar'
$mixinJar = Join-Path $installDir 'libraries\net\fabricmc\sponge-mixin\0.15.2+mixin.0.8.7\sponge-mixin-0.15.2+mixin.0.8.7.jar'
$fmlLoaderJar = Join-Path $installDir 'libraries\net\neoforged\fancymodloader\loader\4.0.43\loader-4.0.43.jar'
$eventBusJar = Join-Path $installDir 'libraries\net\neoforged\bus\8.0.5\bus-8.0.5.jar'
$mergeToolApiJar = Join-Path $installDir 'libraries\net\neoforged\mergetool\2.0.7\mergetool-2.0.7-api.jar'
$modLauncherJar = Join-Path $installDir 'libraries\cpw\mods\modlauncher\11.0.5\modlauncher-11.0.5.jar'
$neoForgeJar = Join-Path $installDir 'libraries\net\neoforged\neoforge\21.1.248\neoforge-21.1.248-universal.jar'
$asmJar = Join-Path $installDir 'libraries\org\ow2\asm\asm\9.7.1\asm-9.7.1.jar'
$asmTreeJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-tree\9.7.1\asm-tree-9.7.1.jar'
$dataFixerJar = Join-Path $installDir 'libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar'
$guavaJar = Join-Path $installDir 'libraries\com\google\guava\guava\33.3.1-jre\guava-33.3.1-jre.jar'
$relicsJar = Join-Path $instanceDir 'mods\relics-1.21.1-0.12.8.jar'
$addonJar = Join-Path $instanceDir 'mods\reliquified_twilight_forest-1.21.1-0.5.3.jar'
$curiosJar = Join-Path $instanceDir 'mods\curios-neoforge-9.5.1+1.21.1.jar'

$requiredFiles = @(
    $javac, $jarTool, $minecraftJar, $mixinJar, $fmlLoaderJar, $eventBusJar, $mergeToolApiJar,
    $modLauncherJar, $neoForgeJar, $asmJar, $asmTreeJar,
    $dataFixerJar, $guavaJar, $relicsJar, $addonJar, $curiosJar
)
foreach ($requiredFile in $requiredFiles) {
    if (-not (Test-Path -LiteralPath $requiredFile)) {
        throw "Dependência de compilação não encontrada: $requiredFile"
    }
}

$version = '1.0.4'
$jarName = "reliquified-twilight-forest-new-relics-fix-$version.jar"
$buildDir = Join-Path $projectDir 'build'
$classesDir = Join-Path $buildDir 'classes'
$stagingDir = Join-Path $buildDir 'staging'
$libsDir = Join-Path $buildDir 'libs'
$jarPath = Join-Path $libsDir $jarName
$buildArchiveDir = Join-Path $meusModsDir 'builds'
$archivePath = Join-Path $buildArchiveDir $jarName

foreach ($directory in @($classesDir, $stagingDir)) {
    if (Test-Path -LiteralPath $directory) {
        $resolved = [IO.Path]::GetFullPath($directory)
        if (-not $resolved.StartsWith($projectDir + [IO.Path]::DirectorySeparatorChar)) {
            throw "Diretório de build fora do projeto: $resolved"
        }
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}
New-Item -ItemType Directory -Force -Path $classesDir, $stagingDir, $libsDir, $buildArchiveDir | Out-Null

$classpath = @(
    $minecraftJar, $mixinJar, $fmlLoaderJar, $eventBusJar, $mergeToolApiJar, $modLauncherJar,
    $neoForgeJar, $asmJar, $asmTreeJar, $dataFixerJar, $guavaJar,
    $relicsJar, $addonJar, $curiosJar
) -join [IO.Path]::PathSeparator

$sources = Get-ChildItem -LiteralPath (Join-Path $projectDir 'src\main\java') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName
if (-not $sources) {
    throw 'Nenhum arquivo Java encontrado.'
}

& $javac --release 21 -encoding UTF-8 -proc:none -classpath $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) {
    throw "A compilação falhou com o código $LASTEXITCODE."
}

$resourcesDir = Join-Path $projectDir 'src\main\resources'
Copy-Item -Path (Join-Path $resourcesDir '*') -Destination $stagingDir -Recurse
Copy-Item -Path (Join-Path $classesDir '*') -Destination $stagingDir -Recurse

# Relics 0.12 uses relics.description.*, while the add-on ships tooltip.relics.*.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$languageDir = Join-Path $stagingDir 'assets\reliquified_twilight_forest_new_relics_fix\lang'
New-Item -ItemType Directory -Force -Path $languageDir | Out-Null
$addonArchive = [IO.Compression.ZipFile]::OpenRead($addonJar)
try {
    $languageEntries = $addonArchive.Entries | Where-Object {
        $_.FullName -match '^assets/reliquified_twilight_forest/lang/([^/]+)\.json$'
    }
    foreach ($entry in $languageEntries) {
        $language = [IO.Path]::GetFileNameWithoutExtension($entry.Name)
        $reader = [IO.StreamReader]::new($entry.Open())
        try {
            $sourceTranslations = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable
        } finally {
            $reader.Dispose()
        }
        $aliases = [ordered]@{}
        foreach ($key in $sourceTranslations.Keys) {
            if ($key.StartsWith('tooltip.relics.')) {
                $aliases[$key -replace '^tooltip\.relics\.', 'relics.description.'] = $sourceTranslations[$key]
            }
        }
        $aliases | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $languageDir "$language.json") -Encoding UTF8
        Write-Output "Aliases de tradução gerados ($language): $($aliases.Count)"
    }
} finally {
    $addonArchive.Dispose()
}

if (Test-Path -LiteralPath $jarPath) {
    Remove-Item -LiteralPath $jarPath -Force
}
& $jarTool --create --file $jarPath --no-manifest -C $stagingDir .
if ($LASTEXITCODE -ne 0) {
    throw "A criação do JAR falhou com o código $LASTEXITCODE."
}

Copy-Item -LiteralPath $jarPath -Destination $archivePath -Force
Write-Output "Build arquivada: $archivePath"

if ($Install) {
    $modsDir = Join-Path $instanceDir 'mods'
    $resolvedModsDir = [IO.Path]::GetFullPath($modsDir)
    $expectedModsDir = [IO.Path]::GetFullPath('C:\Users\pichau\curseforge\minecraft\Instances\21\mods')
    if ($resolvedModsDir -ne $expectedModsDir) {
        throw "Destino de instalação inesperado: $resolvedModsDir"
    }
    $previousBuilds = Get-ChildItem -LiteralPath $resolvedModsDir -Filter 'reliquified-twilight-forest-new-relics-fix-*.jar' -File
    $backupDir = Join-Path $buildDir ('installed-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
    if ($previousBuilds) {
        New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    }
    foreach ($previousBuild in $previousBuilds) {
        if ([IO.Path]::GetFullPath($previousBuild.DirectoryName) -ne $expectedModsDir -or
            $previousBuild.Name -notmatch '^reliquified-twilight-forest-new-relics-fix-[0-9]+\.[0-9]+\.[0-9]+\.jar$') {
            throw "Build anterior fora do escopo permitido: $($previousBuild.FullName)"
        }
        Copy-Item -LiteralPath $previousBuild.FullName -Destination $backupDir
        Remove-Item -LiteralPath $previousBuild.FullName -Force
        Write-Output "Build instalada substituída: $($previousBuild.Name)"
    }
    if ($previousBuilds) { Write-Output "Backup da versão anterior: $backupDir" }
    $installedJar = Join-Path $resolvedModsDir $jarName
    Copy-Item -LiteralPath $jarPath -Destination $installedJar -Force
    Write-Output "Instalado: $installedJar"
}

Write-Output "Gerado: $jarPath"
