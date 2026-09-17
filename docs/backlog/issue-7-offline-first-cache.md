# Issue #7: Persistência Local Offline-First e Sincronização de Dados

- **Título:** `[US07] Persistência local offline-first e sincronização de dados`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P2`
- **Estimativa:** `8 pts`
- **Sprint:** `sprint:sprint-3`
- **Componentes:** `component:offline-cache`, `component:network-ktor`

---

## 👤 Descrição da História
- **Como** aluno ou monitor
- **Quero** que o aplicativo funcione e armazene dados localmente mesmo em ambientes do laboratório com conexão Wi-Fi fraca ou inexistente
- **Para que** eu possa consultar meus equipamentos sob custódia e registrar leituras sem ser impedido por falhas momentâneas de rede

---

## 📋 Contexto e Arquitetura Offline-First (Rúbrica Sprint 3)

1. **Fonte Única da Verdade Local (Single Source of Truth):**
   - As telas do aplicativo sempre observam o banco de dados local (Room / SQLDelight) através de `Flow`.
   - As chamadas de rede atualizam o banco de dados local, que por sua vez dispara a recomposição reativa da interface.
2. **Esquema de Dados Versionado:**
   - Tabelas locais: `equipamentos`, `emprestimos` e `sync_queue` (fila de sincronização).
3. **Fila de Escrita Pendente:**
   - Leituras e ações de empréstimo/devolução tentadas sem conectividade são enfileiradas localmente com status `PENDENTE`.
   - Ao restabelecer a conexão de rede, um Worker ou corrotina em segundo plano descarrega a fila na SmOrg API.
4. **Política Declarada de Resolução de Conflitos:**
   - Para status de equipamentos: O servidor detém a autoridade final (*Server Authority*). Caso um item tenha sido alterado remotamente, o app atualiza o registro local e alerta o usuário.

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Consulta offline do catálogo
- **Dado** que o aplicativo sincronizou o catálogo anteriormente
- **Quando** o usuário abre o aplicativo sem conexão de internet (Modo Avião)
- **Então** o app carrega e exibe os equipamentos imediatamente a partir do banco de dados local
- **E** exibe um discreto indicador de "Modo Offline"

### Cenário 2: Enfileiramento de ação pendente
- **Dado** que o dispositivo está offline
- **Quando** o usuário registra a leitura de uma devolução
- **Então** o app grava a operação na fila de sincronização local com status `PENDENTE`
- **E** informa que a ação será transmitida assim que a conexão retornar

### Cenário 3: Sincronização automática ao reconectar
- **Dado** que há ações pendentes na fila local
- **Quando** a conectividade de rede for restabelecida
- **Então** o app envia sequencialmente os dados pendentes para o backend
- **E** limpa os itens concluídos da fila de sincronização

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Configurar Room ou SQLDelight com esquema versionado no módulo `commonMain`.
- [ ] Criar entidades e DAOs para `EquipmentEntity` e `LoanEntity`.
- [ ] Implementar padrão Repository Offline-First (`offline-first repository pattern`) emitindo `Flow`.
- [ ] Criar monitor de conectividade de rede (`NetworkMonitor`).
- [ ] Criar gerenciador de sincronização em segundo plano (`SyncManager`).
- [ ] Documentar política de resolução de conflitos em `docs/offline.md`.
- [ ] Escrever testes de integração do banco local e testes de sincronização com mocks de rede.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Aplicativo navegável e funcional em modo offline.
- [ ] Fila de sincronização gravando ações e descarregando na reconexão.
- [ ] Documento `docs/offline.md` criado com a política de conflitos.
- [ ] Testes de repositório e persistência passando no CI.
