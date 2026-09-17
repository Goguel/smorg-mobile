# Issue #5: Catálogo e Busca Geral de Equipamentos do Laboratório

- **Título:** `[US05] Catálogo e busca geral de equipamentos do laboratório`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P2`
- **Estimativa:** `3 pts`
- **Sprint:** `sprint:sprint-1`
- **Componentes:** `component:compose-ui`, `component:navigation`

---

## 👤 Descrição da História
- **Como** aluno ou monitor
- **Quero** consultar o catálogo completo de equipamentos com filtros por categoria e status
- **Para que** eu encontre os materiais disponíveis antes mesmo de ir até o armário ou bancada do laboratório

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Acesso Rápido ao Acervo:** Uma aba de catálogo que apresenta todos os bens catalogados no laboratório.
2. **Filtros por Chips (FilterChip Material 3):**
   - Status: *Todos*, *Apenas Disponíveis*, *Em Uso*.
   - Categorias: *Eletrônica*, *Microcontroladores*, *Ferramentas*, *Cabos*.
3. **Barra de Busca Dinâmica (`SearchBar` / `DockedSearchBar`):**
   - Filtragem instantânea conforme o usuário digita (debounce de 300ms).
4. **Layout Adaptativo (Rúbrica Sprint 1):**
   - Em smartphones (tela compacta): Exibição em lista vertical (`LazyColumn`).
   - Em telas maiores (tablets ou desktop JVM): Exibição em grade adaptativa (`LazyVerticalGrid`).

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Filtragem por status "Disponível"
- **Dado** que o catálogo contém itens com status diversos
- **Quando** o usuário seleciona o chip "Disponíveis"
- **Então** apenas os equipamentos livres para empréstimo são listados

### Cenário 2: Busca por termo textual
- **Dado** que o usuário digita "Osciloscópio" na barra de busca
- **Quando** o texto for processado
- **Então** o app filtra a lista trazendo apenas itens cujo nome corresponda ao termo

### Cenário 3: Navegação para a tela de detalhes
- **Dado** a lista de equipamentos exibida
- **Quando** o usuário clica em um item
- **Então** o app navega para a rota de detalhes passando o ID do equipamento como argumento tipado

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Implementar rota tipada `@Serializable data object CatalogoRoute` e `@Serializable data class DetalheEquipamentoRoute(val equipamentoId: String)`.
- [ ] Construir tela `CatalogoScreen` com `LazyColumn` e `LazyVerticalGrid` adaptativo dependendo do `WindowWidthSizeClass`.
- [ ] Implementar `FilterChip` do Material 3 para alternar categorias.
- [ ] Criar ViewModel `CatalogoViewModel` gerenciando os filtros e o estado de busca reativo.
- [ ] Garantir acessibilidade com `contentDescription` em todas as imagens/ícones e alvos de toque >= 48dp.
- [ ] Testes de UI validando a filtragem por chips e digitação na busca.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Catálogo navegável com busca dinâmica e filtros por chip.
- [ ] Layout adaptado para tela compacta (celular) e expandida (tablet/desktop).
- [ ] Testes de interface passando no CI.
