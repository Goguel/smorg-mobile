# Issue #4: Visualizar Meus Empréstimos Ativos e Prazos de Devolução

- **Título:** `[US04] Visualizar equipamentos sob custódia e prazos de devolução`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P1`
- **Estimativa:** `5 pts`
- **Sprint:** `sprint:sprint-1`
- **Componentes:** `component:compose-ui`, `component:navigation`

---

## 👤 Descrição da História
- **Como** aluno do laboratório
- **Quero** acessar uma tela que liste todos os equipamentos atualmente sob minha responsabilidade
- **Para que** eu acompanhe a data limite de devolução de cada item e evite atrasos ou bloqueios

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Visão Central de Custódia:** Uma das abas principais da barra de navegação inferior (`NavigationBar`) deve ser "Meus Empréstimos".
2. **Alertas Visuais Baseados em Tempo:**
   - **Verde:** Prazo restante > 24 horas.
   - **Amarelo / Âmbar:** Menos de 24 horas para expirar ("Vence hoje!").
   - **Vermelho:** Prazo expirado ("Em atraso - Devolva imediatamente").
3. **Ações Rápidas no Item:**
   - Toque no card abre a tela de detalhes com informações do equipamento e botão "Devolver via NFC".
4. **Estados de Tela:**
   - *Loading:* Shimmer effect ou indicador de progresso.
   - *Empty:* Ilustração/ícone indicando "Nenhum equipamento em posse no momento".
   - *Error:* Mensagem de erro de rede com botão "Tentar novamente".
   - *Pull-to-refresh:* Atualização manual da lista puxando a tela para baixo.

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Listagem de empréstimos com prazos destacados
- **Dado** que o aluno possui 2 equipamentos sob sua posse
- **Quando** ele abre a aba "Meus Empréstimos"
- **Então** o app renderiza os 2 cards com os respectivos nomes dos equipamentos
- **E** exibe a data limite formatada com destaque de cor de acordo com a proximidade do vencimento

### Cenário 2: Usuário sem nenhum empréstimo ativo
- **Dado** que o usuário devolveu todos os seus materiais
- **Quando** ele acessar a tela "Meus Empréstimos"
- **Então** o app exibe o estado vazio amigável com mensagem explicativa

### Cenário 3: Atualização via Pull-to-refresh
- **Dado** que a lista de empréstimos está exibida na tela
- **Quando** o usuário faz o gesto de pull-to-refresh
- **Então** o app revalida os dados junto à API e atualiza os cards

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Implementar rota tipada `@Serializable data object MeusEmprestimosRoute` no Navigation Compose.
- [ ] Criar ViewModel `MeusEmprestimosViewModel` com `StateFlow<MeusEmprestimosUiState>`.
- [ ] Construir componente Compose `EmprestimoCard` com suporte a temas Material 3 e alvos de toque >= 48dp.
- [ ] Implementar componente de estado vazio (`EmptyStateView`) e tratamento de pull-to-refresh (`pullRefresh` / `Modifier`).
- [ ] Testes de ViewModel com Turbine validando transições de estado.
- [ ] Teste de interface com Compose Test validando a listagem de múltiplos cards e cores semânticas.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Tela "Meus Empréstimos" navegável com rotas tipadas.
- [ ] Cores semânticas de alerta de prazo funcionando conforme o tempo restante.
- [ ] Testes de interface e ViewModel verdes no CI.
