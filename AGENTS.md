# AGENTS.md

Este arquivo descreve o estado real do repositório no momento da análise e serve
como contexto para agentes de IA/Codex que forem trabalhar no projeto.

## 1. Visão geral do projeto

O projeto é um MVP de gestão de contratos. Ele permite cadastrar e consultar
contratos e pessoas, manter o histórico de responsáveis, acompanhar vigências,
agendar notificações de vencimento, registrar auditoria, visualizar métricas e
exportar relatórios CSV.

A arquitetura é um monólito dividido em duas aplicações:

- `backend/`: API REST stateless em Spring Boot, com regras de negócio, segurança,
  persistência, scheduler e integrações de notificação.
- `frontend/`: SPA Angular que consome a API, fornece a interface Material e
  integra o login OIDC/PKCE com Keycloak.
- `docker-compose.yml`: ambiente local com PostgreSQL, Keycloak, backend e
  frontend.

O backend é a autoridade final de autenticação/autorização e validação. O
frontend apenas melhora a experiência do usuário, ocultando ou desabilitando
ações sem substituir a proteção da API.

## 2. Stack tecnológica

### Backend

- Java 21.
- Spring Boot 3.5.15.
- Maven; o Dockerfile usa Maven 3.9.16 com Eclipse Temurin 21.
- Spring Web MVC, Spring Data JPA, Bean Validation e Spring Security.
- OAuth2 Resource Server com validação de JWT emitido pelo Keycloak.
- PostgreSQL Driver e Flyway para o schema do banco.
- Spring Boot Actuator, com `health` e `info` expostos.
- Spring Boot Mail para o provider SMTP.
- Springdoc OpenAPI 2.8.17 para documentação da API.
- JUnit 5 via Spring Boot Test, Spring Security Test, H2 e Testcontainers.

### Frontend

- Angular 21.2.x, com Angular CLI/build 21.2.10.
- TypeScript 5.9.2, target ES2022 e compilação estrita.
- Angular Material/CDK 21.2.x.
- Angular Router, Reactive Forms, HttpClient e RxJS 7.8.x.
- `keycloak-angular` 21.0.0 e `keycloak-js` 26.2.4.
- Vitest 4.0.8 para testes unitários.
- npm 10.9.3, declarado em `frontend/package.json`.

### Infraestrutura e serviços

- PostgreSQL 17 Alpine no Compose.
- Keycloak 26.7.0 no Compose, com importação do realm versionado.
- Nginx 1.28.3 Alpine servindo o build do frontend e fazendo proxy de `/api/`.
- Imagens Docker multi-stage para backend e frontend.
- Integração opcional com WhatsApp Cloud API da Meta.
- SMTP configurável por ambiente; nenhum servidor SMTP real é versionado.

## 3. Estrutura do projeto

```text
backend/
  pom.xml
  Dockerfile
  src/main/java/com/organization/contractmanager/
    config/        CORS, OpenAPI e scheduling
    controller/    endpoints REST
    domain/        entidades JPA e enums de domínio
    dto/           records de entrada e saída da API
    exception/     exceções de negócio e RestControllerAdvice
    mapper/        conversão entre entidades e DTOs
    report/        abstrações e exporter de relatórios
    repository/    repositórios Spring Data e Specifications
    scheduler/     disparo periódico do processamento
    security/      regras JWT, roles e acesso por contrato
    service/       regras de negócio e providers
  src/main/resources/
    application*.yml
    db/migration/  migrations Flyway V1 a V8
  src/test/java/   testes unitários, MVC, JPA, segurança e integração

frontend/
  package.json
  angular.json
  Dockerfile
  nginx.conf
  src/
    app/
      core/         autenticação, configuração, HTTP e loading
      features/     dashboard, contratos, pessoas, notificações,
                    administração e relatórios
      shared/       componentes compartilhados
    environments/   configurações de desenvolvimento, produção e Docker
    styles.scss     tema Material e estilos globais

docker/
  keycloak/         realm local do Keycloak

docker-compose.yml  ambiente local completo
.env.example        nomes e placeholders de configuração local
README.md           execução, configuração e limitações do MVP
CHANGELOG.md        histórico da versão inicial
```

## 4. Arquitetura

### Backend

O backend usa uma arquitetura em camadas dentro do package base
`com.organization.contractmanager`:

- Controllers recebem HTTP, validam DTOs e delegam para services. Eles não
  concentram regras de negócio.
- Services são beans `@Service`, normalmente transacionais, e implementam as
  regras de contratos, pessoas, vínculos, prazos, schedules, notificações,
  dashboard, auditoria e relatórios.
