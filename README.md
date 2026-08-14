# Sistema de Gestão de Contratos

MVP para cadastro e acompanhamento de contratos, responsáveis, vigências,
notificações de vencimento, auditoria, dashboard e relatórios.

## Funcionalidades do MVP

- CRUD de contratos e pessoas, com encerramento/cancelamento e desativação lógica.
- Vínculos históricos de gestores, fiscais titulares e substitutos.
- Pesquisa, paginação, ordenação e filtros de contratos executados no banco.
- Prazos de notificação configuráveis e schedules recalculados ao alterar a vigência.
- Scheduler diário com recuperação de avisos atrasados e proteção contra execução
  concorrente por lock pessimista no PostgreSQL.
- Notificações por e-mail e, opcionalmente, WhatsApp Cloud API.
- Histórico das principais alterações e entregas de notificação.
- Dashboard com contagens agregadas.
- Relatórios CSV de contratos e notificações; arquitetura preparada para XLSX/PDF.
- Autenticação OIDC/PKCE no Angular e OAuth2 Resource Server JWT no backend.
- Papéis `ADMIN`, `CONTRACT_MANAGER`, `INSPECTOR` e `VIEWER`.
- Administração de usuários Keycloak por ADMIN, com atribuição das quatro roles
  da aplicação.
- Página de acesso `/login` com entrada explícita via Keycloak e fluxo OIDC/PKCE.
- Central administrativa `Configurações`, com usuários e permissões, prazos de
  notificações e informações do sistema.

## Arquitetura

- `backend/`: Java 21, Spring Boot, Maven, JPA, Flyway e PostgreSQL.
- `frontend/`: Angular, TypeScript, Angular Material e Reactive Forms.
- `docker/`: importação local do realm Keycloak.
- `docker-compose.yml`: PostgreSQL, Keycloak, backend e frontend.

Controllers tratam HTTP, services concentram regras de negócio e repositories
executam a persistência. O schema é alterado exclusivamente por migrations Flyway.

## Execução local com Docker

Pré-requisito: Docker com Docker Compose.

```bash
cp .env.example .env
docker compose up --build
```

No PowerShell, use `Copy-Item .env.example .env`. Serviços padrão:

- frontend: `http://localhost:4200`
- backend: `http://localhost:8080`
- Keycloak: `http://localhost:8081`
- health: `http://localhost:8080/actuator/health`

O realm, cliente público, cliente de administração e papéis são importados; nenhum
usuário da aplicação é criado. O cliente de administração usa uma service account
com permissões mínimas para consultar usuários e gerenciar roles. Os volumes
`postgres_data` e `keycloak_data` preservam dados. `docker compose down` mantém os
volumes; `docker compose down -v` os remove definitivamente.

Os valores do `.env.example` são placeholders. Nunca reutilize as senhas locais
em ambiente compartilhado ou de produção.

## Configuração

Principais variáveis:

- banco: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- autenticação: `KEYCLOAK_ISSUER_URI`
- administração Keycloak: `KEYCLOAK_ADMIN_ENABLED`, `KEYCLOAK_ADMIN_BASE_URL`,
  `KEYCLOAK_ADMIN_REALM`, `KEYCLOAK_ADMIN_CLIENT_ID`,
  `KEYCLOAK_ADMIN_CLIENT_SECRET`
- CORS: `CORS_ALLOWED_ORIGINS` (lista separada por vírgulas; vazia por padrão)
- scheduler: `NOTIFICATION_CRON`, `NOTIFICATION_BATCH_SIZE`
- SMTP: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
  e `MAIL_HEALTH_ENABLED` (desabilitado no Compose enquanto não houver SMTP real)
- WhatsApp: `WHATSAPP_ENABLED`, `WHATSAPP_API_URL`, `WHATSAPP_API_VERSION`,
  `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_ACCESS_TOKEN`

O timezone de negócio para vencimentos, dashboard, relatórios e scheduler é
`America/Sao_Paulo`. Timestamps persistidos usam `TIMESTAMP WITH TIME ZONE` e
`Instant`. Em desenvolvimento, CORS aceita apenas as origens locais configuradas;
em produção, informe explicitamente a origem HTTPS do frontend.

O WhatsApp fica desabilitado por padrão. IDs, status e códigos sanitizados do
provider podem aparecer nos logs; tokens e respostas brutas não são registrados.

## Autenticação e autorização

