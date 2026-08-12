# Changelog

Todas as alterações relevantes deste projeto serão registradas neste arquivo.

## [0.1.0] - 2026-08-12

Primeira versão do MVP do Sistema de Gestão de Contratos.

### Funcionalidades

- Gestão de contratos, pessoas e responsáveis com preservação de histórico.
- Pesquisa e filtros de contratos, dashboard e relatórios CSV.
- Prazos configuráveis, schedules de vencimento e reagendamento de vigência.
- Processamento diário de notificações por e-mail e WhatsApp configurável.
- Histórico de auditoria e consulta de notificações/falhas.
- Autenticação Keycloak, autorização por papéis e restrição por inspetor.
- Aplicação Angular responsiva com tratamento padronizado de erros e loading.
- Ambiente Docker Compose com PostgreSQL, Keycloak, backend e frontend.

### Persistência e segurança

- Oito migrations Flyway com constraints, chaves estrangeiras e índices.
- Unicidade de contratos, CPF, prazos, schedules e entregas de notificação.
- CORS explícito por origem, Actuator restrito e respostas de erro sanitizadas.
- Lock pessimista e processamento em lote para concorrência do scheduler.
- Consultas com entity graphs para evitar N+1 em notificações e relatórios.

### Testes

- Testes unitários, de controller, segurança, persistência e integração.
- Fluxo PostgreSQL real coberto por Testcontainers.
- Testes Angular e builds de produção para backend, frontend e imagens Docker.

### Limitações

- Somente exportação CSV; XLSX e PDF estão planejados.
- Sem fila/outbox e sem retentativa automática de notificações.
- Configuração Keycloak do Compose destinada apenas ao desenvolvimento.
