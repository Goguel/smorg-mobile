[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$Token = $env:GITHUB_TOKEN
)

if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host "=================================================================" -ForegroundColor Yellow
    Write-Host "ATENCAO: Token do GitHub nao fornecido." -ForegroundColor Yellow
    Write-Host "Para executar a atualizacao automatica, forneca seu token do GitHub:" -ForegroundColor Yellow
    Write-Host ".\scripts\sync_github_issues.ps1 -Token <SEU_GITHUB_TOKEN>" -ForegroundColor Cyan
    Write-Host "=================================================================" -ForegroundColor Yellow
    $Token = Read-Host "Insira seu Personal Access Token do GitHub"
    if ([string]::IsNullOrWhiteSpace($Token)) {
        Write-Warning "Operacao cancelada."
        exit 0
    }
}

$RepoOwner = "Goguel"
$RepoName = "smorg-mobile"
$BaseApi = "https://api.github.com/repos/$RepoOwner/$RepoName"
$GraphqlApi = "https://api.github.com/graphql"

$Headers = @{
    "Authorization"        = "Bearer $Token"
    "Accept"               = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
    "User-Agent"           = "SmOrg-Mobile-Automation-Script"
}

Write-Host "`n[START] Iniciando sincronizacao no repositorio $RepoOwner/$RepoName...`n" -ForegroundColor Cyan

# 1. Configurar Labels
$Labels = @(
    @{ name = "prio:P1"; color = "B60205"; description = "Alta prioridade (essencial para o MVP)" },
    @{ name = "prio:P2"; color = "D93F0B"; description = "Media prioridade (catalogo e offline)" },
    @{ name = "prio:P3"; color = "FBCA04"; description = "Baixa prioridade (seguranca e auth)" },
    @{ name = "sprint:sprint-1"; color = "0E8A16"; description = "Tarefas da Sprint 1 (Interface e Navegacao)" },
    @{ name = "sprint:sprint-2"; color = "1D76DB"; description = "Tarefas da Sprint 2 (Estado e Arquitetura)" },
    @{ name = "sprint:sprint-3"; color = "5319E7"; description = "Tarefas da Sprint 3 (Rede, Dados e Offline)" },
    @{ name = "type:user-story"; color = "0075CA"; description = "Historia de Usuario (User Story)" },
    @{ name = "type:tech-task"; color = "A2EEEF"; description = "Tarefa tecnica de infraestrutura ou CI/CD" },
    @{ name = "type:ui-ux"; color = "E99695"; description = "Componentes de Interface Compose e Acessibilidade" },
    @{ name = "component:compose-ui"; color = "C5DEF5"; description = "Telas Compose Multiplatform Material 3" },
    @{ name = "component:navigation"; color = "D4C5F9"; description = "Navigation Compose com rotas tipadas" },
    @{ name = "component:nfc-native"; color = "BFDADC"; description = "Integracao de hardware NFC nativo" },
    @{ name = "component:network-ktor"; color = "F9D0C4"; description = "Ktor Client consumindo a SmOrg API" },
    @{ name = "component:offline-cache"; color = "FEF2C0"; description = "Persistencia local e cache offline" }
)

Write-Host "[LABELS] Criando/Atualizando labels padronizadas..." -ForegroundColor Yellow
foreach ($lbl in $Labels) {
    $bodyJson = @{
        name        = $lbl.name
        color       = $lbl.color
        description = $lbl.description
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Uri "$BaseApi/labels" -Method Post -Headers $Headers -Body $bodyJson -ContentType "application/json; charset=utf-8" -ErrorAction Stop | Out-Null
        Write-Host "  [+] Label criada: $($lbl.name)" -ForegroundColor Green
    } catch {
        try {
            $encodedName = [System.Uri]::EscapeDataString($lbl.name)
            Invoke-RestMethod -Uri "$BaseApi/labels/$encodedName" -Method Patch -Headers $Headers -Body $bodyJson -ContentType "application/json; charset=utf-8" -ErrorAction Stop | Out-Null
            Write-Host "  [~] Label atualizada: $($lbl.name)" -ForegroundColor Gray
        } catch {
            Write-Host "  [*] Label existente: $($lbl.name)" -ForegroundColor DarkGray
        }
    }
}

