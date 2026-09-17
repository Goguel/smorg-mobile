# Backlog de Histórias de Usuário — SmOrg Mobile

Este diretório contém os detalhamentos completos das histórias de usuário (User Stories) e especificações de tela/arquitetura cadastradas no [GitHub Project #3 (Kanban do Mobile)](https://github.com/users/Goguel/projects/3) e na aba [Issues](https://github.com/Goguel/smorg-mobile/issues).

---

## 📊 Matriz do Backlog

| # | Título | Tipo | Prioridade | Estimativa | Sprint | Status |
|:---:|---|:---:|:---:|:---:|:---:|:---:|
| **#1** | [Leitura e Identificação via NFC](./issue-1-leitura-identificacao-nfc.md) | User Story | **P1** | 5 pts | Sprint 1 | Pronto p/ Refinamento |
| **#2** | [Confirmar Retirada via NFC](./issue-2-confirmar-retirada.md) | User Story | **P1** | 5 pts | Sprint 1 | Pronto p/ Refinamento |
| **#3** | [Registrar Devolução via NFC](./issue-3-registrar-devolucao.md) | User Story | **P1** | 3 pts | Sprint 1 | Pronto p/ Refinamento |
| **#4** | [Meus Empréstimos Ativos e Prazos](./issue-4-meus-emprestimos-ativos.md) | User Story | **P1** | 5 pts | Sprint 1 | Pronto p/ Refinamento |
| **#5** | [Catálogo e Busca Geral](./issue-5-catalogo-e-busca.md) | User Story | **P2** | 3 pts | Sprint 1 | Pronto p/ Refinamento |
| **#6** | [Autenticação e Armazenamento Seguro](./issue-6-autenticacao-jwt.md) | User Story | **P3** | 5 pts | Sprint 3 | Pronto p/ Refinamento |
| **#7** | [Persistência Offline-First e Sincronização](./issue-7-offline-first-cache.md) | User Story | **P2** | 8 pts | Sprint 3 | Pronto p/ Refinamento |

---

## 🏷️ Convenção de Labels do Projeto Mobile

| Label | Cor | Descrição |
|---|:---:|---|
| `prio:P1` | `#B60205` | Alta prioridade (essencial para o MVP) |
| `prio:P2` | `#D93F0B` | Média prioridade (gestão e experiência offline) |
| `prio:P3` | `#FBCA04` | Baixa prioridade / segurança de longo prazo |
| `sprint:sprint-1` | `#0E8A16` | Tarefas da Sprint 1 (Interface e Navegação) |
| `sprint:sprint-2` | `#1D76DB` | Tarefas da Sprint 2 (Estado e Arquitetura) |
| `sprint:sprint-3` | `#5319E7` | Tarefas da Sprint 3 (Rede, Dados e Offline-First) |
| `type:user-story` | `#0075CA` | História de Usuário (User Story) |
| `type:tech-task` | `#A2EEEF` | Tarefa técnica de arquitetura, Koin ou CI/CD |
| `type:ui-ux` | `#E99695` | Telas Compose, Material 3 e Acessibilidade |
| `component:compose-ui` | `#C5DEF5` | Telas e componentes em Compose Multiplatform |
| `component:navigation` | `#D4C5F9` | Navigation Compose com rotas tipadas |
| `component:nfc-native` | `#BFDADC` | Integração de hardware NFC via expect/actual |
| `component:network-ktor` | `#F9D0C4` | Ktor Client consumindo a SmOrg API |
| `component:offline-cache` | `#FEF2C0` | Banco local (Room/SQLDelight) e offline-first |

---

## 🎯 Milestones Sugeridas

1. **Sprint 1 — Interface e Navegação:** Telas em Compose Material 3 (modo claro/escuro), rotas tipadas no Navigation Compose, layout adaptativo e testes de UI.
2. **Sprint 2 — Estado e Arquitetura:** ViewModels multiplataforma com StateFlow, arquitetura em camadas (`commonMain`), injeção com Koin e testes com Turbine.
3. **Sprint 3 — Rede, Dados e Offline-First:** Integração real com a [smorg-api](https://github.com/Goguel/smorg-api), banco local (Room/SQLDelight), cache offline e autenticação.
4. **Sprint Final — Recursos Nativos, Segurança e Distribuição:** NFC em dispositivo real, relatório OWASP Mobile, otimização de recomposição e pipeline de release APK.
