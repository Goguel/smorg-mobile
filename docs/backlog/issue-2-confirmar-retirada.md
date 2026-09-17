# Issue #2: Confirmar Retirada e Empréstimo de Equipamento via NFC

- **Título:** `[US02] Confirmar retirada e empréstimo de equipamento via aproximação`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P1`
- **Estimativa:** `5 pts`
- **Sprint:** `sprint:sprint-1`
- **Componentes:** `component:compose-ui`, `component:network-ktor`

---

## 👤 Descrição da História
- **Como** aluno do laboratório
- **Quero** confirmar o empréstimo de um equipamento disponível após sua leitura por NFC
- **Para que** o ativo seja vinculado formalmente à minha matrícula e eu receba o comprovante com a data limite de devolução

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Confirmação Clara de Custódia:** Após identificar o item via NFC com status `DISPONIVEL`, a tela apresenta o botão primário "Confirmar Retirada".
2. **Exibição do Prazo Previsto:** A resposta do backend (calculada pelo microsserviço Go com base no perfil do usuário e no calendário acadêmico) devolve a data limite exata, que deve ser destacada na tela de sucesso.
3. **Feedback de Bloqueio ou Erro de Negócio:**
   - Se o equipamento já foi emprestado para outro colega: Exibir modal de conflito explicando a situação.
   - Se o aluno possui atrasos pendentes (inadimplência): Exibir aviso claro de bloqueio orientando regularizar a pendência anterior.
4. **Estados de UI Selados:** A tela deve transicionar entre `Idle`, `Loading` (botão desabilitado com indicador circular), `Success` (recibo visual com botão para ver meus empréstimos) e `Error` (mensagem amigável com opção de tentar novamente).

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Confirmação de empréstimo com sucesso
- **Dado** que o aluno realizou a leitura de um item disponível
- **Quando** ele clica em "Confirmar Retirada"
- **Então** o app envia a requisição para `POST /api/v1/emprestimos` no backend
- **E** exibe a tela de confirmação com a data limite de devolução recebida do servidor
- **E** o status visual do item é atualizado para "Emprestado"

### Cenário 2: Equipamento indisponível ou já retirado
- **Dado** que o item foi retirado por outro usuário momentos antes
- **Quando** o aluno tenta confirmar o empréstimo
- **Então** o app exibe mensagem de erro explicando que o equipamento não está mais disponível

### Cenário 3: Aluno com pendências em aberto
- **Dado** que o backend retorna erro `422 (Usuário Bloqueado)`
- **Quando** a tentativa de empréstimo for processada
- **Então** o app exibe um diálogo de aviso instruindo o aluno a devolver o equipamento em atraso

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Criar DTOs de requisição e resposta de empréstimo (`EmprestimoRequestDto`, `EmprestimoResponseDto`) com `kotlinx.serialization`.
- [ ] Implementar caso de uso `ConfirmarRetiradaUseCase` em `commonMain`.
- [ ] Modelar `RetiradaUiState` (Idle, Loading, Success, Error) com classes seladas.
- [ ] Criar tela Compose `ConfirmacaoRetiradaScreen` com Material 3 Card e botão com estado de loading.
- [ ] Testes de ViewModel utilizando a biblioteca Turbine para testar as transições de fluxo de estado.
- [ ] Teste de interface Compose simulando o clique de confirmação.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Fluxo de confirmação de empréstimo funcional na interface.
- [ ] Estados de loading, sucesso e erro tratados com componentes Material 3.
- [ ] Testes de ViewModel passando com Turbine no CI.