# 2. Configurar Milestones
$Milestones = @(
    @{ title = "Sprint 1 - Interface e Navegacao"; description = "Telas Compose Material 3 (claro/escuro), rotas tipadas Navigation Compose, layout adaptativo e testes de UI." },
    @{ title = "Sprint 2 - Estado e Arquitetura"; description = "ViewModels multiplataforma com StateFlow, arquitetura em camadas commonMain, Koin e testes Turbine." },
    @{ title = "Sprint 3 - Rede, Dados e Offline-First"; description = "Integracao real com a SmOrg API, Room/SQLDelight, cache offline e autenticacao segura." },
    @{ title = "Sprint Final - Recursos Nativos, Seguranca e Distribuicao"; description = "NFC em dispositivo real, relatorio OWASP Mobile, otimizacao e release APK." }
)

Write-Host "`n[MILESTONES] Criando milestones..." -ForegroundColor Yellow
$MilestoneMap = @{}
try {
    $existingMilestones = Invoke-RestMethod -Uri "$BaseApi/milestones" -Method Get -Headers $Headers -ErrorAction SilentlyContinue
    foreach ($m in $existingMilestones) {
        $MilestoneMap[$m.title] = $m.number
    }
} catch {
    $null = $null
}

foreach ($ms in $Milestones) {
    if (-not $MilestoneMap.ContainsKey($ms.title)) {
        $bodyJson = @{
            title       = $ms.title
            description = $ms.description
        } | ConvertTo-Json
        try {
            $created = Invoke-RestMethod -Uri "$BaseApi/milestones" -Method Post -Headers $Headers -Body $bodyJson -ContentType "application/json; charset=utf-8" -ErrorAction Stop
            $MilestoneMap[$ms.title] = $created.number
            Write-Host "  [+] Milestone criada: $($ms.title) (#$($created.number))" -ForegroundColor Green
        } catch {
            Write-Warning "  [!] Falha ao criar milestone $($ms.title): $_"
        }
    } else {
        Write-Host "  [*] Milestone ja existe: $($ms.title)" -ForegroundColor Gray
    }
}

