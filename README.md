# SmOrg (Smart Organization) — Mobile

Repositório da aplicação cliente móvel do sistema **SmOrg**, um ecossistema de gestão de ativos físicos e controle de empréstimos por aproximação (**NFC**) para laboratórios acadêmicos, projetado para oferecer uma experiência de "atrito zero".

Desenvolvido para a disciplina **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis** (UFRN / DIMAp).

* **Proposta do Produto e MVP:** [`docs/proposta.md`](docs/proposta.md)
* **Documentação de Arquitetura e Estado (Sprint 2):** [`docs/arquitetura.md`](docs/arquitetura.md)
* **Registro de Uso de IA:** [`docs/uso-de-ia.md`](docs/uso-de-ia.md)

---

## Equipe e Informações Acadêmicas

* **Aluno:** Miguel Xavier de Morais (Matrícula: `20240027427`)
* **Integração Declarada:** Este projeto está formalmente integrado com a disciplina **DIM0547 — Desenvolvimento de Sistemas Web II**. O backend consumido por este aplicativo móvel é o [SmOrg API](https://github.com/Goguel/smorg-api), construído em Kotlin (Ktor) e Go (gRPC).
* **Vídeo de Demonstração (Sprint 2 — 5 min):** [Link do Vídeo da Sprint 2](URL_DO_VIDEO_SPRINT_2_AQUI)

---

## Arquitetura e Tecnologias

A aplicação adota **Kotlin Multiplatform (KMP)** e **Compose Multiplatform**, compartilhando integralmente as camadas de domínio (`domain`), dados (`data`) e apresentação (`presentation`) em `commonMain`:

* **Linguagem:** Kotlin 2.0.21
* **Interface Declarativa:** Compose Multiplatform com Material 3 (suporte a modo claro/escuro, semântica de acessibilidade e layout adaptativo compacto/expandido)
* **Navegação:** Navigation Compose com rotas tipadas (`@Serializable`), argumentos e deep link (`smorg://equipamento/{id}`)
* **Gerenciamento de Estado:** ViewModels multiplataforma (`CatalogViewModel`, `LoginViewModel`) expondo `StateFlow` imutável e estados de tela modelados por interfaces seladas (`Loading`, `Empty`, `Success`, `Error`)
* **Injeção de Dependências:** Raiz de composição centralizada em [`AppModule.kt`](composeApp/src/commonMain/kotlin/br/ufrn/smorg/AppModule.kt), injetando `EquipmentRepository` por construtor
* **Comunicação de Rede:** Ktor Client (`OkHttp` no Android, `CIO` no Desktop, `Darwin` no iOS) + `kotlinx.serialization`, com `expectSuccess = true`, `ignoreUnknownKeys = true`, DTOs separados do domínio e tradução para falhas nomeadas (`NoConnection` e `InvalidResponse`)
* **Persistência Local (Offline-First):** `DataStore Preferences` multiplataforma (`CachedEquipmentRepository`), garantindo abertura sem rede com exibição do último conteúdo salvo e aviso de dados offline
* **Testes e Qualidade:** 27 testes automatizados em `commonTest` (`kotlin.test`, `Turbine`, `kotlinx-coroutines-test`, `MockEngine` do Ktor e `runComposeUiTest`), além de `ktlint` e `detekt` no GitHub Actions

---

## Como Executar Localmente (em menos de 15 minutos)

### 1. Pré-requisitos
* **JDK 17 ou 21** instalado e configurado no `JAVA_HOME`.
* **Android Studio** (com Android SDK 35) para rodar no emulador/dispositivo físico Android, ou apenas o terminal para rodar no alvo **Desktop (JVM)**.

### 2. Como Apontar para o Backend (`SmOrg API`)

O endereço do backend é definido por plataforma via `expect`/`actual` em `br.ufrn.smorg.data.defaultApiBaseUrl`:

* **Padrão (Nuvem — zero configuração):** Tanto no Android quanto no Desktop, o app já aponta por padrão para a instância pública da API de Web II no Render (`https://smorg-api.onrender.com/api/v1`).
* **No Desktop (via variável de ambiente `SMORG_API`):**
  * **Usar a API na nuvem (padrão):**
    ```bash
    ./gradlew :composeApp:run
    ```
  * **Usar a API rodando localmente (`http://localhost:8080/api/v1`):**
    ```bash
    SMORG_API=http://localhost:8080/api/v1 ./gradlew :composeApp:run
    ```
  * **Rodar sem backend (repositório em memória):**
    ```bash
    SMORG_API=memoria ./gradlew :composeApp:run
    ```
* **No Android (`ApiAddress.android.kt`):**
  * Para apontar o emulador Android para o backend rodando localmente na máquina hospedeira, altere `defaultApiBaseUrl` em [`ApiAddress.android.kt`](composeApp/src/androidMain/kotlin/br/ufrn/smorg/data/ApiAddress.android.kt) para `"http://10.0.2.2:8080/api/v1"`.

### 3. Execução no Desktop e no Android

* **Rodar no Desktop (JVM):**
  ```bash
  ./gradlew :composeApp:run
  ```
* **Instalar e rodar no Emulador / Dispositivo Android conectado:**
  ```bash
  ./gradlew :composeApp:installDebug
  ```
* **Gerar o APK de Debug (`composeApp/build/outputs/apk/debug/composeApp-debug.apk`):**
  ```bash
  ./gradlew :composeApp:assembleDebug
  ```

### 4. Como Rodar os Testes Automatizados e Análise Estática

Todos os **27 testes** (ViewModel com Turbine, Interface Compose, Repositório Remoto com `MockEngine`, Cache Local com DataStore, Módulo de DI e Regras de Domínio) rodam no alvo Desktop sem precisar de emulador:

```bash
./gradlew :composeApp:desktopTest
```

Para executar a verificação de estilo e análise estática (`ktlint` + `detekt`):

```bash
./gradlew ktlintCheck detekt
```

---

## Demonstração de Recursos

### Deep Link
O aplicativo suporta deep links no esquema `smorg://equipamento/{id}`. Para testar no emulador ou dispositivo Android via terminal:
```bash
adb shell am start -a android.intent.action.VIEW -d "smorg://equipamento/ID_DO_ITEM"
```

### Layout Adaptativo (2 Larguras)
* **Largura compacta (< 600dp, smartphone em pé):** exibe a lista de equipamentos em tela cheia com navegação empilhada para a tela de detalhes.
* **Largura expandida (>= 600dp, smartphone na horizontal, tablet, desktop):** exibe lista e detalhes lado a lado em duas colunas com `VerticalDivider`, aproveitando o espaço útil sem overflow.

### Operação Offline (Cache Local)
1. Abra o aplicativo conectado à internet para carregar os equipamentos da API (os dados são salvos automaticamente via DataStore em `filesDir` no Android ou `~/.smorg-mobile` no Desktop).
2. Feche o aplicativo e desligue a rede (Modo Avião no Android ou parada da API).
3. Abra o aplicativo novamente: a última lista salva será exibida imediatamente com o aviso *"Sem conexão: mostrando a última lista salva no aparelho."*

---

## Checklist de Avaliação (Rúbricas)

### Sprint 0 — Fundação, Proposta e CI
- [x] Repositório público, app compila e roda nos alvos Android e Desktop
- [x] CI verde no GitHub Actions (`ktlintCheck` + `detekt`)
- [x] `docs/proposta.md` completo com justificativa de plataforma e backend
- [x] Coorte e integração com Sistemas Web II declaradas
- [x] Vídeo de 5 minutos publicado

### Sprint 1 — Interface e Navegação
- [x] Todas as telas principais do MVP implementadas em Compose
- [x] Navigation Compose: rotas tipadas, argumentos e deep link funcional (`smorg://equipamento/{id}`)
- [x] Tema Material 3 completo com modo claro e escuro coerente
- [x] Layout adaptado a 2 larguras de janela (compacto e lado a lado expandido)
- [x] Acessibilidade: descrições de conteúdo, contraste, alvos >= 48dp e semântica com `heading()`
- [x] Formulário de Login com validações de entrada e teclados específicos
- [x] Vídeo de 5 minutos

### Sprint 2 (Final) — Estado, Rede, Persistência e Testes
- [x] Estado com ViewModel multiplataforma (`CatalogViewModel`, `LoginViewModel`) e `StateFlow`
- [x] Camadas `data`, `domain` e `presentation` separadas em `commonMain`; domínio sem import de Compose, Ktor ou DataStore
- [x] Estados `Loading`, `Error`, `Empty` e `Success` modelados por interface selada com `when` sem `else`
- [x] Dados reais do backend escolhido via Ktor Client com `EquipamentoDto` separado da entidade de domínio
- [x] Falha de conexão (`NoConnection`) e resposta inválida (`InvalidResponse`) com estado e mensagem próprios na tela
- [x] Dados do usuário sobrevivem ao fechamento do app (`DataStore`) e abrem sem rede com aviso de cache
- [x] >= 5 testes de interface (`ScreensUiTest`) e >= 5 testes de ViewModel com Turbine (`CatalogViewModelTest`), verdes no CI
- [x] `docs/arquitetura.md`, `README.md` atualizado e APK de debug gerado
- [ ] Vídeo 5 min
