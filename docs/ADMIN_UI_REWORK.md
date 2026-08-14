# ALTERAÇÃO — PÁGINA DE ACESSO E REORGANIZAÇÃO DA ADMINISTRAÇÃO

Leia integralmente o arquivo `AGENTS.md` antes de realizar qualquer alteração.

Este projeto já está em funcionamento e já possui autenticação, autorização e gerenciamento administrativo de usuários integrado ao Keycloak.

**NÃO recrie essas funcionalidades.**

A tarefa consiste principalmente em:

1. criar uma página inicial própria de acesso ao Sistema de Gestão de Contratos;
2. preservar a autenticação existente via Keycloak;
3. reorganizar e aprimorar a interface administrativa Angular existente;
4. centralizar as configurações administrativas em uma área denominada **Configurações**;
5. reutilizar integralmente a infraestrutura existente de gerenciamento de usuários e roles;
6. garantir que toda a área de configurações seja acessível somente por `ADMIN`.

---

# 1. PRIMEIRA ETAPA — ANALISAR O ESTADO ATUAL

Antes de modificar código, examine obrigatoriamente:

## Frontend

* configuração do `keycloak-angular`;
* configuração do `keycloak-js`;
* `app.config.ts`;
* rotas atuais;
* guards de autenticação;
* guards de role;
* serviço de autenticação;
* serviço de permissões;
* interceptors;
* shell/layout principal;
* sidenav/menu;
* `features/administration`;
* página atual de usuários;
* página atual de `NotificationDeadline`;
* services utilizados pela administração;
* testes existentes dessas funcionalidades.

## Backend

Examine apenas para compreender as APIs existentes:

* `UserAdminController`;
* `UserAdminService`;
* `KeycloakAdminRestClient`;
* `SecurityConfig`;
* configuração de roles;
* endpoints `/api/v1/admin/**`;
* administração de `NotificationDeadline`;
* testes de autorização existentes.

A princípio, **não altere o backend de gerenciamento de usuários**, salvo se encontrar uma necessidade real para suportar a reorganização da interface.

Antes de criar qualquer novo service, component ou endpoint, procure implementação equivalente existente.

---

# 2. PRINCÍPIO FUNDAMENTAL

O Keycloak continuará sendo a fonte de identidade dos usuários.

Não criar:

* tabela local de usuários para autenticação;
* tabela local de senhas;
* autenticação paralela;
* sincronização de senha;
* cópia de senha do Keycloak;
* endpoint próprio que receba usuário e senha;
* outro mecanismo de gerenciamento de roles.

A aplicação deverá continuar utilizando:

```text
Keycloak
OIDC
Authorization Code
PKCE
JWT
```

O backend continuará sendo a autoridade final de autorização.

---

# 3. NÃO RECRIAR GERENCIAMENTO DE USUÁRIOS

O projeto já possui integração administrativa com o Keycloak.

Reutilizar obrigatoriamente:

```text
UserAdminController
UserAdminService
KeycloakAdminRestClient
```

Também reutilizar os DTOs, services Angular, models e componentes existentes quando aplicável.

O fluxo deverá permanecer:

```text
Angular
   ↓
API Spring Boot
   ↓
UserAdminController
   ↓
UserAdminService
   ↓
KeycloakAdminRestClient
   ↓
Keycloak Admin REST API
```

Nunca:

```text
Angular
   ↓
Keycloak Admin REST API
```

O frontend nunca deverá receber:

```text
client secret
service account secret
credenciais administrativas
```

---

# 4. OBJETIVO DA REORGANIZAÇÃO

Atualmente existem recursos administrativos distribuídos pela aplicação.

Reorganize-os sob uma única área:

```text
Configurações
```

A área deve funcionar como ponto central de administração do Sistema de Gestão de Contratos.

Estrutura desejada:

```text
Configurações
│
├── Usuários e permissões
│
├── Notificações
│
└── Sistema
```

Outras configurações administrativas poderão futuramente ser adicionadas nesse mesmo módulo.

---

# 5. MENU PRINCIPAL

Reorganize o menu principal.

Para usuários comuns:

```text
Dashboard

Contratos

Pessoas

Notificações

Relatórios
```

Para ADMIN:

```text
Dashboard

Contratos

Pessoas

Notificações

Relatórios

Configurações
```

