# Seeho Download Center

## Project Overview

**Seeho Download Center** is a production-inspired backend subsystem designed as a
reference implementation for enterprise-grade download and export workflows.

The project is extracted and refined from the Seeho system, focusing on
task orchestration, asynchronous processing, and large-scale data export
under real-world constraints.

---

## Design Focus

> **This project focuses on backend architecture and system design.  
> Frontend UI is intentionally kept minimal.**

The primary goal of this repository is to demonstrate backend system behavior,
architectural boundaries, and engineering decisions rather than frontend
implementation details.

---

## Architecture Overview

The Download Center is designed as an independent backend subsystem responsible
for managing long-running export tasks.

Key architectural characteristics include:

- Asynchronous task execution
- Clear separation of domain logic and infrastructure
- Failure-aware task lifecycle management
- Batch processing to control memory usage
- Decoupled scheduling and execution

---

## Module Structure

```text
seeho-download-center
├── seeho-download-center-base
│   └── Common models, constants, and shared abstractions
├── seeho-download-center-domain
│   └── Core business logic and task orchestration
├── seeho-download-center-persistence
│   └── Data access layer (entities, mappers, repositories)
├── seeho-download-center-start
│   └── Application bootstrap and REST endpoints
└── pom.xml
```

Each module has a clearly defined responsibility boundary to ensure
maintainability, testability, and long-term evolution.

---

Execution Flow

A typical download workflow follows these steps:

1.  A client submits a download request
2.  A download task is created and persisted
3.  Task execution is triggered asynchronously
4.  Data is exported in batches to avoid memory pressure
5.  Task status is updated and exposed for querying

This design keeps API responses fast while safely handling large datasets.

---

# Running the Project

This repository is designed to be compilable and runnable with minimal setup.

### Prerequisites

* JDK 17+
* Maven 3.8+

### Build & Run

### Build

```bash
mvn clean package
```

### Run

```bash
cd seeho-download-center-start
mvn spring-boot:run
```

A minimal configuration is provided for local exploration and code reading. No full production environment is required.

---

## Scope & Non-Goals

This repository intentionally does **not** include:

- Authentication or authorization systems
- Production-grade message broker or scheduler deployment
- Full frontend UI implementations
- Infrastructure provisioning (Docker, Helm, etc.)

The focus is on backend orchestration patterns and architectural design.

---

## Documentation

Additional design notes and analysis are provided in the repository, including:

- Thread-safety analysis
- Task queue configuration notes
- Refactoring notes on producer–consumer decoupling

Please refer to the markdown documents in the repository root for details.

---

## License

This project is licensed under the MIT License.
