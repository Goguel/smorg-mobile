# SmOrg (Smart Organization) â€” Mobile

RepositÃ³rio da aplicaÃ§Ã£o cliente mÃ³vel do sistema **SmOrg**, um ecossistema de gestÃ£o de ativos fÃ­sicos e controle de emprÃ©stimos por aproximaÃ§Ã£o (**NFC**) para laboratÃ³rios acadÃªmicos, projetado para oferecer uma experiÃªncia de "atrito zero".

Desenvolvido para a disciplina **DIM0524 â€” Desenvolvimento de Sistemas para Dispositivos MÃ³veis** (UFRN / DIMAp).

O documento detalhado com visÃ£o do produto, MVP, backlog e decisÃµes tÃ©cnicas encontra-se em [`docs/proposta.md`](docs/proposta.md).

---

## Equipe e InformaÃ§Ãµes AcadÃªmicas

* **Aluno:** Miguel Xavier de Morais (MatrÃ­cula: `20240027427`)
* **Coorte de ApresentaÃ§Ã£o:** Coorte A (Presencial)
* **IntegraÃ§Ã£o Declarada:** Este projeto estÃ¡ formalmente integrado com a disciplina **DIM0547 â€” Desenvolvimento de Sistemas Web II**. O backend consumido por este aplicativo mÃ³vel Ã© o [SmOrg API](https://github.com/Goguel/smorg-api), construÃ­do em Kotlin (Ktor) e Go (gRPC).

---

## Arquitetura e Tecnologias

A aplicaÃ§Ã£o mÃ³vel adota **Kotlin Multiplatform (KMP)** e **Compose Multiplatform**, permitindo o compartilhamento integral da lÃ³gica de negÃ³cios e da interface grÃ¡fica declarativa em `commonMain`:

* **Linguagem:** Kotlin
* **Interface Declarativa:** Compose Multiplatform com Material 3 (suporte a modo claro/escuro e layout adaptativo)
* **NavegaÃ§Ã£o:** Navigation Compose com rotas tipadas, passagem de argumentos e deep links
* **Gerenciamento de Estado:** ViewModel multiplataforma com `StateFlow` e modelagem de UI por interfaces seladas
* **InjeÃ§Ã£o de DependÃªncias:** Koin em `commonMain`
* **ComunicaÃ§Ã£o de Rede:** Ktor Client + `kotlinx.serialization` consumindo a [SmOrg API](https://github.com/Goguel/smorg-api)
* **PersistÃªncia Local e Cache Offline:** Room / SQLDelight com esquema versionado e polÃ­tica offline-first
* **Recursos Nativos:** Suporte a NFC e hardware via abstraÃ§Ãµes `expect`/`actual`
* **Testes e Qualidade:** `kotlin.test`, Turbine (Flows), Compose UI Tests, `ktlint` e `detekt`

---

## Como Executar Localmente

### PrÃ©-requisitos
* **JDK 17** ou superior instalado e configurado no `JAVA_HOME`.
* **Android Studio** atualizado com o plugin *Kotlin Multiplatform*.
* Dispositivo fÃ­sico Android conectado via USB (com depuraÃ§Ã£o ativada) ou emulador Android configurado.

### ExecuÃ§Ã£o no Desktop (Ciclo RÃ¡pido com Hot Reload)
Para desenvolvimento Ã¡gil de interfaces sem a necessidade de iniciar o emulador:
```bash
./gradlew :composeApp:run
```

### ExecuÃ§Ã£o no Android
Via linha de comando:
```bash
./gradlew :composeApp:installDebug
```
Ou selecione a configuraÃ§Ã£o **composeApp** no Android Studio e clique em **Run â–¶**.

### AnÃ¡lise EstÃ¡tica de CÃ³digo (Qualidade)
Para conferir localmente as regras de estilo e lint antes do commit:
```bash
./gradlew ktlintCheck detekt
```

---

---

## Demonstração de Recursos da Sprint 1

### Deep Link
O aplicativo suporta deep links no esquema `smorg://equipamento/{id}`. Para testar no emulador ou dispositivo via terminal:
```bash
adb shell am start -a android.intent.action.VIEW -d "smorg://equipamento/ID_DO_ITEM"
```

### Layout Adaptativo (2 Larguras)
* **Largura compacta (< 600dp, smartphone em pé):** exibe a lista de equipamentos em tela cheia com navegação empilhada para a tela de detalhes.
* **Largura expandida (>= 600dp, smartphone na horizontal, tablet, desktop):** exibe lista e detalhes lado a lado em duas colunas com `VerticalDivider`, aproveitando o espaço útil sem rolagem horizontal ou overflow.

### Acessibilidade e Tema
* **Tema Material 3:** alternância dinâmica entre modo claro e escuro (`isSystemInDarkTheme()`).
* **Acessibilidade:** descrições de conteúdo em elementos visuais, títulos marcados com `heading()` para leitores de tela (TalkBack) e alvos de toque >= 48dp.

---

## Checklist de Avaliação (Rúbricas Atualizadas)

### Sprint 0 — Fundação, Proposta e CI
- [x] Repositório público, app compila e roda nos alvos Android e Desktop
- [x] CI verde no GitHub Actions (`ktlintCheck` + `detekt`)
- [x] `docs/proposta.md` completo com justificativa de plataforma e backend
- [x] Coorte (A - Presencial) e integração declaradas
- [x] Vídeo de 5 minutos publicado

### Sprint 1 — Interface e Navegação
- [x] Todas as telas principais do MVP implementadas em Compose
- [x] Navigation Compose: rotas tipadas, argumentos e deep link funcional (`smorg://equipamento/{id}`)
- [x] Tema Material 3 completo com modo claro e escuro coerente
- [x] Layout adaptado a 2 larguras de janela (compacto e lado a lado expandido)
- [x] Acessibilidade: descrições de conteúdo, contraste, alvos >= 48dp e semântica com `heading()`
- [x] Formulário de Login com validações de entrada e teclados específicos
- [ ] Vídeo de 5 minutos: [Link do Vídeo](URL_DO_VIDEO_AQUI)

### Sprint 2 (Final) — Estado, Rede, Persistência e Testes
- [ ] Estado gerenciado com ViewModel multiplataforma e `StateFlow`
- [ ] Arquitetura em camadas (`data`, `domain`, `presentation`) em `commonMain`
- [ ] Estados da interface modelados por classes seladas (Loading, Success, Empty, Error)
- [ ] Dados reais do backend via Ktor Client com DTOs desacoplados do domínio
- [ ] Tratamento de falha de conexão e resposta inválida com estado de tela próprio
- [ ] Persistência local (os dados sobrevivem ao fechamento do app e abrem sem rede)
- [ ] >= 5 testes de interface e >= 5 testes de ViewModel com Turbine, verdes no CI
- [ ] `docs/arquitetura.md`, README e APK de debug anexado a uma release
- [ ] Vídeo 5 min