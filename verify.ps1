$ErrorActionPreference = 'Stop'

$projectDir = (Resolve-Path $PSScriptRoot).Path
$instanceDir = 'C:\Users\pichau\curseforge\minecraft\Instances\21'
$installDir = 'C:\Users\pichau\curseforge\minecraft\Install'
$javaHome = 'C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot'
$javac = Join-Path $javaHome 'bin\javac.exe'
$java = Join-Path $javaHome 'bin\java.exe'
$addonJar = Join-Path $instanceDir 'mods\reliquified_twilight_forest-1.21.1-0.5.3.jar'
$compatJar = Join-Path $projectDir 'build\libs\reliquified-twilight-forest-new-relics-fix-1.0.4.jar'
$classesDir = Join-Path $projectDir 'build\classes'
$testClassesDir = Join-Path $projectDir 'build\test-classes'
$asmJar = Join-Path $installDir 'libraries\org\ow2\asm\asm\9.7.1\asm-9.7.1.jar'
$asmTreeJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-tree\9.7.1\asm-tree-9.7.1.jar'
$asmCommonsJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-commons\9.7.1\asm-commons-9.7.1.jar'
$asmUtilJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-util\9.7.1\asm-util-9.7.1.jar'
$modLauncherJar = Join-Path $installDir 'libraries\cpw\mods\modlauncher\11.0.5\modlauncher-11.0.5.jar'
$secureJarHandlerJar = Join-Path $installDir 'libraries\cpw\mods\securejarhandler\3.0.8\securejarhandler-3.0.8.jar'
$coreModsJar = Join-Path $installDir 'libraries\net\neoforged\coremods\7.0.3\coremods-7.0.3.jar'
$nashornJar = Join-Path $installDir 'libraries\org\openjdk\nashorn\nashorn-core\15.4\nashorn-core-15.4.jar'
$log4jApiJar = Join-Path $installDir 'libraries\org\apache\logging\log4j\log4j-api\2.24.1\log4j-api-2.24.1.jar'
$log4jCoreJar = Join-Path $installDir 'libraries\org\apache\logging\log4j\log4j-core\2.24.1\log4j-core-2.24.1.jar'
$minecraftJar = Join-Path $installDir 'libraries\net\minecraft\client\1.21.1\client-1.21.1-official.jar'
$mixinJar = Join-Path $installDir 'libraries\net\fabricmc\sponge-mixin\0.15.2+mixin.0.8.7\sponge-mixin-0.15.2+mixin.0.8.7.jar'
$fmlLoaderJar = Join-Path $installDir 'libraries\net\neoforged\fancymodloader\loader\4.0.43\loader-4.0.43.jar'
$eventBusJar = Join-Path $installDir 'libraries\net\neoforged\bus\8.0.5\bus-8.0.5.jar'
$mergeToolApiJar = Join-Path $installDir 'libraries\net\neoforged\mergetool\2.0.7\mergetool-2.0.7-api.jar'
$neoForgeJar = Join-Path $installDir 'libraries\net\neoforged\neoforge\21.1.248\neoforge-21.1.248-universal.jar'
$dataFixerJar = Join-Path $installDir 'libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar'
$guavaJar = Join-Path $installDir 'libraries\com\google\guava\guava\33.3.1-jre\guava-33.3.1-jre.jar'
$relicsJar = Join-Path $instanceDir 'mods\relics-1.21.1-0.12.8.jar'
$curiosJar = Join-Path $instanceDir 'mods\curios-neoforge-9.5.1+1.21.1.jar'

& (Join-Path $projectDir 'build.ps1')
if ($LASTEXITCODE -ne 0) { throw 'Build failed before verification.' }

if (Test-Path -LiteralPath $testClassesDir) {
    $resolved = [IO.Path]::GetFullPath($testClassesDir)
    if (-not $resolved.StartsWith($projectDir + [IO.Path]::DirectorySeparatorChar)) {
        throw "Diretório de teste fora do projeto: $resolved"
    }
    Remove-Item -LiteralPath $resolved -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $testClassesDir | Out-Null

$classpath = @($classesDir, $asmJar, $asmTreeJar, $modLauncherJar, $coreModsJar) -join [IO.Path]::PathSeparator
$testSource = Join-Path $projectDir 'src\test\java\dev\codex\rtfnrf\BytecodeCompatibilityVerifier.java'
& $javac --release 21 -encoding UTF-8 -proc:none -classpath $classpath -d $testClassesDir $testSource
if ($LASTEXITCODE -ne 0) { throw 'Verifier compilation failed.' }

$runtimeClasspath = @(
    $testClassesDir, $classesDir, $asmJar, $asmTreeJar, $asmCommonsJar, $asmUtilJar,
    $modLauncherJar, $secureJarHandlerJar, $coreModsJar, $nashornJar, $log4jApiJar, $log4jCoreJar,
    $minecraftJar, $mixinJar, $fmlLoaderJar, $eventBusJar, $mergeToolApiJar,
    $neoForgeJar, $dataFixerJar, $guavaJar, $relicsJar, $curiosJar
) -join [IO.Path]::PathSeparator
& $java -classpath $runtimeClasspath dev.codex.rtfnrf.BytecodeCompatibilityVerifier $addonJar $compatJar
if ($LASTEXITCODE -ne 0) { throw 'Bytecode verification failed.' }

$metadata = tar -xOf $compatJar META-INF/neoforge.mods.toml
$ranges = $metadata | Select-String -Pattern '^versionRange=' | ForEach-Object { $_.Line }
$closedUpperBounds = $ranges | Where-Object { $_ -notmatch ',\)"$' }
if ($closedUpperBounds) {
    throw "Requisito com versão máxima encontrado: $($closedUpperBounds -join ', ')"
}
Write-Output "Requisitos verificados sem versão máxima: $($ranges.Count)"

$mixinConfig = (tar -xOf $compatJar reliquified_twilight_forest_new_relics_fix.mixins.json) | ConvertFrom-Json
if ($mixinConfig.client -notcontains 'ClientAbilityActivationMixin' -or
    $mixinConfig.mixins -contains 'ClientAbilityActivationMixin') {
    throw 'A ponte do Ride Along precisa ser registrada somente no cliente.'
}
Write-Output 'Ponte do Ride Along registrada somente no cliente.'
