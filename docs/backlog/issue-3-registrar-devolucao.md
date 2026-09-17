# Issue #3: Registrar Devolução de Equipamento via NFC

- **Título:** `[US03] Registrar devolução de equipamento via leitura NFC`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P1`
- **Estimativa:** `3 pts`
- **Sprint:** `sprint:sprint-1`
- **Componentes:** `component:compose-ui`, `component:network-ktor`

---

## 👤 Descrição da História
- **Como** monitor ou aluno do laboratório
- **Quero** registrar a devolução de um equipamento através da leitura da tag NFC
- **Para que** a custódia do item seja encerrada, a pendência seja baixada e o item retorne imediatamente ao status de disponível no acervo

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Ação Rápida de Balcão:** O monitor do laboratório pode manter o app na tela "Modo Devolução" e simplesmente aproximar os equipamentos devolvidos em sequência.
2. **Identificação de Status de Pontualidade:**
   - Devolução no prazo: Exibir badge verde "Devolvido com Sucesso".
   - Devolução com atraso: Exibir badge âmbar/vermelho indicando "Devolvido com X dias de atraso".
3. **Validação de Equipamento não Emprestado:** Se a tag lida pertencer a um equipamento que já consta como `DISPONIVEL`, o app deve informar que o item não possui empréstimo ativo em aberto.

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Devolução no prazo com sucesso
- **Dado** que o item sob custódia é aproximado do celular
- **Quando** a leitura da tag NFC for confirmada
- **Então** o app envia `POST /api/v1/emprestimos/devolucao`
- **E** exibe mensagem de sucesso confirmando que o empréstimo foi encerrado no prazo
- **E** o status do equipamento passa para "Disponível"

### Cenário 2: Devolução com atraso registrado
- **Dado** que o equipamento devolvido ultrapassou a data limite
- **Quando** a devolução for processada
- **Então** o app exibe confirmação com aviso de devolução fora do prazo com a contagem de dias

### Cenário 3: Item sem empréstimo em aberto
- **Dado** um equipamento que já consta como disponível
- **Quando** for feita a tentativa de devolução
- **Então** o app exibe aviso informando que o item não possui pendência ativa

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Criar caso de uso `RegistrarDevolucaoUseCase` em `commonMain`.
- [ ] Construir componente Compose `DevolucaoResultCard` com suporte a estados de pontualidade.
- [ ] Integrar chamada de API Ktor para a rota `/api/v1/emprestimos/devolucao`.
- [ ] Testes unitários do caso de uso e ViewModel com Turbine.
- [ ] Testes de tela Compose cobrindo a renderização de devolução pontual vs. com atraso.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Fluxo de devolução por leitura NFC concluído e validado.
- [ ] Tratamento visual de devolução pontual e com atraso implementado.
- [ ] Testes unitários e de UI passando no CI.