- Repositories são interfaces Spring Data JPA. Consultas de listagem usam
  `Specification`, paginação, ordenação, projeção agregada ou `EntityGraph`.
- Entities usam UUID, JPA, enums persistidos como string e callbacks de
  auditoria temporal (`@PrePersist`/`@PreUpdate`). O relacionamento de
  responsáveis é a entidade explícita `ContractAssignment`; não há
  `@ManyToMany` simples entre contrato e pessoa.
- DTOs são Java records. Mappers convertem DTOs e entidades.
- `GlobalExceptionHandler` padroniza erros em `ApiErrorResponse`, sem expor
  stack trace, SQL ou detalhes internos ao cliente.
- `SecurityConfig` valida JWT do Keycloak, converte `realm_access.roles` para
  authorities `ROLE_*` e aplica regras por método e rota.
- `UserAdminController` e `UserAdminService` expõem administração paginada de
  usuários e substituição das roles de aplicação, protegidas por ADMIN.
- `KeycloakAdminRestClient` usa client credentials de uma service account para
  consultar usuários e aplicar roles na API administrativa do Keycloak. A
  integração é configurável e preserva roles internas que não pertencem à
  aplicação.
- `ContractAccessPolicy` limita inspetores aos contratos associados à pessoa
  identificada pelo claim JWT `person_id`.
- `NotificationScheduler` usa `@Scheduled` com timezone
  `America/Sao_Paulo`. `NotificationScheduleRepository` usa lock pessimista e
  consulta em lotes para evitar processamento concorrente do mesmo schedule.
- `NotificationProvider` abstrai canais. O provider de e-mail é sempre
  registrado; o provider WhatsApp e o cliente da API oficial são condicionais à
  configuração de habilitação.
- `ReportExporter` é uma extensão por formato. Atualmente existe somente
  `CsvReportExporter`, embora `ReportFormat` enumere também XLSX e PDF.

Principais rotas REST existentes:

- `/api/v1/contracts`: CRUD, filtros/paginação, fechamento, cancelamento,
  histórico e `/assignments`.
- `/api/v1/persons`: CRUD e pesquisa por nome; desativação é lógica via update.
- `/api/v1/notifications`: consulta paginada e filtrada.
- `/api/v1/admin/notification-deadlines`: administração de prazos para ADMIN.
- `/api/v1/admin/users`: consulta de usuários e atribuição de roles para ADMIN.
- `/api/v1/dashboard`: contagens agregadas.
- `/api/v1/reports/export`: exportação CSV dos tipos de relatório suportados.
- `/actuator/health`, `/actuator/info` e OpenAPI/Swagger são públicos; as APIs
  `/api/v1/**` exigem JWT.

### Frontend

O frontend é uma aplicação Angular standalone inicializada por
`bootstrapApplication`, sem NgModules de aplicação. `app.config.ts` registra
Router, HttpClient, animações, Keycloak e interceptors. As telas são carregadas
por rotas lazy-loaded e usam componentes standalone, signals, Reactive Forms e
Angular Material.

- `core/auth`: configuração Keycloak OIDC/PKCE, refresh automático, login,
  logout, guard e checagem de permissões para UX.
- `core/http`: token de backend, interceptor de bearer token, interceptor de
  erros e indicador de loading.
- `features/contracts`: modelos, service HTTP, lista, formulário, detalhe,
  contratos próximos do vencimento e edição de responsáveis.
- `features/persons`: lista, formulário e detalhe de pessoas.
- `features/dashboard`: cards de métricas e contratos próximos do vencimento.
- `features/notifications`: consulta paginada, filtros e diálogo de falha.
- `features/administration`: CRUD de `NotificationDeadline` e página de
  configurações de usuários/roles, central administrativa, notificações e
  informações do sistema.
- `features/reports`: filtros e download de CSV.
- `shared`: componentes reutilizáveis, atualmente incluindo placeholder de
  feature.

Rotas principais do Angular: `/login`, `/dashboard`, `/contracts`,
`/contracts/new`, `/contracts/:id`, `/contracts/:id/edit`,
`/contracts/expiring`, `/persons`, `/persons/new`, `/persons/:id`,
`/persons/:id/edit`, `/notificacoes` (com alias `/notifications`), `/reports`,
`/administration`, `/administration/users`, `/administration/notifications` e
`/administration/system`. A rota `/login` é pública e inicia o login somente
após ação explícita do usuário; as rotas de administração usam um guard de role
ADMIN e as demais rotas são protegidas pelo guard de autenticação.

