# Sistema de Gestão de Contratos

Aplicação para centralizar o cadastro e o acompanhamento de contratos, responsáveis, vigências e notificações de vencimento.

## Estado atual

O backend Spring Boot está inicializado com sua infraestrutura técnica e sem entidades ou funcionalidades de negócio. O frontend ainda não foi inicializado.

## Arquitetura planejada

- `backend/`: API REST em Java 21 com Spring Boot e Maven, persistência PostgreSQL, migrations Flyway e autenticação OAuth2/JWT via Keycloak.
- `frontend/`: aplicação Angular com TypeScript, Angular Material, Reactive Forms e Angular Router.
- `docker/`: arquivos auxiliares de infraestrutura e containers.
- PostgreSQL: armazenamento transacional de contratos, pessoas, vínculos, notificações e auditoria.
- Keycloak: autenticação e autorização baseada em papéis.

A solução será organizada em camadas, mantendo controllers responsáveis pelo protocolo HTTP, services pelas regras de negócio e repositories pela persistência. Alterações no banco serão versionadas exclusivamente por migrations.

## Estrutura

```text
.
├── backend/
├── frontend/
├── docker/
├── .env.example
├── .gitignore
└── README.md
```

Os diretórios vazios são preservados por arquivos `.gitkeep` até a inicialização das aplicações.

## Configuração local

Copie `.env.example` para `.env` e substitua os valores de exemplo somente no ambiente local. O arquivo `.env` é ignorado pelo Git e não deve conter credenciais destinadas ao versionamento.

### Backend

Pré-requisitos: Java 21, Maven 3.6.3 ou superior e Docker com Docker Compose.

Crie o arquivo local de ambiente e ajuste os valores se necessário:

```bash
cp .env.example .env
```

No PowerShell, o comando equivalente é `Copy-Item .env.example .env`.

### Ambiente completo com Docker Compose

Com Docker em execução, suba PostgreSQL, Keycloak, backend e frontend a partir
da raiz do repositório:

```bash
docker compose up --build
```

Em segundo plano, use `docker compose up --build -d`. A aplicação fica em
`http://localhost:4200`, a API em `http://localhost:8080` e o Keycloak em
`http://localhost:8081`. O realm `contract-manager`, o cliente público e os
papéis iniciais são importados automaticamente; nenhum usuário é criado.

Os volumes `postgres_data` e `keycloak_data` preservam os dados. Para parar sem
removê-los, execute `docker compose down`. Use `docker compose down -v` somente
quando quiser apagar deliberadamente todos os dados locais.

Os valores do `.env.example` são apenas placeholders de desenvolvimento. Copie
o arquivo para `.env` e substitua senhas e endereços antes de qualquer ambiente
compartilhado. O modo `start-dev` do Keycloak não deve ser usado em produção.

Inicie somente o PostgreSQL de desenvolvimento, a partir da raiz do repositório:

```bash
docker compose up -d postgres
docker compose ps
```

O volume nomeado `postgres_data` preserva os dados entre reinicializações. Para interromper o serviço sem apagar o volume, execute `docker compose stop postgres`.

As configurações de conexão são fornecidas exclusivamente pelas variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`. Os valores presentes em `.env.example` são apenas exemplos para desenvolvimento local.

O backend atua como OAuth2 Resource Server e valida a assinatura, o emissor e a
validade dos JWTs emitidos pelo Keycloak. Configure `KEYCLOAK_ISSUER_URI` com a URL
exata do realm, por exemplo `http://localhost:8081/realms/contract-manager`.
Todas as rotas `/api/v1/**` exigem um bearer token válido. Permanecem públicos
somente o health/info do Actuator e a documentação OpenAPI.

O frontend utiliza Authorization Code Flow com PKCE pelo adaptador oficial do
Keycloak. Configure `url`, `realm` e `clientId` nos arquivos em
`frontend/src/environments`. O cliente deve ser público, ter Standard Flow
habilitado e aceitar as URLs de redirect e web origins do frontend. Os tokens
permanecem somente em memória, as chamadas para o backend recebem o bearer token
automaticamente e as rotas da aplicação exigem login.

Os papéis de realm reconhecidos são `ADMIN`, `CONTRACT_MANAGER`, `INSPECTOR` e
`VIEWER`. Configure no Keycloak um protocol mapper que inclua no access token o
claim `person_id` com o UUID da `Person` vinculada ao usuário `INSPECTOR`. O
backend exige esse claim e restringe as consultas do inspetor aos contratos em
que essa pessoa possui vínculo; controles visuais do Angular são apenas UX.

Com o banco saudável, execute a aplicação:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testes de integração PostgreSQL

A suíte Maven inclui um fluxo de integração com PostgreSQL 16 real via
Testcontainers. Com Docker em execução, use `mvn test`; o container é temporário,
recebe todas as migrations Flyway e é removido automaticamente ao final.

O Flyway é executado automaticamente durante a inicialização e mantém seu histórico no PostgreSQL. Ainda não há migrations SQL porque esta etapa não requer extensões, schemas adicionais ou tabelas de negócio; migrations vazias não são criadas.

Para validar e empacotar o backend:

```bash
cd backend
mvn test
mvn package
```

O endpoint técnico público fica disponível em `GET /actuator/health`. A documentação OpenAPI pode ser consultada em `/v3/api-docs` e `/swagger-ui.html`.

O Actuator expõe publicamente somente `/actuator/health` e `/actuator/info`; o
health não revela componentes nem detalhes internos. Os logs operacionais
registram o ciclo do scheduler, quantidades e identificadores técnicos de
schedules/notificações. Senhas, JWTs completos, tokens de API, destinatários e
conteúdo de credenciais não devem ser incluídos nos logs.

## Próximas etapas

As próximas etapas inicializarão separadamente o backend Spring Boot, o banco PostgreSQL e o frontend Angular. As instruções de execução e testes serão acrescentadas conforme cada componente for criado.
