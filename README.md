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

Inicie somente o PostgreSQL de desenvolvimento, a partir da raiz do repositório:

```bash
docker compose up -d postgres
docker compose ps
```

O volume nomeado `postgres_data` preserva os dados entre reinicializações. Para interromper o serviço sem apagar o volume, execute `docker compose stop postgres`.

As configurações de conexão são fornecidas exclusivamente pelas variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`. Os valores presentes em `.env.example` são apenas exemplos para desenvolvimento local.

Com o banco saudável, execute a aplicação:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O Flyway é executado automaticamente durante a inicialização e mantém seu histórico no PostgreSQL. Ainda não há migrations SQL porque esta etapa não requer extensões, schemas adicionais ou tabelas de negócio; migrations vazias não são criadas.

Para validar e empacotar o backend:

```bash
cd backend
mvn test
mvn package
```

O endpoint técnico público fica disponível em `GET /actuator/health`. A documentação OpenAPI pode ser consultada em `/v3/api-docs` e `/swagger-ui.html`.

## Próximas etapas

As próximas etapas inicializarão separadamente o backend Spring Boot, o banco PostgreSQL e o frontend Angular. As instruções de execução e testes serão acrescentadas conforme cada componente for criado.
