# Issue #6: Autenticação de Usuário e Armazenamento Seguro de Token

- **Título:** `[US06] Autenticação de usuário com matrícula e armazenamento seguro de token`
- **Tipo:** `type:user-story`
- **Prioridade:** `prio:P3`
- **Estimativa:** `5 pts`
- **Sprint:** `sprint:sprint-3`
- **Componentes:** `component:compose-ui`, `component:network-ktor`

---

## 👤 Descrição da História
- **Como** aluno ou monitor
- **Quero** autenticar-me com minha matrícula e senha no aplicativo móvel
- **Para que** minhas operações de empréstimo sejam devidamente auditadas e meu token de sessão fique protegido contra vazamentos no dispositivo

---

## 📋 Contexto e Experiência do Usuário (Mobile UX)

1. **Formulário com Validação de Entrada (Rúbrica Sprint 1 & 3):**
   - Campo matrícula: validação de formato (11 dígitos numéricos da UFRN, ex: `20240027427`).
   - Campo senha: mínimo 6 caracteres com máscara de visualização (ícone de olho).
2. **Armazenamento Seguro de Credenciais:**
   - O token JWT recebido no login não pode ser salvo em texto puro no `SharedPreferences` padrão.
   - Utilizar o Android Keystore / `EncryptedSharedPreferences` via abstração `expect`/`actual` em KMP.
3. **Interceptor Automático de Autorização no Ktor Client:**
   - Inserir cabeçalho `Authorization: Bearer <token>` em todas as requisições protegidas.
   - Tratamento de erro 401 Unauthorized com redirecionamento automático para a tela de login.

---

## 🎯 Critérios de Aceitação (BDD / Given-When-Then)

### Cenário 1: Login com credenciais válidas
- **Dado** que o aluno preenche matrícula e senha válidas
- **Quando** ele pressiona o botão "Entrar"
- **Então** o app realiza a requisição de login
- **E** salva o token JWT de forma criptografada no dispositivo
- **E** redireciona o usuário para a tela inicial do sistema

### Cenário 2: Validação de campo em tempo real
- **Dado** que o usuário digita uma matrícula com menos de 11 dígitos ou caracteres inválidos
- **Quando** o campo perde o foco ou é submetido
- **Então** o app exibe mensagem de erro abaixo do campo indicando o formato esperado

### Cenário 3: Expiração de sessão (401 Unauthorized)
- **Dado** um token JWT expirado
- **Quando** o app recebe resposta 401 da API
- **Então** o token local é limpo com segurança e o usuário é redirecionado para a tela de login com mensagem explicativa

---

## 🛠️ Tarefas Técnicas de Implementação (Checklist)
- [ ] Construir tela Compose `LoginScreen` com validações de formulário Material 3 (`OutlinedTextField`).
- [ ] Implementar interface `TokenStorage` no módulo `commonMain` com implementação nativa `EncryptedTokenStorage` no Android.
- [ ] Configurar plugin `Auth` do Ktor Client com suporte a `BearerTokens`.
- [ ] Implementar fluxo de Logout e limpeza de estado seguro.
- [ ] Escrever testes de validação de formulário e testes de ViewModel com Turbine.

---

## 🏁 Definição de Pronto (Definition of Done - DoD)
- [ ] Tela de login funcional com validação visual de campos.
- [ ] Tokens JWT armazenados com criptografia via Keystore.
- [ ] Interceptor de autenticação ativo nas chamadas de rede da API.