Todas as rotas `/api/v1/**` exigem JWT válido do Keycloak. O backend é a autoridade
final; controles visuais do Angular são apenas UX. Permanecem públicos somente
`/actuator/health`, `/actuator/info` e a documentação OpenAPI.

- `ADMIN`: acesso completo e administração de prazos.
- `CONTRACT_MANAGER`: contratos, responsáveis, notificações e relatórios.
- `INSPECTOR`: contratos vinculados à pessoa indicada pelo claim UUID `person_id`.
- `VIEWER`: consultas permitidas, sem mutações.

Ao abrir o frontend, usuários não autenticados são direcionados para `/login`.
O botão **Entrar com Keycloak** inicia o Authorization Code Flow com PKCE e, após
o retorno, leva o usuário ao dashboard (ou à rota originalmente solicitada).
A aplicação não solicita nem armazena senha; o frontend mantém tokens apenas em
memória. O backend continua validando o JWT e sendo a autoridade final.

### Administração de usuários

Administradores acessam a central **Configurações** em `/administration`. A área
é composta por:

- `/administration/users`: usuários e permissões;
- `/administration/notifications`: configuração dos períodos de aviso;
- `/administration/system`: informações operacionais não sensíveis.

A página de usuários consulta `/api/v1/admin/users` e atualiza roles com
`PUT /api/v1/admin/users/{id}/roles`. Tanto a rota Angular quanto a API exigem
ADMIN; o backend nunca confia somente no controle visual do frontend.

O backend usa client credentials para acessar a API administrativa do Keycloak.
No Compose, `KEYCLOAK_ADMIN_BASE_URL` é ajustada para a rede interna e as demais
variáveis vêm do `.env`. O client importado precisa manter as roles de service
account `manage-users`, `query-users`, `view-users` e `view-realm`; essas roles não
são atribuídas aos usuários da aplicação. Se o volume antigo do Keycloak já tiver
sido inicializado antes da inclusão desse client, configure-o manualmente no
Keycloak ou inicialize um volume local novo.

O endpoint altera somente `ADMIN`, `CONTRACT_MANAGER`, `INSPECTOR` e `VIEWER`,
preserva outras roles internas do Keycloak e impede que um administrador remova a
role ADMIN do próprio usuário pela aplicação.

O gerenciamento de permissões realizado pela aplicação modifica as roles do
usuário no Keycloak através do backend. Nenhuma senha, client secret, JWT ou
credencial administrativa é exibida no frontend.

## Execução sem Docker

Pré-requisitos: Java 21, Maven, Node.js 22+, npm e PostgreSQL.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
cd frontend
npm ci
npm start
```

## Testes e builds

```bash
cd backend
mvn test
mvn package
```

A suíte backend inclui integração com PostgreSQL real via Testcontainers quando
Docker está disponível.

```bash
cd frontend
npm test -- --watch=false
npm run build
```

```bash
docker compose build
```

## Operação e segurança

- Flyway valida e aplica oito migrations; `ddl-auto=validate` impede criação
  implícita de tabelas.
- Constraints únicas protegem número do contrato, CPF informado, prazos,
  schedules e notificações por contrato/pessoa/vigência/prazo/canal.
- O scheduler busca `PENDING` com `scheduledDate <= hoje`, em lotes configuráveis,
  e usa lock pessimista para impedir processamento simultâneo entre instâncias.
- Actuator expõe somente `health` e `info`, sem detalhes internos.
- Erros inesperados são registrados por tipo e caminho; respostas ao cliente não
  incluem stack trace, SQL, tokens ou mensagens internas.
- Logs não devem conter senhas, JWTs completos, tokens de API ou destinatários.

## Limitações conhecidas do MVP

- Não há retentativa automática de notificações falhas nem fila externa.
- Envio SMTP/WhatsApp depende de providers e credenciais externos.
- XLSX e PDF ainda não possuem exporters; somente CSV está habilitado.
- O Keycloak do Compose usa `start-dev` e não é configuração de produção.
- A listagem de pessoas ainda não é paginada.
- O processamento mantém transação/lock durante a chamada ao provider; para alto
  volume, recomenda-se outbox/fila e workers independentes.
- Não há alta disponibilidade, backup ou observabilidade distribuída prontos.

Consulte [CHANGELOG.md](CHANGELOG.md) para o conteúdo da primeira versão do MVP.
