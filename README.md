# SmOrg (Smart Organization) — Mobile

Repositório da aplicação cliente móvel do sistema **SmOrg**, um ecossistema de gestão de ativos físicos e controle de empréstimos por aproximação (**NFC**) para laboratórios acadêmicos, projetado para oferecer uma experiência de "atrito zero".

Desenvolvido para a disciplina **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis** (UFRN / DIMAp).

O documento detalhado com visão do produto, MVP, backlog e decisões técnicas encontra-se em [`docs/proposta.md`](docs/proposta.md).

---

## Equipe e Informações Acadêmicas

* **Aluno:** Miguel Xavier de Morais (Matrícula: `20240027427`)
* **Coorte de Apresentação:** Coorte A (Presencial)
* **Integração Declarada:** Este projeto está formalmente integrado com a disciplina **DIM0547 — Desenvolvimento de Sistemas Web II**. O backend consumido por este aplicativo móvel é o [SmOrg API](https://github.com/Goguel/smorg-api), construído em Kotlin (Ktor) e Go (gRPC).

---

## Arquitetura e Tecnologias

A aplicação móvel adota **Kotlin Multiplatform (KMP)** e **Compose Multiplatform**, permitindo o compartilhamento integral da lógica de negócios e da interface gráfica declarativa em `commonMain`:

* **Linguagem:** Kotlin
* **Interface Declarativa:** Compose Multiplatform com Material 3 (suporte a modo claro/escuro e layout adaptativo)
* **Navegação:** Navigation Compose com rotas tipadas, passagem de argumentos e deep links
* **Gerenciamento de Estado:** ViewModel multiplataforma com `StateFlow` e modelagem de UI por interfaces seladas
* **Injeção de Dependências:** Koin em `commonMain`
* **Comunicação de Rede:** Ktor Client + `kotlinx.serialization` consumindo a [SmOrg API](https://github.com/Goguel/smorg-api)
* **Persistência Local e Cache Offline:** Room / SQLDelight com esquema versionado e política offline-first
* **Recursos Nativos:** Suporte a NFC e hardware via abstrações `expect`/`actual`
* **Testes e Qualidade:** `kotlin.test`, Turbine (Flows), Compose UI Tests, `ktlint` e `detekt`

---

## Como Executar Localmente

### Pré-requisitos
* **JDK 17** ou superior instalado e configurado no `JAVA_HOME`.
* **Android Studio** atualizado com o plugin *Kotlin Multiplatform*.
* Dispositivo físico Android conectado via USB (com depuração ativada) ou emulador Android configurado.

### Execução no Desktop (Ciclo Rápido com Hot Reload)
Para desenvolvimento ágil de interfaces sem a necessidade de iniciar o emulador:
```bash
./gradlew :composeApp:run
```

### Execução no Android
Via linha de comando:
```bash
./gradlew :composeApp:installDebug
```
Ou selecione a configuração **composeApp** no Android Studio e clique em **Run ▶**.

### Análise Estática de Código (Qualidade)
Para conferir localmente as regras de estilo e lint antes do commit:
```bash
./gradlew ktlintCheck detekt
```

---

## Checklist de Avaliação (Rúbricas da Disciplina)

### Sprint 0 — Fundação, Proposta e CI
- [ ] Repositório público, app compila e roda nos alvos Android e Desktop
- [ ] CI verde no GitHub Actions (`ktlintCheck` + `detekt` + `assembleDebug`)
- [ ] `docs/proposta.md` completo com justificativa técnica de plataforma e backend
- [ ] *(opcional)* 1 tela em Compose com componente próprio e estado elevado
- [ ] Coorte (A - Presencial) e integração declaradas
- [ ] Vídeo de 5 minutos publicado

### Sprint 1 — Interface e Navegação
- [ ] Todas as telas principais do MVP implementadas em Compose
- [ ] Navigation Compose: rotas tipadas, navegação com argumentos e $\ge 1$ deep link funcional
- [ ] Tema Material 3 completo (esquema de cores, modo claro e escuro)
- [ ] Layout adaptado a $\ge 2$ tamanhos de janela sem quebra/overflow
- [ ] Acessibilidade: descrições de conteúdo, contraste e alvos de toque $\ge 48$ dp
- [ ] Formulário com validações de entrada
- [ ] $\ge 5$ testes de interface passando no CI
- [ ] Vídeo de 5 minutos e apresentação presencial

### Sprint 2 — Estado e Arquitetura
- [ ] Gerenciamento de estado com ViewModel multiplataforma e `StateFlow`
- [ ] Arquitetura em camadas (`data`, `domain`, `presentation`) em `commonMain`
- [ ] Domínio puro: sem dependência ou imports de Compose
- [ ] Repositórios definidos por interface no domínio
- [ ] Modelagem de estado da UI com classes seladas (Loading, Success, Empty, Error)
- [ ] Injeção de dependências com Koin em `commonMain`
- [ ] $\ge 8$ testes de ViewModel com Turbine
- [ ] `docs/arquitetura.md` com diagrama de camadas e justificativas
- [ ] Vídeo de 5 minutos e apresentação presencial

### Sprint 3 — Rede, Dados e Offline-First
- [ ] Consumo de dados reais da [smorg-api](https://github.com/Goguel/smorg-api) via Ktor Client
- [ ] Serialização com `kotlinx.serialization` e desacoplamento entre DTOs e entidades de domínio
- [ ] Tratamento granular de erros de rede (timeout, conexão, respostas inválidas) com retentativas
- [ ] Persistência local com Room ou SQLDelight (esquema versionado com migrações)
- [ ] Funcionamento **offline-first**: cache local, fila de escrita pendente e sincronização ao reconectar
- [ ] `docs/offline.md` com especificação da política de resolução de conflitos
- [ ] Autenticação com armazenamento seguro de token no dispositivo
- [ ] Vídeo de 5 minutos e apresentação presencial

### Entrega Final — Recursos Nativos, Segurança e Distribuição
- [ ] Aplicação completa e estável em dispositivo real
- [ ] $\ge 2$ recursos do dispositivo integrados via `expect`/`actual` (NFC e hardware) com fluxo completo de permissões
- [ ] Segurança e mitigação do OWASP Mobile Top 10 documentada em `docs/seguranca.md`
- [ ] Otimização de desempenho comprovada em `docs/desempenho.md` com métricas de recomposição antes/depois
- [ ] Pipeline de CD gerando e publicando release de APK assinado automaticamente a partir de `main`
- [ ] Suíte completa de testes (unidade, ViewModel, interface e integração) verde no CI
- [ ] $\ge 2$ ambientes de build configurados (ex: dev/staging e prod)
- [ ] Documentação pública atualizada, README com instruções claras e licença
- [ ] Vídeo de 10 minutos e apresentação final ao vivo
