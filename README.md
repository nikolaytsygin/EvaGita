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

```text
TODO
IN_PROGRESS
DONE