A opção:

```text
Configurações
```

deve existir visualmente somente para usuários com:

```text
ADMIN
```

Não mostrar item desabilitado para usuários comuns.

Simplesmente não apresentar a opção.

Entretanto, ocultar o menu é apenas UX.

As rotas administrativas deverão continuar protegidas pelo guard e as APIs pelo backend.

---

# 6. ROTA DE CONFIGURAÇÕES

Preserve preferencialmente a rota existente:

```text
/administration
```

Não altere URLs existentes sem necessidade.

Ela passará a funcionar como página inicial das configurações.

Rotas desejadas:

```text
/administration

/administration/users

/administration/notifications

/administration/system
```

Caso já exista rota equivalente para `NotificationDeadline`, preserve compatibilidade ou faça redirecionamento apropriado.

Não quebre URLs existentes desnecessariamente.

---

# 7. PÁGINA INICIAL DE CONFIGURAÇÕES

Reformule:

```text
/administration
```

como uma página de central administrativa.

Título:

```text
Configurações
```

Subtítulo sugerido:

```text
Gerencie usuários, permissões e parâmetros do Sistema de Gestão de Contratos.
```

Apresente cards administrativos.

Exemplo conceitual:

```text
┌──────────────────────────────────────────────┐
│ Configurações                                │
│                                              │
│ Gerencie usuários, permissões e parâmetros   │
│ do Sistema de Gestão de Contratos.           │
│                                              │
│ ┌────────────────────┐ ┌───────────────────┐ │
│ │ 👤                 │ │ 🔔                │ │
│ │ Usuários e         │ │ Notificações      │ │
│ │ permissões         │ │                   │ │
│ │                    │ │ Configure os      │ │
│ │ Gerencie perfis e  │ │ prazos de aviso.  │ │
│ │ acessos.           │ │                   │ │
│ │                    │ │                   │ │
│ │ [Gerenciar]        │ │ [Configurar]      │ │
│ └────────────────────┘ └───────────────────┘ │
│                                              │
│ ┌────────────────────┐                       │
│ │ ⚙                  │                       │
│ │ Sistema            │                       │
│ │                    │                       │
│ │ Informações gerais │                       │
│ │ da aplicação.      │                       │
│ │                    │                       │
│ │ [Visualizar]       │                       │
│ └────────────────────┘                       │
└──────────────────────────────────────────────┘
```

Utilize Angular Material e preserve o tema corporativo atual.

---

# 8. CARD — USUÁRIOS E PERMISSÕES

O card:

```text
Usuários e permissões
```

deverá redirecionar para:

```text
/administration/users
```

Descrição sugerida:

```text
Gerencie os usuários e os níveis de acesso ao sistema.
```

Não implementar novamente a comunicação com Keycloak.

Utilizar a página/service/API existente.

O objetivo nesta etapa é **melhorar a experiência da interface atual**.

---

# 9. APRIMORAR A TELA EXISTENTE DE USUÁRIOS

Analise primeiro a implementação atual.

Não substitua uma tela funcional sem necessidade.

Aprimore-a para apresentar os usuários de maneira clara.

Tabela desejada:

```text
Usuário | Nome | E-mail | Permissões | Ações
```

Exemplo:

```text
admin
Administrador
admin@empresa.com

ADMIN

[Gerenciar permissões]
```

Outro exemplo:

```text
joao.silva
João Silva
joao@empresa.com

VIEWER
INSPECTOR

[Gerenciar permissões]
```

Utilizar chips do Angular Material para representar roles quando fizer sentido.

---

# 10. PESQUISA DE USUÁRIOS

Se a API atual permitir pesquisa, disponibilizar campo:

```text
Pesquisar usuário
```

Permitir pesquisa pelos atributos já suportados pela API existente.

Preferencialmente:

```text
username
nome
email
```

Não criar filtragem pesada no frontend caso o backend já possua pesquisa paginada.

Preservar paginação existente.

---

# 11. GERENCIAR PERMISSÕES

Ao selecionar:

```text
Gerenciar permissões
```

abrir preferencialmente:

* diálogo Angular Material;
* drawer;
* ou página dedicada,

conforme o padrão já utilizado no projeto.

Não criar nova infraestrutura de permissões.