# 3. Descobrir ID do GitHub Project #3
$ProjectId = $null
try {
    $gqlQuery = @{
        query = 'query { user(login: "Goguel") { projectV2(number: 3) { id title } } }'
    } | ConvertTo-Json
    $gqlRes = Invoke-RestMethod -Uri $GraphqlApi -Method Post -Headers $Headers -Body $gqlQuery -ContentType "application/json; charset=utf-8" -ErrorAction SilentlyContinue
    if ($gqlRes.data.user.projectV2.id) {
        $ProjectId = $gqlRes.data.user.projectV2.id
        Write-Host "`n[PROJECT] GitHub Project #3 localizado: $($gqlRes.data.user.projectV2.title) ($ProjectId)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "`n[PROJECT] Nao foi possivel acessar a API GraphQL de Projects v2: $_" -ForegroundColor DarkGray
}

# 4. Criar / Atualizar Issues
$IssueDefs = @(
    @{
        number    = 1
        title     = "[US01] Leitura e identificacao de equipamentos via aproximacao NFC"
        file      = "docs/backlog/issue-1-leitura-identificacao-nfc.md"
        labels    = @("type:user-story", "prio:P1", "sprint:sprint-1", "component:nfc-native", "component:compose-ui")
        milestone = "Sprint 1 - Interface e Navegacao"
    },
    @{
        number    = 2
        title     = "[US02] Confirmar retirada e emprestimo de equipamento via aproximacao"
        file      = "docs/backlog/issue-2-confirmar-retirada.md"
        labels    = @("type:user-story", "prio:P1", "sprint:sprint-1", "component:compose-ui", "component:network-ktor")
        milestone = "Sprint 1 - Interface e Navegacao"
    },
    @{
        number    = 3
        title     = "[US03] Registrar devolucao de equipamento via leitura NFC"
        file      = "docs/backlog/issue-3-registrar-devolucao.md"
        labels    = @("type:user-story", "prio:P1", "sprint:sprint-1", "component:compose-ui", "component:network-ktor")
        milestone = "Sprint 1 - Interface e Navegacao"
    },
    @{
        number    = 4
        title     = "[US04] Visualizar equipamentos sob custodia e prazos de devolucao"
        file      = "docs/backlog/issue-4-meus-emprestimos-ativos.md"
        labels    = @("type:user-story", "prio:P1", "sprint:sprint-1", "component:compose-ui", "component:navigation")
        milestone = "Sprint 1 - Interface e Navegacao"
    },
    @{
        number    = 5
        title     = "[US05] Catalogo e busca geral de equipamentos do laboratorio"
        file      = "docs/backlog/issue-5-catalogo-e-busca.md"
        labels    = @("type:user-story", "prio:P2", "sprint:sprint-1", "component:compose-ui", "component:navigation")
        milestone = "Sprint 1 - Interface e Navegacao"
    },
    @{
        number    = 6
        title     = "[US06] Autenticacao de usuario com matricula e armazenamento seguro de token"
        file      = "docs/backlog/issue-6-autenticacao-jwt.md"
        labels    = @("type:user-story", "prio:P3", "sprint:sprint-3", "component:compose-ui", "component:network-ktor")
        milestone = "Sprint 3 - Rede, Dados e Offline-First"
    },
    @{
        number    = 7
        title     = "[US07] Persistencia local offline-first e sincronizacao de dados"
        file      = "docs/backlog/issue-7-offline-first-cache.md"
        labels    = @("type:user-story", "prio:P2", "sprint:sprint-3", "component:offline-cache", "component:network-ktor")
        milestone = "Sprint 3 - Rede, Dados e Offline-First"
    }
)

Write-Host "`n[ISSUES] Criando / Atualizando as Issues 1 a 7..." -ForegroundColor Yellow
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    $ProjectRoot = Get-Location
}

# Obter issues existentes
$ExistingMap = @{}
try {
    $existingIssues = Invoke-RestMethod -Uri "$BaseApi/issues?state=all" -Method Get -Headers $Headers -ErrorAction SilentlyContinue
    foreach ($ei in $existingIssues) {
        $ExistingMap[$ei.number] = $ei
    }
} catch {}

foreach ($iss in $IssueDefs) {
    $filePath = Join-Path $ProjectRoot $iss.file
    if (-not (Test-Path $filePath)) {
        Write-Warning "Arquivo $($iss.file) nao encontrado. Pulando issue #$($iss.number)."
        continue
    }

    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)

    $payload = @{
        title  = $iss.title
        body   = $content
        labels = $iss.labels
    }

    if ($MilestoneMap.ContainsKey($iss.milestone)) {
        $payload["milestone"] = $MilestoneMap[$iss.milestone]
    }

    $bodyJson = $payload | ConvertTo-Json -Depth 5

    $issueNodeId = $null
    $targetNum = $iss.number

    if ($ExistingMap.ContainsKey($targetNum)) {
        # Atualiza issue existente
        try {
            $updated = Invoke-RestMethod -Uri "$BaseApi/issues/$targetNum" -Method Patch -Headers $Headers -Body $bodyJson -ContentType "application/json; charset=utf-8" -ErrorAction Stop
            $issueNodeId = $updated.node_id
            Write-Host "  [OK] Issue #$($targetNum) atualizada: $($iss.title)" -ForegroundColor Green
        } catch {
            Write-Error "  [FALHA] Nao foi possivel atualizar Issue #$($targetNum) - Erro: $_"
        }
    } else {
        # Cria nova issue
        try {
            $created = Invoke-RestMethod -Uri "$BaseApi/issues" -Method Post -Headers $Headers -Body $bodyJson -ContentType "application/json; charset=utf-8" -ErrorAction Stop
            $issueNodeId = $created.node_id
            $targetNum = $created.number
            Write-Host "  [+] Issue #$($targetNum) criada com sucesso: $($iss.title)" -ForegroundColor Green
        } catch {
            Write-Error "  [FALHA] Nao foi possivel criar Issue #$($targetNum) - Erro: $_"
        }
    }

    # Adicionar item ao GitHub Project #3 via GraphQL se o Project ID foi localizado
    if ($ProjectId -and $issueNodeId) {
        try {
            $mutation = @{
                query = "mutation { addProjectV2ItemById(input: { projectId: `"$ProjectId`", contentId: `"$issueNodeId`" }) { item { id } } }"
            } | ConvertTo-Json
            Invoke-RestMethod -Uri $GraphqlApi -Method Post -Headers $Headers -Body $mutation -ContentType "application/json; charset=utf-8" -ErrorAction Stop | Out-Null
            Write-Host "    -> Adicionada ao Project #3!" -ForegroundColor Cyan
        } catch {
            Write-Host "    [i] Nota: Nao foi possivel adicionar diretamente no card do Project #3 via API ($_) - pode ser associada via interface." -ForegroundColor DarkGray
        }
    }
}

Write-Host "`n[SUCCESS] Sincronizacao do SmOrg Mobile concluida! Acesse https://github.com/$RepoOwner/$RepoName/issues" -ForegroundColor Cyan
