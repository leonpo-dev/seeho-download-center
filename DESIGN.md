# Download Center Design

This document describes the architectural decisions and design rationale
behind the **Seeho Download Center**, focusing on asynchronous task
orchestration and decoupling strategies rather than implementation details.

---

## 1. Problem Background (Why)

Large-scale data exports embedded directly in business applications tend to
introduce several systemic problems:

- Request threads are blocked for long periods of time
- Memory usage becomes unpredictable when exporting large datasets
- Failures are hard to observe and recover
- User experience conflicts with platform stability

In real-world systems, synchronous export controllers must query data,
format files (e.g., Excel), stream results, and write to storage before
returning a response. This approach does not scale and frequently leads
to timeouts or memory exhaustion.

To address these issues, the Seeho system isolates export execution into
a dedicated **Download Center** subsystem. Responsibilities are clearly
separated: HTTP endpoints trigger tasks, domain logic orchestrates execution,
and persistence tracks task lifecycle and state transitions.

---

## 2. Design Goals (Goals)

The Download Center is designed with the following goals:

1. **Non-blocking APIs**  
   HTTP endpoints create and query download tasks without waiting for file
   generation. Task metadata is persisted and returned immediately.

2. **Recoverable execution**  
   All task metadata—including parameters, column definitions, retry count,
   and message keys—is persisted. Failed tasks can be resumed without losing
   context.

3. **Observable task lifecycle**  
   Task status transitions (`NOT_EXECUTED`, `EXECUTING`, `FAILED`, `SUCCESS`)
   are first-class domain concepts. Clients can query progress and results
   through dedicated APIs.

4. **Decoupling from business logic**  
   Business services only implement a data-provider interface
   (`QueryExportDataService`). They remain unaware of queues, retries, or
   file-generation mechanics.

---

## 3. Architecture Choices (Key Decisions)

### Why introduce asynchronous messaging?

Asynchronous execution is essential to decouple request ingress from
long-running export logic.

In this design, the messaging layer acts as a seam between task creation
and task execution:

- HTTP controllers persist tasks and return immediately
- Tasks are enqueued for later processing
- Consumers execute exports independently of request threads

Even when a full-featured message broker is not present, the system enforces
the same asynchronous model through an in-memory queue. This ensures that
architectural decisions are not coupled to a specific middleware choice.

### Why keep messaging outside the domain layer?

The domain layer depends only on a minimal abstraction
(`SendTaskToMQService`) rather than concrete MQ clients.

This provides several benefits:

- **Infrastructure independence**  
  No RocketMQ-specific annotations or libraries leak into domain code.

- **Replaceability**  
  The messaging backend can evolve from in-memory queues to RocketMQ,
  Kafka, or scheduled jobs without impacting core business logic.

- **Testability**  
  Domain logic can be tested with no-op or in-memory implementations,
  avoiding broker dependencies in unit tests.

---

## 4. Decoupling Model

> **The domain layer defines what should happen,  
> while the messaging layer defines how and when it happens.**

The execution flow is intentionally split across layers:

- **Domain publishes task intent**  
  Task creation validates input, persists metadata, and emits an execution
  intent through a messaging abstraction.

- **Messaging layer schedules execution**  
  Queues (in-memory or external) control concurrency, delays, and retries
  without knowledge of export logic.

- **Consumers execute strategies**  
  Consumers locate the appropriate data provider, stream data in batches,
  generate files, and update task state.

This separation allows domain experts to add new export logic while
infrastructure engineers tune execution behavior independently.

---

## 5. Failure & Idempotency Strategy

Failure handling is designed as an explicit workflow rather than an
implicit side effect of transactions.

- **Retry placement**  
  Consumers update task state to `FAILED`. A scheduled job identifies
  retryable tasks and re-enqueues them based on retry thresholds.

- **State alignment**  
  Task state transitions use optimistic conditions to ensure that only
  one worker can execute a task at a time, preventing duplicate processing.

- **Why not rely on a single transaction?**  
  Export execution spans database access, file system I/O, streaming, and
  optional uploads. Wrapping these steps in a single transaction would
  hold locks for extended periods and reduce system throughput.

This design favors **explicit state transitions and eventual consistency**
over strict transactional guarantees—a deliberate trade-off to improve
scalability and fault isolation.

---

## 6. Scope & Boundaries (Scope)

This repository demonstrates **download-center design patterns**, not a
turn-key production environment.

The following aspects are intentionally simplified or omitted:

- Full message-broker deployment (e.g., RocketMQ)
- Distributed storage systems
- Authentication and authorization
- Infrastructure provisioning and observability stacks

This project is intended for engineers interested in backend architecture,
asynchronous task orchestration, and production-oriented design trade-offs,
rather than as a drop-in solution.

---

## 7. Summary

The Seeho Download Center illustrates how complex, long-running export
workflows can be isolated from request handling, made observable, and
executed safely at scale.

By separating domain intent from execution mechanics, the system remains
flexible, testable, and resilient—key qualities of production-grade backend
architectures.