Utilizar o endpoint administrativo existente.

Apresentar:

```text
Usuário
Nome
E-mail
```

e as permissões disponíveis:

```text
Administrador
Gestor de contratos
Fiscal
Visualização
```

Correspondência:

```text
Administrador
→ ADMIN

Gestor de contratos
→ CONTRACT_MANAGER

Fiscal
→ INSPECTOR

Visualização
→ VIEWER
```

---

# 12. EXIBIÇÃO DAS PERMISSÕES

Não obrigue o usuário administrador a conhecer os nomes técnicos das roles.

Na interface utilizar:

```text
Administrador
Gestor de contratos
Fiscal
Visualização
```

Opcionalmente mostrar o nome técnico em texto secundário:

```text
Administrador
ADMIN
```

Mas priorizar nomes amigáveis em português.

---

# 13. DESCRIÇÃO DAS PERMISSÕES

Apresente descrição curta para cada role.

## Administrador

```text
Possui acesso completo ao sistema, incluindo configurações e gerenciamento de usuários.
```

## Gestor de contratos

```text
Pode cadastrar e alterar contratos e gerenciar seus responsáveis.
```

## Fiscal

```text
Pode acessar os contratos relacionados à sua fiscalização.
```

## Visualização

```text
Possui acesso somente às funcionalidades de consulta permitidas.
```

---

# 14. SALVAMENTO DE PERMISSÕES

Ao clicar em:

```text
Salvar
```

utilizar o fluxo administrativo existente.

Enquanto estiver salvando:

* desabilitar botão;
* apresentar loading;
* impedir clique repetido.

Após sucesso:

```text
Permissões atualizadas com sucesso.
```

Após falha:

```text
Não foi possível atualizar as permissões do usuário.
```

Preservar o tratamento global de erros já existente.

---

# 15. NÃO REMOVER ROLES EXTERNAS

Preservar o comportamento backend existente que mantém roles do Keycloak que não pertencem à aplicação.

A interface administrativa deverá gerenciar somente:

```text
ADMIN
CONTRACT_MANAGER
INSPECTOR
VIEWER
```

Não apresentar ou modificar:

```text
offline_access
uma_authorization
default-roles-*
```

ou quaisquer roles internas/externas não pertencentes à aplicação.

---

# 16. CARD — NOTIFICAÇÕES

O card:

```text
Notificações
```

deverá redirecionar para:

```text
/administration/notifications
```

Descrição:

```text
Configure os períodos de aviso de vencimento dos contratos.
```

A feature de `NotificationDeadline` já existe.

NÃO recriá-la.

Reutilizar:

* service existente;
* models existentes;
* endpoints existentes;
* componentes existentes sempre que adequado.

O objetivo é integrar a funcionalidade existente à nova organização visual.

---

# 17. PÁGINA DE CONFIGURAÇÕES DE NOTIFICAÇÕES

Título:

```text
Configurações de notificações
```

Descrição:

```text
Defina com quantos dias de antecedência os responsáveis serão avisados sobre o vencimento dos contratos.
```

Apresentar os períodos existentes.

Exemplo:

```text
60 dias        Ativo
30 dias        Ativo
15 dias        Ativo
```

Preservar as funcionalidades já implementadas de:

```text
adicionar
habilitar
desabilitar
remover
```

Não alterar regras de:

```text
NotificationSchedule
Notification
Scheduler
```

sem necessidade relacionada diretamente à interface.

---

# 18. CARD — SISTEMA

Criar:

```text
/administration/system
```

Esta página inicialmente será informativa.

Não criar configurações complexas nesta etapa.

Título:

```text
Informações do sistema
```

Poderá apresentar:

```text
Aplicação
Sistema de Gestão de Contratos

Backend
Disponível

Autenticação
Keycloak

Ambiente
<quando disponível de forma segura>

Versão
<quando disponível>
```

Utilizar `/actuator/health` ou informações já existentes apenas se adequado.

Não expor dados sensíveis.

---

# 19. DADOS QUE NÃO DEVEM SER EXIBIDOS

Nunca apresentar na interface:

```text
KEYCLOAK_ADMIN_CLIENT_SECRET
KEYCLOAK_ADMIN_PASSWORD
JWT
Refresh Token
Client Secret
MAIL_PASSWORD
WHATSAPP_ACCESS_TOKEN
DB_PASSWORD
```