## 5. Convenções de desenvolvimento

- Java segue package base, classes em PascalCase, métodos/campos em camelCase e
  enums em maiúsculas com underscore quando necessário.
- DTOs de requisição/resposta ficam em `dto` e usam records; entidades ficam em
  `domain`; conversões ficam em `mapper`.
- A injeção de dependências é feita por construtor; não há Lombok.
- Entidades JPA possuem construtor sem argumentos protegido e usam `UUID` como
  identificador. Relacionamentos são `LAZY` quando aplicável.
- Transações são declaradas no service com `@Transactional`; leituras usam
  `readOnly = true` quando adequado.
- Regras de data e unicidade são validadas no service e reforçadas por
  constraints do banco. Contratos não aceitam `endDate` anterior a `startDate`.
- Listagens de contratos/notificações usam paginação, ordenação e Specifications
  no banco, não filtragem posterior em memória. `EntityGraph`/`join fetch` são
  usados nas consultas que precisam de relacionamentos.
- Histórico registra ações de contrato, vínculos e entregas de notificação;
  valores devem permanecer resumidos e não conter segredos ou dados pessoais
  desnecessários.
- Logging backend usa SLF4J com placeholders e registra IDs, estados, tipos de
  erro e contagens operacionais. Não registrar senhas, JWT completo, tokens de
  API, respostas brutas de providers ou destinatários desnecessariamente.
- No frontend, serviços usam `inject`, observables e `takeUntilDestroyed`; os
  componentes usam signals para estado local. Estilos ficam em SCSS global ou
  no próprio componente.
- O TypeScript é estrito (`strictTemplates`, `strictInjectionParameters` e
  `noImplicitReturns`). Preserve esse nível ao alterar código.
- A interface utiliza português e o tema corporativo atual usa cores frias
  (azul-marinho, azul e ciano).

## 6. Como executar o projeto

### Docker Compose (desenvolvimento local)

Pré-requisito: Docker com Docker Compose.

```bash
cp .env.example .env
docker compose up --build
```

No PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Serviços locais: frontend em `http://localhost:4200`, backend em
`http://localhost:8080`, Keycloak em `http://localhost:8081` e health do backend
em `/actuator/health`. O realm é importado automaticamente. O Compose é para
desenvolvimento; os volumes `postgres_data` e `keycloak_data` persistem os
dados.

### Backend sem Docker

Pré-requisitos: Java 21, Maven, PostgreSQL e configuração de Keycloak/banco.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Build e testes:

```bash
cd backend
mvn test
mvn package
```

### Frontend sem Docker

Pré-requisito: Node.js compatível com a versão usada no container e npm.

```bash
cd frontend
npm ci
npm start
```

Testes e build:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

Para construir as imagens sem iniciar os serviços:

```bash
docker compose build
```

Não há configuração de e2e/browser automation versionada; `ng e2e` aparece no
README gerado do Angular, mas nenhum framework e2e foi adicionado ao projeto.

## 7. Banco de dados

O banco oficial é PostgreSQL. O Hibernate está configurado com
`spring.jpa.hibernate.ddl-auto=validate`; tabelas e índices são controlados
exclusivamente por Flyway.

Migrations presentes:

1. `V1__create_contracts.sql`: contratos, unicidade do número, datas, status e
   índices de vigência/status/empresa.
2. `V2__create_persons.sql`: pessoas, CPF único quando informado e índices de
   nome/e-mail.
3. `V3__create_contract_assignments.sql`: vínculos históricos, FKs e índices de
   contrato/pessoa/ativo.
4. `V4__create_notification_deadlines.sql`: configuração de prazos, constraint
   de duplicidade e seeds iniciais de prazos.
5. `V5__create_notification_schedules.sql`: schedules por contrato/vigência,
   constraint de unicidade e índice operacional de status/data.
6. `V6__create_notifications.sql`: notificações por pessoa/canal, snapshot do
   destinatário, unicidade de entrega e índices de consulta.
7. `V7__create_contract_history.sql`: auditoria das ações principais.
8. `V8__add_operational_query_indexes.sql`: índices compostos de contratos e
   notificações.

As entidades principais são `Contract`, `Person`, `ContractAssignment`,
`NotificationDeadline`, `NotificationSchedule`, `Notification`, e
`ContractHistory`. Contratos podem estar `ACTIVE`, `CLOSED`, `CANCELLED` ou
`SUSPENDED`; não existe status persistido `EXPIRED`, pois expiração é calculada
por `endDate`.

