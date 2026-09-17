# Issue #1: Leitura e Identificação de Equipamentos via Aproximação NFC

- **Título:** `[US01] Leitura e identificação de equipamentos via aproximação NFC`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P1`
- **Estimativa:** `5 pts`
- **Sprint:** `sprint:sprint-1`
- **Componentes:** `component:nfc-native`, `component:compose-ui`

---

## 👤 Descrição da História
- **Como** aluno ou monitor no laboratório
- **Quero** aproximar meu smartphone da tag NFC fixada em um equipamento físico
- **Para que** o aplicativo reconheça instantaneamente o ativo e exiba seu nome, modelo, categoria e status de disponibilidade sem necessidade de digitação

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Atrito Zero:** O núcleo de usabilidade do SmOrg é eliminar digitação de códigos de patrimônio em cadernos ou telas.
2. **Reconhecimento em Primeiro Plano (Foreground Dispatch):** Ao manter o app aberto e encostar o celular na tag NFC (NfcA/IsoDep/NDEF), o sistema intercepta o evento sem abrir o app nativo de tags do Android.
3. **Feedback Imediato Multi-sensorial:** A leitura bem-sucedida deve disparar:
   - Resposta háptica (vibração curta via `HapticFeedback`).
   - Transição suave na tela para o Card de Detalhes do Equipamento.
4. **Tratamento de Exceções:**
   - Tag física não cadastrada na base: Exibir Snackbar de erro informativo ("Tag NFC não reconhecida no laboratório").
   - NFC desativado no aparelho: Exibir banner com botão para abrir as Configurações do Android.

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Leitura de tag NFC válida com sucesso
- **Dado** que o app está aberto na tela inicial ou na tela de escaneamento
- **Quando** o usuário aproxima o celular de um equipamento com tag `04A1B2C3D4`
- **Então** o app emite uma vibração tátil de confirmação
- **E** exibe os detalhes do item: Nome, Categoria e Badge de status ("Disponível" em verde ou "Emprestado" em amarelo)

### Cenário 2: Leitura de tag NFC não cadastrada
- **Dado** que o usuário aproxima o celular de uma tag não registrada no SmOrg
- **Quando** a leitura for realizada
- **Então** o app exibe uma mensagem de erro em destaque informando que a tag não pertence ao acervo do laboratório

### Cenário 3: Aparelho com NFC desativado
- **Dado** que o usuário abre a funcionalidade de leitura com o sensor NFC desligado nas configurações do sistema
- **Quando** a tela de leitura é carregada
- **Então** o app exibe um alerta visual com ação rápida para habilitar o NFC nas configurações do dispositivo

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Criar abstração de leitor NFC em `commonMain` com `expect`/`actual` para Android (`NfcAdapter`).
- [ ] Configurar permissão `<uses-permission android:name="android.permission.NFC" />` no `AndroidManifest.xml`.
- [ ] Implementar ativação do `enableForegroundDispatch` no ciclo de vida da Activity Android.
- [ ] Criar componente Compose `NfcScannerOverlay` com animação de radar/onda indicando prontidão para leitura.
- [ ] Criar tela/bottom sheet `EquipmentDetailSheet` para exibição reativa dos dados lidos.
- [ ] Integrar feedback tátil utilizando `LocalHapticFeedback.current`.
- [ ] Escrever testes de UI com Compose Test verificando a renderização do estado pós-leitura.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Leitura por aproximação NFC funcionando em aparelho físico Android.
- [ ] Feedback tátil e sonoro integrado.
- [ ] Layout Material 3 adaptado a tema claro e escuro.
- [ ] Teste de interface Compose passando no CI.