Não apresentar configuração bruta do backend.

---

# 20. BREADCRUMBS / NAVEGAÇÃO INTERNA

Se o projeto já possuir padrão equivalente, utilizar.

Caso seja coerente com o layout atual, apresentar:

```text
Configurações > Usuários e permissões
```

```text
Configurações > Notificações
```

```text
Configurações > Sistema
```

Permitir retorno fácil à página principal:

```text
Configurações
```

---

# 21. NOVA PÁGINA DE ACESSO

Além da reorganização administrativa, criar uma página própria da aplicação para usuários não autenticados.

Rota:

```text
/login
```

A aplicação não deverá redirecionar imediatamente para o Keycloak ao ser aberta.

Fluxo esperado:

```text
/
 ↓
Usuário autenticado?
 ↓
SIM ─────────→ /dashboard
 ↓
NÃO
 ↓
/login
```

---

# 22. DESIGN DA TELA DE LOGIN

Criar interface corporativa alinhada ao tema existente.

Exemplo conceitual:

```text
┌───────────────────────────────────────────────┐
│                                               │
│           SISTEMA DE GESTÃO                   │
│              DE CONTRATOS                     │
│                                               │
│     Gestão e acompanhamento de contratos      │
│                                               │
│          ┌─────────────────────────┐          │
│          │                         │          │
│          │    Acesso ao sistema    │          │
│          │                         │          │
│          │ Entre utilizando sua    │          │
│          │ conta institucional.    │          │
│          │                         │          │
│          │ [ Entrar com Keycloak ] │          │
│          │                         │          │
│          └─────────────────────────┘          │
│                                               │
└───────────────────────────────────────────────┘
```

Não criar autenticação local.

---

# 23. BOTÃO DE LOGIN

Criar botão:

```text
Entrar
```

ou:

```text
Entrar com Keycloak
```

Ao clicar:

```text
Angular
 ↓
Keycloak
 ↓
OIDC + PKCE
 ↓
Angular
 ↓
/dashboard
```

Utilizar o serviço de autenticação existente.

Não duplicar inicialização do Keycloak.

---

# 24. INICIALIZAÇÃO DO KEYCLOAK

Analise como o Keycloak está atualmente inicializado.

Caso atualmente esteja configurado para forçar login imediatamente, adapte-o para permitir a exibição de:

```text
/login
```

antes do redirecionamento.

Utilize a estratégia compatível com as versões existentes de:

```text
keycloak-angular 21
keycloak-js 26
```

Preserve:

```text
Authorization Code + PKCE
```

Não implementar Direct Access Grant.

Não solicitar usuário e senha diretamente ao Angular.

---

# 25. USUÁRIO ADMIN

Não criar usuário administrativo no PostgreSQL da aplicação.

O administrador continua sendo um usuário do Keycloak com:

```text
ADMIN
```

As credenciais utilizadas para acessar o sistema devem ser exatamente as credenciais existentes no Keycloak.

Não duplicar credenciais.

---

# 26. MENU DE CONFIGURAÇÕES

Reutilize o shell/sidenav existente.

Evite criar um segundo menu.

Modifique a estrutura atual para que:

```text
Configurações
```

seja apresentada como opção administrativa principal.

Se atualmente houver:

```text
Administração
```

avalie renomear apenas a **label visual** para:

```text
Configurações
```

sem alterar desnecessariamente a rota:

```text
/administration
```

Isso preserva compatibilidade e reduz regressões.

---

# 27. AUTORIZAÇÃO DO MENU

Reutilizar o mecanismo de checagem de roles existente no:

```text
core/auth
```

Não criar outro serviço somente para verificar:

```text
ADMIN
```

O item Configurações deve aparecer quando:

```text
hasRole('ADMIN')
```

ou equivalente existente retornar verdadeiro.

---

# 28. PROTEÇÃO DAS ROTAS

Preservar o guard administrativo existente.

As rotas:

```text
/administration
/administration/users
/administration/notifications
/administration/system
```

devem exigir:

```text
ADMIN
```

Usuário não autenticado:

```text
→ /login
```

Usuário autenticado sem ADMIN:

```text
→ /dashboard
```

ou comportamento `403` já utilizado pelo sistema.

