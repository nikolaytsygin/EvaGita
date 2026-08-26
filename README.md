# EvaGita

> Production-like task management platform built as a DevOps portfolio project.

EvaGita — современный веб-сервис для управления личными и командными задачами.

Основная функциональность приложения намеренно остаётся относительно простой. Главная цель проекта — построить вокруг приложения полноценную production-like инфраструктуру и на практике продемонстрировать DevOps-инженерные подходы.

---

## Project Goals

EvaGita создаётся как практический DevOps-пет-проект и портфолио.

Основные цели:

- разработка полноценного веб-приложения;
- контейнеризация;
- CI/CD;
- Kubernetes / K3s;
- Helm;
- PostgreSQL;
- RabbitMQ;
- monitoring;
- centralized logging;
- alerting;
- security;
- backup and recovery;
- disaster recovery;
- failure testing;
- эксплуатация production-like окружения на собственном сервере.

Главный принцип проекта:

> Мы не усложняем приложение ради DevOps. Мы усложняем инфраструктуру только там, где это имеет инженерный смысл.

---

## Application

EvaGita — task management platform в стиле современных productivity-приложений.

### Основные возможности

- регистрация пользователя;
- авторизация;
- JWT authentication;
- создание задач;
- редактирование задач;
- удаление задач;
- статусы задач;
- приоритеты;
- сроки выполнения;
- проекты;
- теги;
- поиск;
- фильтрация;
- dashboard;
- уведомления;
- аналитика;
- dark / light theme.

### Task statuses

- `TODO`
- `IN_PROGRESS`
- `DONE`

### Task priorities

- `LOW`
- `MEDIUM`
- `HIGH`

---

## Current Architecture

На текущем этапе реализуется backend как модульный монолит.

Текущий поток обработки запроса:

**REST API → Controller → DTO + Validation → Service → Repository → PostgreSQL**

Текущий backend построен на Spring Boot.

---

## Planned Production Architecture

Финальная архитектура будет развиваться постепенно.

Основной поток:

**Internet → DNS → Ingress / Nginx → Frontend → API Gateway → Application Services**

Основные backend-компоненты:

- Auth Service;
- Task Service;
- Notification Service;
- PostgreSQL;
- RabbitMQ.

### Observability

Целевая observability-архитектура:

**Application → Prometheus → Grafana**

**Application → Promtail → Loki → Grafana**

**Prometheus → Alertmanager → Telegram / Email**

---

## Technology Stack

### Application

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- RabbitMQ

### Frontend

- To be defined during implementation

### Containers

- Docker
- Docker Compose

### Orchestration

- Kubernetes
- K3s
- Helm

### CI/CD

- GitHub Actions
- Container Registry

### Monitoring

- Prometheus
- Grafana
- Node Exporter
- kube-state-metrics

### Logging

- Loki
- Promtail

### Alerting

- Alertmanager

### Security

- Kubernetes Secrets
- RBAC
- NetworkPolicy
- Trivy
- non-root containers
- HTTPS
- firewall

### Infrastructure

- Linux
- Nginx / Ingress
- cert-manager
- Let's Encrypt

---

## Repository Structure

Основная структура репозитория:

- `app/` — application-level components;
- `backend/` — backend application;
- `frontend/` — frontend application;
- `deploy/` — deployment and infrastructure;
- `monitoring/` — monitoring and observability;
- `.github/workflows/` — CI/CD workflows;
- `docker-compose.yml` — local development stack;
- `README.md` — project documentation;
- `LICENSE` — project license.

Структура репозитория будет постепенно развиваться по мере реализации проекта.

---

# Development Roadmap

## Phase 1 — Foundation

- [x] Repository structure
- [x] Spring Boot backend
- [x] PostgreSQL
- [x] Task entity
- [x] Task repository
- [x] Repository integration tests
- [x] Task service
- [x] Service unit tests
- [x] REST controller
- [x] REST API error handling
- [x] Task DTOs
- [x] Request validation
- [x] Controller validation tests

## Phase 2 — MVP

- [ ] User entity
- [ ] User repository
- [ ] User service
- [ ] Registration
- [ ] Authentication
- [ ] JWT
- [ ] User-owned tasks
- [ ] Projects
- [ ] Tags
- [ ] Search
- [ ] Filtering
- [ ] Dashboard
- [ ] Notifications
- [ ] Frontend

## Phase 3 — Production Server

- [ ] Linux server preparation
- [ ] SSH hardening
- [ ] Firewall
- [ ] DNS
- [ ] Nginx
- [ ] HTTPS
- [ ] Docker deployment

## Phase 4 — CI/CD

- [ ] CI pipeline
- [ ] Unit tests
- [ ] Build
- [ ] Docker image build
- [ ] Security scan
- [ ] Container Registry
- [ ] Automated deployment

