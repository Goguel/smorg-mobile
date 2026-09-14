# Proposta de Projeto: SmOrg (Smart Organization) — Mobile

**Disciplina:** DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis (2026.2)  
**Docente:** Fernando Figueira Filho — UFRN / DIMAp  
**Repositório:** [github.com/Goguel/smorg-mobile](https://github.com/Goguel/smorg-mobile)  
**Projeto Integrado (Backend):** [github.com/Goguel/smorg-api](https://github.com/Goguel/smorg-api) (DIM0547 — Sistemas Web II)

---

## 1. Visão do Produto

**Para** alunos, monitores e professores que utilizam e administram laboratórios universitários  
**Que** sofrem com filas, anotações manuais em papel e lentidão na retirada e devolução de ferramentas e kits didáticos (ex.: multímetros, osciloscópios, kits de microcontroladores)  
**O SmOrg Mobile é um** aplicativo móvel de gestão física de ativos e controle de empréstimos por aproximação  
**Que** permite realizar a retirada, conferência e devolução de equipamentos instantaneamente através da leitura de tags NFC acopladas aos ativos  
**Diferente de** planilhas de papel, cadernos de protocolo ou aplicativos genéricos que exigem digitação de longos números de patrimônio  
**Nosso produto** oferece uma experiência móvel de "atrito zero", onde basta aproximar o smartphone da tag do equipamento para identificar o item, validar prazos com o backend em tempo real e registrar a custódia com confirmação tátil e visual imediata.

---

## 2. Definição do MVP

### Hipótese de Valor
> *Acreditamos que alunos e monitores de laboratório registrarão 100% dos empréstimos e devoluções pelo aplicativo móvel porque a leitura física por NFC associada à resposta instantânea da interface elimina o atrito de digitação e filas na bancada do laboratório.*

### Escopo do MVP (O que entra vs. O que fica fora)

| No MVP | Fora do MVP |
|---|---|
| Autenticação e seleção de perfil (Aluno / Monitor) | Notificações push em segundo plano via Firebase Cloud Messaging |
| Leitura de tags NFC para identificação instantânea de itens | Suporte a leitura ótica/QR Code como fallback |
| Tela de catálogo e consulta rápida do status dos equipamentos do laboratório | Painel administrativo gerencial completo (dashboard analítico) |
| Fluxo de retirada (empréstimo) com validação de regras de devolução | Reserva prévia com fila de espera para itens ocupados |
| Fluxo de devolução com conferência de integridade do equipamento | Geração e exportação de relatórios ou termos de custódia em PDF |
| Histórico local dos itens sob custódia atual do estudante | Suporte a múltiplos laboratórios simultâneos |
| Operação offline com cache para consulta do acervo e fila de sincronização | Auditoria biométrica avançada do usuário |

---

## 3. Backlog Inicial

O backlog e o quadro Kanban integrado do projeto SmOrg estão hospedados no GitHub Projects da organização/usuário:  
**Quadro do Projeto:** [https://github.com/users/Goguel/projects/2](https://github.com/users/Goguel/projects/2)

### Histórias de Usuário Priorizadas

| Prio | História de Usuário | Critérios de Aceitação | Sprint Prevista |
|:---:|---|---|:---:|
| **P1** | **Como aluno**, quero aproximar meu celular da tag NFC de um equipamento para visualizar rapidamente sua identificação, detalhes e disponibilidade. | 1. O app detecta a tag NFC em primeiro plano.<br>2. Exibe o nome, modelo e status atual (Disponível / Ocupado).<br>3. Exibe alerta sonoro/tátil de leitura com sucesso. | Sprint 1 |
| **P1** | **Como aluno**, quero confirmar o empréstimo de um item disponível aproximando o celular para registrá-lo sob minha responsabilidade acadêmica. | 1. O app envia a requisição de empréstimo para a API Ktor.<br>2. Recebe e exibe o prazo final de devolução calculado pelo serviço Go.<br>3. O status na interface atualiza imediatamente para "Emprestado". | Sprint 1 / 2 |
| **P1** | **Como monitor**, quero registrar a devolução de um equipamento através da leitura NFC para liberar o item no inventário do laboratório. | 1. Valida se o item estava sob custódia de algum aluno.<br>2. Chama endpoint de devolução e fecha a transação.<br>3. Altera o status para "Disponível". | Sprint 1 / 2 |
| **P1** | **Como aluno**, quero visualizar a lista dos equipamentos que estão atualmente em minha posse para acompanhar os prazos de entrega. | 1. Tela com lista dos itens emprestados pelo usuário logado.<br>2. Exibição clara da data limite de devolução com destaque visual para prazos próximos. | Sprint 1 |
| **P2** | **Como aluno/monitor**, quero consultar o catálogo geral de equipamentos do laboratório para saber quais materiais estão livres para retirada. | 1. Lista paginada/filtrável de equipamentos por categoria.<br>2. Indicador visual claro de status (Disponível / Em uso).<br>3. Busca textual rápida por nome ou tag. | Sprint 1 |
| **P3** | **Como usuário**, quero autenticar-me com minha matrícula para garantir que os registros de ativos sejam vinculados à minha conta com segurança. | 1. Tela de login com matrícula e senha.<br>2. Armazenamento seguro de token JWT no Keystore seguro da plataforma.<br>3. Interceptor de autorização nas requisições HTTP. | Sprint 3 |
| **P2** | **Como usuário**, quero que o app armazene localmente a lista de equipamentos para permitir consulta rápida mesmo em áreas do laboratório com sinal Wi-Fi instável. | 1. Persistência local (Room / SQLDelight).<br>2. O app abre e lista os itens a partir do cache quando sem internet.<br>3. Sincroniza dados com o backend ao restabelecer a conexão. | Sprint 3 |

---

## 4. Escolha e Justificativa da Plataforma-Alvo

* **Plataforma Prioritária Escolhida:** **Android**
* **Alvo Secundário de Desenvolvimento:** **Desktop (JVM)**

### Justificativa Técnica e de Produto:
1. **Aderência ao Caso de Uso de Hardware (NFC):** A proposta de valor central do SmOrg baseia-se na aproximação física via NFC. O ecossistema Android oferece suporte amplo, aberto e direto à leitura e emulação de tags NFC (NfcAdapter) em aparelhos físicos de entrada e intermediários, amplamente acessíveis aos estudantes da universidade.
2. **Ambiente de Testes e Disponibilidade de Hardware:** O desenvolvimento e os testes físicos serão realizados diretamente em dispositivo físico Android com depuração USB habilitada e no emulador Android oficial do Android Studio.
3. **Alternativas Consideradas e Descartadas:**
   * *iOS como plataforma prioritária:* Foi descartado porque a compilação e teste contínuo de recursos nativos (como NFC NDEF ReaderSession) em iOS exige obrigatoriamente hardware Apple (macOS) e provisionamento via Xcode/Apple Developer Program, os quais não estão disponíveis na infraestrutura de desenvolvimento do desenvolvedor neste semestre.
   * *Web / PWA:* Descartada como prioritária porque o Web NFC possui suporte extremamente restrito e experimental, não operando de forma confiável para o modelo de atrito zero exigido na rotina laboratorial.
4. **Papel do Alvo Desktop:** O alvo Desktop (JVM) é mantido para possibilitar o ciclo rápido de desenvolvimento de telas em Compose com *Hot Reload*, viabilizando prototipação ágil antes de subir o build para o aparelho Android.

---

## 5. Escolha e Justificativa da Estratégia de Backend

* **Estratégia Escolhida:** **Opção C — API Própria de Sistemas Web II (DIM0547)**
* **Repositório da API:** [github.com/Goguel/smorg-api](https://github.com/Goguel/smorg-api)

### Justificativa:
1. **Integração entre Disciplinas (DIM0547 & DIM0524):** O backend do SmOrg já está sendo desenvolvido paralelamente na disciplina DIM0547 ministrada pelo mesmo docente. Essa integração atende aos critérios do curso e qualifica o projeto para o **bônus de integração de 15% por sprint**.
2. **Sinergia Tecnológica (Kotlin de Ponta a Ponta):** O backend principal (`smorg-api`) é implementado em **Kotlin com Ktor**. A utilização de Kotlin tanto no servidor quanto no aplicativo móvel com Kotlin Multiplatform (KMP) permite:
   * Compartilhamento de conceitos de design de API e contratos de dados serializáveis (`kotlinx.serialization`).
   * Uso consistente de Coroutines e programação reativa em todo o ciclo de vida da requisição.
3. **Atendimento aos Casos de Uso Específicos do Laboratório:** Diferente de um BaaS genérico (como Firebase ou Supabase — Opções A e B), a API própria integra um microsserviço em Go via gRPC responsável por calcular datas limites com base em regras acadêmicas (dias úteis, feriados e perfil do aluno), algo que não seria viável nativamente em soluções BaaS puras sem infraestrutura adicional.
4. **Alternativas Descartadas:**
   * *Supabase / Firebase (Opções A e B):* Embora possuam SDKs e auth facilitada, limitariam o modelo relacional de transações de ativos e impediriam a integração planejada com o microsserviço de cálculo de prazos em Go.
   * *Local com APIs Públicas (Opção D):* Não suportaria o ciclo de vida transacional fechado exigido para empréstimos e devoluções físicas de um laboratório real.

---

## 6. Equipe e Informações Acadêmicas

* **Estudante:** Miguel Xavier de Morais  
* **Matrícula:** 20240027427  
* **Papel:** Desenvolvedor Mobile Multiplataforma (KMP / Compose Multiplatform)  
*(Projeto individual; redação no plural mantida por convenção acadêmica).*

---

## 7. Coorte e Integrações Declaradas

* **Coorte de Apresentação:** **Coorte A (Presencial)** — Apresentações em sala de aula nas datas estipuladas no cronograma.
* **Integração Declarada:**  
  Este projeto móvel está **formalmente integrado** com a disciplina **DIM0547 — Desenvolvimento de Sistemas Web II**. O aplicativo consome a API do backend **SmOrg**, implementada em Ktor e Go:
  * **Repositório da API:** [https://github.com/Goguel/smorg-api](https://github.com/Goguel/smorg-api)
  * A URL do backend e os contratos de comunicação serão mantidos em sincronia contínua, com evidências documentadas e demonstradas nos vídeos de sprint.