Não criar comportamento divergente sem necessidade.

---

# 29. PROTEÇÃO BACKEND

Nenhuma reorganização visual poderá reduzir a segurança atual.

Endpoints:

```text
/api/v1/admin/users/**
```

e:

```text
/api/v1/admin/notification-deadlines/**
```

devem continuar exigindo:

```text
ROLE_ADMIN
```

O backend continua sendo a autoridade final.

Não considerar o guard Angular como mecanismo de segurança suficiente.

---

# 30. RESILIÊNCIA DA ADMINISTRAÇÃO DE USUÁRIOS

A integração de administração de usuários depende da Keycloak Admin API.

Caso a integração esteja indisponível:

```text
Configurações
→ Usuários e permissões
```

deverá apresentar mensagem amigável.

Exemplo:

```text
Não foi possível acessar o gerenciamento de usuários no momento.
Tente novamente mais tarde.
```

Não deixar a aplicação inteira indisponível.

As demais funcionalidades:

```text
Dashboard
Contratos
Pessoas
Notificações
Relatórios
```

devem continuar funcionando normalmente.

---

# 31. LOADING E EMPTY STATE

Aprimore a interface administrativa para tratar:

## Loading

Exemplo:

```text
Carregando usuários...
```

preferencialmente utilizando o indicador de loading já existente.

## Nenhum resultado

```text
Nenhum usuário encontrado.
```

## Pesquisa sem resultado

```text
Nenhum usuário corresponde aos critérios informados.
```

## Erro

Utilizar mensagens amigáveis e o padrão atual da aplicação.

---

# 32. RESPONSIVIDADE

As páginas administrativas deverão funcionar em:

```text
desktop
notebook
tablet
```

Em telas pequenas:

* evitar overflow desnecessário;
* permitir scroll horizontal da tabela somente quando inevitável;
* considerar cards ou colunas prioritárias;
* manter ações acessíveis.

Preservar o padrão responsivo já utilizado pelo projeto.

---

# 33. PADRÃO VISUAL

Manter identidade existente:

```text
Angular Material
azul-marinho
azul
ciano
```

Não introduzir outra biblioteca de UI.

Não criar outro sistema de tema.

Reutilizar:

* typography;
* spacing;
* botões;
* cards;
* dialogs;
* snackbars;
* tabelas;
* inputs;

já existentes.

---

# 34. REUTILIZAÇÃO DE COMPONENTES

Antes de criar:

```text
novo dialog
nova tabela
novo loading
novo empty-state
novo card
novo snackbar wrapper
```

procure componentes compartilhados existentes em:

```text
shared/
core/
features/
```

Reutilize sempre que isso não prejudicar clareza ou responsabilidade do componente.

---

# 35. ORGANIZAÇÃO ANGULAR

Preservar a arquitetura atual.

Preferencialmente:

```text
features/
  administration/
      administration-home/
      users/
      notifications/
      system/
```

Mas antes examine a estrutura existente.

Não mova arquivos apenas por estética se isso criar alterações desnecessárias.

Faça a menor reorganização capaz de produzir uma estrutura clara.

---

# 36. NÃO CRIAR NOVOS ENDPOINTS SEM NECESSIDADE

Antes de solicitar ou implementar qualquer novo endpoint, confirme que os endpoints atuais não atendem à interface.

Já existem APIs administrativas.

Reutilize-as.

Somente altere o backend se existir requisito impossível de atender pela API atual.

Caso seja necessária mudança:

1. justifique;
2. preserve compatibilidade;
3. adicione testes;
4. não recrie `UserAdminService`;
5. não recrie `KeycloakAdminRestClient`.

---

# 37. NÃO CRIAR NOVAS MIGRATIONS

Esta tarefa é prioritariamente uma reorganização de frontend.

A princípio:

```text
NÃO criar migration Flyway.
```

Usuários continuam no Keycloak.

Roles continuam no Keycloak.

Configurações de notificação já possuem persistência.

Somente crie migration se identificar uma necessidade funcional real que não possa ser atendida pelo modelo existente.

Se isso ocorrer, explique claramente o motivo antes de concluir.

---

# 38. TESTES FRONTEND

Atualize ou crie testes para:

```text
usuário não autenticado acessa /login
```

```text
/login não inicia autenticação automaticamente
```

