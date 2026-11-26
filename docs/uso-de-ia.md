# Registro de Uso de Ferramentas de Inteligência Artificial

**Disciplina:** DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis  
**Projeto:** SmOrg Mobile  
**Aluno:** Miguel Xavier de Morais (`20240027427`)

Conforme estabelecido na Sistemática de Avaliação, este documento registra a transparência no uso de ferramentas de inteligência artificial durante o ciclo de vida do projeto.

---

## Sprint 0

| Data | Ferramenta / Modelo | Tarefa / Finalidade | Contexto e Descrição |
|---|---|---|---|
| 13/09/2026 | Google Antigravity (Gemini 3.8 Flash) | Análise de Rúbricas e Elaboração da Proposta | Leitura das diretrizes e cronograma da disciplina em sistemas-moveis-2026-2, mapeamento da integração com a API smorg-api (DIM0547) e estruturação dos documentos docs/proposta.md e README.md. |

---

## Sprint 1

| Data | Ferramenta / Modelo | Tarefa / Finalidade | Contexto e Descrição |
|---|---|---|---|
| 16/10/2026 | Google Antigravity (Gemini) | Deep Links e Material 3 Theming | Configuração do intent-filter Android e navDeepLink no Compose Navigation (smorg://equipamento/{id}); criação da paleta de cores e SmOrgTheme com suporte dinâmico a Light/Dark Mode via isSystemInDarkTheme(). |
| 16/10/2026 | Google Antigravity (Gemini) | Layout Adaptativo e Acessibilidade | Implementação de layout responsivo com BoxWithConstraints (Master-Detail em telas >= 600dp e navegação em pilha em compactas); inclusão de Modifier.semantics { heading() }, contentDescription acessíveis e touch targets >= 48dp. |
| 16/10/2026 | Google Antigravity (Gemini) | Análise de Conformidade e Rúbricas | Auditoria dos critérios de avaliação do professor para Sprint 1 (30% Navegação/Deep Links, 20% UI/Temas/Layout adaptativo, 20% Acessibilidade, 15% Arquitetura, 15% Qualidade/Lints) e atualização do README. |

---

## Sprint 2 (Entrega Final)

| Data | Ferramenta / Modelo | Tarefa / Finalidade | Contexto e Descrição |
|---|---|---|---|
| 13/11/2026 | Google Antigravity (Gemini) | Arquitetura em Camadas e Injeção de Dependências (T4) | Separação do código em pacotes `domain`, `data` e `presentation` em `commonMain`, garantindo domínio puro sem dependências de infraestrutura, criação da interface `EquipmentRepository` e montagem na raiz de composição `AppModule.kt`. |
| 25/11/2026 | Google Antigravity (Gemini) | ViewModels, Estados Selados e Tratamento de Rede (T2, T3, T5, T6) | Implementação de `CatalogViewModel` e `LoginViewModel` com `StateFlow` e interfaces seladas (`CatalogScreenState`, `DetailScreenState`), definição de endereço da API via `expect`/`actual` e tradução de exceções do Ktor em `RepositoryFailure.NoConnection` e `RepositoryFailure.InvalidResponse`. |
| 25/11/2026 | Google Antigravity (Gemini) | Cache Local com DataStore, Testes Automatizados e Documentação (T7, T8, T9, T10) | Implementação de `CachedEquipmentRepository` com DataStore Preferences multiplataforma; escrita de 27 testes automatizados (`CatalogViewModelTest` com Turbine, `ScreensUiTest` com `runComposeUiTest`, `RemoteEquipmentRepositoryTest` com `MockEngine` e `CachedEquipmentRepositoryTest`); atualização do pipeline GitHub Actions e elaboração de `docs/arquitetura.md`. |

---

## Princípio de Integridade Acadêmica
Todas as saídas geradas por ferramentas de inteligência artificial são minuciosamente revisadas, adaptadas e plenamente compreendidas pelo aluno, assegurando total domínio conceitual e técnico sobre a arquitetura e implementação submetidas.