## Phase 5 — Kubernetes

- [ ] K3s cluster
- [ ] Namespaces
- [ ] Deployments
- [ ] Services
- [ ] ConfigMaps
- [ ] Secrets
- [ ] Persistent volumes
- [ ] Health probes
- [ ] Resource requests / limits
- [ ] Ingress

## Phase 6 — Helm

- [ ] Helm chart
- [ ] Development values
- [ ] Production values
- [ ] Automated Helm deployment

## Phase 7 — Observability

- [ ] Prometheus
- [ ] Grafana
- [ ] Application metrics
- [ ] Kubernetes metrics
- [ ] Loki
- [ ] Promtail
- [ ] Alertmanager
- [ ] Telegram / Email notifications

## Phase 8 — Security

- [ ] RBAC
- [ ] NetworkPolicy
- [ ] Container hardening
- [ ] Trivy scanning
- [ ] Secret management
- [ ] Security headers

## Phase 9 — Reliability

- [ ] PostgreSQL backups
- [ ] Backup verification
- [ ] Restore procedure
- [ ] Disaster Recovery documentation
- [ ] Failure testing
- [ ] Recovery measurements

## Phase 10 — Portfolio

- [ ] Architecture diagrams
- [ ] Deployment documentation
- [ ] CI/CD documentation
- [ ] Monitoring documentation
- [ ] Security documentation
- [ ] Backup documentation
- [ ] Failure scenarios
- [ ] Screenshots
- [ ] Production demo
- [ ] Lessons learned

---

## DevOps Lifecycle

Целевой процесс:

**Developer → Git → CI → Tests → Build → Docker Build → Security Scan → Container Registry → CD → Kubernetes / K3s → Production**

Observability работает поверх production:

**Metrics + Logs + Alerts → Monitoring Stack**

---

## Observability

### Metrics

- Application metrics;
- Prometheus;
- Grafana;
- Kubernetes metrics;
- Node Exporter;
- kube-state-metrics.

### Logs

- Promtail;
- Loki;
- Grafana.

### Alerts

- Prometheus;
- Alertmanager;
- Telegram;
- Email.

---

## Reliability

EvaGita должна поддерживать проверяемые сценарии отказоустойчивости.

### Planned scenarios

1. Application pod failure
2. Worker node failure
3. PostgreSQL failure
4. Application deployment failure
5. PostgreSQL restore from backup

Для каждого сценария будут фиксироваться:

- причина;
- ожидаемое поведение;
- фактическое поведение;
- время восстановления;
- monitoring signals;
- alerts;
- logs.

---

## Backup and Disaster Recovery

Планируемый процесс резервного копирования:

**PostgreSQL → pg_dump → Compressed Backup → Remote Storage**

Backup считается рабочим только после успешного восстановления и проверки данных.

---

## API

REST API использует versioned prefix:

`/api/v1`

Основные группы:

- `/auth`
- `/users`
- `/tasks`
- `/projects`
- `/tags`
- `/notifications`
- `/analytics`

API должен использовать корректные HTTP status codes и единый формат ошибок.

---

## Current Status

### Current branch

`main`

### Current implementation

Backend foundation:

- Entity;
- Repository;
- Integration tests;
- Service;
- Unit tests;
- REST Controller;
- Error handling;
- DTO;
- Request validation;
- Validation tests.

### Current test status

**22 tests — 0 failures — 0 errors**

### Current Git status

На момент последнего этапа рабочее дерево было чистым.

Последний реализованный этап:

**Task request validation coverage**

---

## Production

Production environment:

- Status: Planned
- Domain: To be configured
- HTTPS: Planned
- Kubernetes: Planned
- Monitoring: Planned

---

## Project Philosophy

EvaGita развивается постепенно.

Каждый этап должен приводить к рабочему и тестируемому состоянию системы.

Проект должен демонстрировать не только:

> "I know this technology."

но и:

> "I used this technology to solve a real engineering problem."

Приложение намеренно остаётся относительно лёгким.

Основной инженерный фокус проекта:

- инфраструктура;
- автоматизация;
- observability;
- reliability;
- security;
- deployment;
- CI/CD.

---

## Portfolio

Финальный проект должен демонстрировать практическую работу с:

- Linux;
- Docker;
- Docker Compose;
- Kubernetes;
- K3s;
- Helm;
- PostgreSQL;
- RabbitMQ;
- Git;
- GitHub Actions;
- CI/CD;
- Prometheus;
- Grafana;
- Loki;
- Promtail;
- Alertmanager;
- Nginx;
- HTTPS;
- Security;
- Backup;
- Disaster Recovery.

---

## License

See [LICENSE](LICENSE).