```text
clicar em Entrar inicia autenticação Keycloak
```

```text
usuário autenticado acessa /dashboard
```

```text
Configurações aparece para ADMIN
```

```text
Configurações não aparece para usuário sem ADMIN
```

```text
/administração exige ADMIN
```

```text
página Configurações apresenta os cards administrativos
```

```text
card Usuários abre /administration/users
```

```text
card Notificações abre configuração existente
```

```text
tela de usuários continua consumindo o service existente
```

```text
edição de roles continua utilizando a API administrativa existente
```

```text
loading de usuários
```

```text
empty state
```

```text
erro da Keycloak Admin API
```

---

# 39. TESTES BACKEND

Não recriar testes já existentes.

Execute primeiro os testes atuais.

Se nenhuma alteração backend for necessária, apenas confirme que continuam passando.

Se houver alteração necessária, adicionar testes somente para o comportamento modificado.

Garantir especialmente que:

```text
ADMIN pode acessar /api/v1/admin/users
```

```text
não ADMIN recebe 403
```

```text
alteração de roles preserva roles externas
```

---

# 40. DOCUMENTAÇÃO

Atualize o README caso o fluxo de entrada da aplicação seja alterado.

Documentar:

```text
/login
```

```text
autenticação Keycloak
```

```text
Configurações
```

```text
gerenciamento de usuários pelo próprio sistema
```

Deixar claro que:

```text
o sistema não armazena senha dos usuários
```

e:

```text
o gerenciamento de permissões realizado pela aplicação modifica as roles do usuário no Keycloak através do backend.
```

Atualizar `AGENTS.md` somente se a alteração modificar significativamente o estado atual documentado.

---

# 41. FLUXO FINAL ESPERADO

## Autenticação

```text
Usuário acessa aplicação
        ↓
       /
        ↓
Está autenticado?
   │             │
  SIM           NÃO
   │             │
   ▼             ▼
Dashboard      /login
                 │
                 ▼
              Entrar
                 │
                 ▼
              Keycloak
                 │
            OIDC + PKCE
                 │
                 ▼
             Dashboard
```

## Administração

```text
Usuário autenticado
        ↓
Possui ADMIN?
   │           │
  NÃO         SIM
   │           │
   │           ▼
   │      Configurações
   │           │
   │     ┌─────┼────────────┐
   │     │     │            │
   │     ▼     ▼            ▼
   │ Usuários Notificações Sistema
   │     │
   │     ▼
   │ Backend existente
   │     │
   │ UserAdminService
   │     │
   │ KeycloakAdminRestClient
   │     │
   │     ▼
   │  Keycloak
   │
   └── Configurações não exibida
```

---

# 42. FOCO DESTA TAREFA

Prioridade:

```text
REUTILIZAR
ORGANIZAR
APRIMORAR
INTEGRAR
```

e não:

```text
RECRIAR
DUPLICAR
REESCREVER
```

Se uma funcionalidade já estiver funcionando, não a substitua apenas porque outra implementação parece mais conveniente.

Prefira pequenas alterações incrementais.

Preserve contratos de API.

Preserve testes.

Preserve funcionalidades atuais.

---

# 43. VALIDAÇÃO OBRIGATÓRIA

Após as alterações:

## Frontend

Execute:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

## Backend

Mesmo que nenhuma alteração significativa seja feita:

```bash
cd backend
mvn test
mvn package
```

Execute também:

```bash
git diff --check
```

Se Docker estiver disponível e as alterações afetarem inicialização/configuração:

```bash
docker compose build
```

Corrija problemas introduzidos pela alteração.

---

# 44. RESULTADO DA EXECUÇÃO

Ao concluir, apresente:

1. estado encontrado antes da alteração;
2. funcionalidades existentes que foram reutilizadas;
3. componentes existentes que foram aprimorados;
4. componentes novos realmente necessários;
5. arquivos modificados;
6. alterações de rotas;
7. alterações no fluxo de login;
8. alterações da área Configurações;
9. alterações na tela de usuários;
10. alterações no gerenciamento de permissões;
11. alterações backend, caso tenham sido realmente necessárias;
12. testes executados;
13. resultado dos builds;
14. limitações ou pontos de atenção.

Não avance para funcionalidades fora deste escopo.