Ao criar contrato ativo, os prazos habilitados geram schedules futuros. Uma
alteração real de `endDate` cancela apenas schedules antigos pendentes e cria os
novos, preservando processados. O scheduler busca `PENDING` com
`scheduledDate <= hoje`, cancela os associados a contratos fechados/cancelados
e prepara notificações para pessoas responsáveis ativas.

## 8. Configuração e variáveis de ambiente

Os nomes abaixo são usados pelo Compose, pelos arquivos `application*.yml` ou
pelos ambientes Angular. Os valores locais ficam em `.env`, que é ignorado pelo
Git; somente placeholders de `.env.example` são versionados.

### Banco e portas

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.
- `BACKEND_PORT`, `FRONTEND_PORT`, `KEYCLOAK_PORT`.
- `BACKEND_URL` para o ambiente Angular local; no build Docker o frontend usa
  URL relativa e o Nginx faz proxy para o backend.

### Keycloak

- `KEYCLOAK_ISSUER_URI` para o issuer JWT do backend.
- `KEYCLOAK_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID` para o Angular/Compose.
- `KEYCLOAK_ADMIN_USERNAME` e `KEYCLOAK_ADMIN_PASSWORD` para o bootstrap local
  do Keycloak; nunca usar valores locais de exemplo em ambiente compartilhado.
- `KEYCLOAK_ADMIN_ENABLED`, `KEYCLOAK_ADMIN_BASE_URL`,
  `KEYCLOAK_ADMIN_REALM`, `KEYCLOAK_ADMIN_CLIENT_ID` e
  `KEYCLOAK_ADMIN_CLIENT_SECRET` habilitam a service account usada pela
  administração de usuários. No Compose, a URL base é substituída pela URL
  interna do serviço Keycloak.
- O Compose também injeta `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`
  para a comunicação interna com o Keycloak.

### CORS e scheduler

- `CORS_ALLOWED_ORIGINS`: origens separadas por vírgula.
- `NOTIFICATION_CRON`: expressão cron de seis campos.
- `NOTIFICATION_BATCH_SIZE`: tamanho máximo de lote do scheduler.

### E-mail

- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`.
- `MAIL_HEALTH_ENABLED` controla a verificação de health do mail no Compose;
  `MANAGEMENT_HEALTH_MAIL_ENABLED` é o override interno correspondente.

### WhatsApp

- `WHATSAPP_ENABLED`.
- `WHATSAPP_API_URL`, `WHATSAPP_API_VERSION`.
- `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_ACCESS_TOKEN`.

O WhatsApp é desabilitado por padrão. Tokens, senhas e credenciais nunca devem
ser colocados neste arquivo, em logs ou no código-fonte.

## 9. Testes

### Backend

Os testes ficam em `backend/src/test/java` e cobrem:

- contexto Spring e Actuator;
- controllers com MockMvc/WebMvcTest;
- services com JUnit/Mockito;
- repositories com `@DataJpaTest` e migrations;
- segurança JWT, roles, CORS e restrição por inspetor;
- providers de e-mail/WhatsApp sem depender de servidor externo;
- exportação CSV;
- integração completa com PostgreSQL real em
  `PostgreSqlContractWorkflowIntegrationTests` via Testcontainers.

`mvn test` executa a suíte. O teste de integração requer Docker disponível para
subir PostgreSQL de teste; os testes de slice usam H2 conforme o profile `test`.

### Frontend

Os testes `.spec.ts` ficam junto dos componentes e services e usam o runner
Vitest integrado ao Angular CLI. Há cobertura da shell, forms, services,
permissões, interceptor de erros, dashboard, contratos, pessoas,
notificações, responsáveis e administração.

Comando confirmado:

```bash
npm test -- --watch=false
```

## 10. Regras para futuras alterações

- Analise primeiro implementações semelhantes existentes e preserve a arquitetura
  em camadas.
- Reutilize componentes, services, mappers, Specifications e utilitários antes
  de criar novos.
- Evite duplicação de código e não adicione dependências sem necessidade clara.
- Não altere contratos de API existentes sem avaliar o frontend e os testes que
  os consomem.
- Mantenha compatibilidade com o schema Flyway, os DTOs e as enumerações já
  persistidas.
- Toda regra de autorização deve permanecer no backend; proteções no Angular
  são somente UX.
- Validações de entrada devem existir no DTO/service quando aplicável e também
  ser reforçadas por constraint do banco quando a invariável for persistente.
- Preserve paginação, Specifications, `EntityGraph` e índices para evitar
  carregar coleções inteiras ou introduzir N+1.
- Não remova funcionalidades, rotas, migrations ou dados históricos sem
  solicitação explícita.
- Não modifique arquivos fora do escopo da tarefa sem justificativa.
- Não altere Compose, Dockerfiles, Keycloak, CORS ou configurações de ambiente
  sem avaliar o impacto e registrar a decisão.
- Nunca versionar secrets, senhas, tokens, chaves privadas ou credenciais.
- Atualize ou crie testes para comportamento alterado e execute os testes/builds
  disponíveis antes de concluir.

## 11. Procedimento antes de modificar código

1. Leia este `AGENTS.md`.
2. Examine todos os arquivos relacionados à tarefa.
3. Procure implementações semelhantes, rotas, DTOs, repositories e testes.
4. Identifique dependências, migrations, consumidores e possíveis impactos.
5. Implemente usando os padrões já observados.
6. Execute os testes, build e lint disponíveis para a área alterada.
7. Corrija problemas introduzidos pela alteração e verifique `git diff --check`.
8. Informe os arquivos modificados e as decisões relevantes.

## 12. Estado atual do projeto

O MVP atualmente possui:

- CRUD de contratos e pessoas, com validações e histórico de responsáveis.
- Fechamento/cancelamento de contratos e desativação lógica de pessoas.
- Pesquisa, filtros, ordenação e paginação de contratos e notificações.
- Dashboard com contagens agregadas de ativos, vencidos, próximos do vencimento
  e falhas.
- Prazos de notificação administráveis e schedules recalculados ao mudar a
  vigência.
- Scheduler diário com timezone configurável por cron, recuperação de atrasados
  e lock pessimista no PostgreSQL.
- Notificações persistidas por e-mail e WhatsApp opcional, com status, erro,
  retry count e auditoria de entrega.
- Histórico das principais alterações contratuais.
- Relatórios CSV para contratos, responsáveis e notificações.
- Autenticação OIDC/PKCE no Angular e autorização por roles no backend.
- Configurações administrativas de usuários Keycloak, com atribuição de
  `ADMIN`, `CONTRACT_MANAGER`, `INSPECTOR` e `VIEWER`.
- Layout Angular responsivo com Angular Material, tratamento de erros e loading.

Pontos em desenvolvimento ou deliberadamente limitados pelo MVP:

- `ReportFormat` possui `XLSX` e `PDF`, mas somente CSV tem exporter implementado.
- Não existe retry automático/fila/outbox externa para notificações.
- Pessoas ainda são listadas sem paginação.
- Não existe controller público de schedules; o processamento é interno ao
  service/scheduler.

## 13. Pontos de atenção

- O Compose usa Keycloak `start-dev` e credenciais de desenvolvimento fornecidas
  pelo ambiente; não é configuração de produção.
- O realm versionado cria cliente público, PKCE, claim `person_id` e roles, mas
- também inclui um client confidencial de service account para a administração
  de usuários. Usuários da aplicação e associação de `person_id` precisam ser
  criados manualmente no Keycloak local.
- A administração de usuários depende das credenciais de client credentials e
  das roles `manage-users`, `query-users`, `view-users` e `view-realm` no
  service account. O endpoint permanece indisponível com resposta sanitizada se
  essa integração estiver desabilitada ou não configurada.
- `application.yml` exige issuer JWT configurado; os profiles `dev` e `test`
  completam o cenário de desenvolvimento/teste.
- O frontend Docker usa o mesmo host para API e Nginx proxy; o frontend local
  usa `environment.ts` com URL de backend separada.
- O scheduler mantém a transação/lock durante o dispatch do provider. Para alto
  volume ou múltiplas instâncias, uma fila/outbox e workers independentes seriam
  uma evolução, não uma funcionalidade existente.
- Não há pipeline CI/CD, infraestrutura de produção, backup, alta
  disponibilidade ou observabilidade distribuída versionados neste repositório.
- Alterações de migrations devem ser aditivas e compatíveis com bancos já
  inicializados; `ddl-auto=validate` faz divergências aparecerem na inicialização.
- Ao modificar logging, preservar a sanitização de tokens, credenciais,
  respostas brutas de providers e dados pessoais.

## Manutenção deste arquivo

Atualize o `AGENTS.md` quando uma alteração modificar significativamente a
arquitetura, a estrutura de diretórios, a stack tecnológica, os comandos de
execução, os padrões de desenvolvimento, as integrações ou regras importantes
do sistema. Mudanças triviais não exigem atualização deste arquivo.
