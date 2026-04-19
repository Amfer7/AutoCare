# 🚗 AutoCare — Smart Vehicle Maintenance & Reminder Management System

> A full-stack Java web application built with Spring Boot that helps vehicle owners track maintenance schedules, receive automated reminders, manage service requests, and handle billing — all in one place.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=flat-square)
![License](https://img.shields.io/badge/License-Academic-lightgrey?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Design Principles](#design-principles)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [Screenshots](#screenshots)
- [Team](#team)

---

## Overview

AutoCare solves a common problem: vehicle owners forget or delay maintenance, leading to breakdowns and costly repairs. The system automatically generates maintenance reminders based on a **hybrid mileage-and-time threshold** — whichever is crossed first triggers the alert. A background scheduler evaluates all active reminders daily, progressing them through a well-defined state machine.

When a service is completed, the **Observer pattern** fires an event that automatically closes the old reminder and starts a fresh cycle, using the mileage recorded at the time of service as the new baseline.

---

## Features

### Vehicle Owner
- Register vehicles with make, model, year, mileage, fuel type, and colour
- Assign a maintenance policy (Standard / Premium / Heavy-Duty)
- View active reminders with urgency indicators (Upcoming, Due Soon, Overdue)
- Update odometer readings — enforced to always increase
- Submit service requests with descriptions
- View bills and mark payments

### Service Staff
- View all pending and in-progress service requests
- Assign technicians and start service
- Complete service with work performed, parts replaced, and billing details
- Live bill preview with GST calculation

### Administrator
- Manage user accounts (activate / deactivate)
- Create and configure maintenance policies
- View system-wide reports and outstanding payments

### System
- Background scheduler evaluates all active reminders daily at midnight
- Automatic reminder cycle reset on service completion (Observer pattern)
- Role-based access control across all routes

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Web Framework | Spring MVC |
| Security | Spring Security 6 |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8 |
| Template Engine | Thymeleaf 3 + Layout Dialect |
| Build Tool | Maven |
| Scheduling | Spring `@Scheduled` |

---

## Architecture

AutoCare follows a strict **Layered MVC architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                    VIEW LAYER                           │
│         Thymeleaf Templates (25 HTML files)             │
│         layout/base.html → shared sidebar + topbar      │
└──────────────────────┬──────────────────────────────────┘
                       │ Model attributes
┌──────────────────────▼──────────────────────────────────┐
│                 CONTROLLER LAYER                        │
│   AuthController · VehicleController · BillController  │
│   ServiceRequestController · AdminController · ...      │
└──────────────────────┬──────────────────────────────────┘
                       │ Interface calls (DIP)
┌──────────────────────▼──────────────────────────────────┐
│                  SERVICE LAYER                          │
│   VehicleService · ReminderService · BillingService    │
│   ServiceRequestService · MaintenancePolicyService      │
│                                                         │
│   ┌─────────────┐  ┌─────────────┐  ┌───────────────┐  │
│   │  Strategy   │  │   Factory   │  │   Observer    │  │
│   │  (Policy    │  │  (Reminder  │  │ (Completion   │  │
│   │  Evaluation)│  │  Creation)  │  │  Event)       │  │
│   └─────────────┘  └─────────────┘  └───────────────┘  │
│                         ┌─────────────┐                 │
│                         │  Singleton  │                 │
│                         │ (Scheduler) │                 │
│                         └─────────────┘                 │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA Repositories
┌──────────────────────▼──────────────────────────────────┐
│                REPOSITORY LAYER                         │
│   UserRepository · VehicleRepository · BillRepository  │
│   ReminderRepository · ServiceRequestRepository · ...   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                    MySQL DATABASE                       │
└─────────────────────────────────────────────────────────┘
```

### Reminder State Machine

```
  ┌──────────┐    threshold    ┌──────────┐    threshold    ┌─────────┐
  │ UPCOMING │ ─────────────▶ │ DUE_SOON │ ─────────────▶ │ OVERDUE │
  └──────────┘   approaching   └──────────┘    crossed      └────┬────┘
                                                                  │
       ◀─────────────── new cycle started ──────────────────      │ service
       │                                                          │ completed
  ┌────┴─────┐                                                    │
  │ UPCOMING │  ◀──────────────────────────────────────────────── ┘
  └──────────┘         (Observer resets cycle)
```

### Service Request State Machine

```
PENDING ──▶ IN_PROGRESS ──▶ COMPLETED
   │              │
   └──────────────┴──▶ CANCELLED
```

---

## Design Patterns

### 1. Strategy Pattern *(Behavioral)*
`MaintenancePolicyStrategy` defines the interface for reminder state evaluation. `StandardPolicyStrategy` and `PremiumPolicyStrategy` are concrete implementations. `ReminderServiceImpl` resolves the correct strategy at runtime from a Spring-injected `Map<String, MaintenancePolicyStrategy>` — adding a new policy requires only a new `@Component`, zero changes to existing code.

```
MaintenancePolicyStrategy (interface)
    ├── StandardPolicyStrategy  @Component("standardPolicyStrategy")
    └── PremiumPolicyStrategy   @Component("premiumPolicyStrategy")
```

### 2. Factory Pattern *(Creational)*
`ReminderFactory` centralises all `Reminder` construction. It computes mileage thresholds (`dueMileage = baseline + policy.mileageInterval`) and date thresholds (`dueDate = today + policy.timeIntervalDays`). Two methods: `createReminder()` for initial registration, `createNextReminder(mileageAtService)` for post-service cycle restarts.

### 3. Observer Pattern *(Behavioral)*
`ServiceCompletionEvent` (Spring `ApplicationEvent`) is published by `ServiceRequestServiceImpl` on service completion. `ServiceCompletionListener` reacts by closing the active reminder, creating the next cycle via `ReminderFactory`, and resetting the vehicle state to `ACTIVE`. The publisher holds zero references to the listener.

### 4. Singleton Pattern *(Creational)*
`ReminderScheduler` is a Spring `@Component`, guaranteeing a single instance per application context. Runs daily at midnight via `@Scheduled(cron = "0 0 0 * * *")` and once on startup after a 5-minute delay.

---

## Design Principles

| Principle | Where Applied |
|-----------|--------------|
| **SRP** | `BillingService` handles only billing. `ServiceRecord` stores operational data; `Bill` stores financial data. `ReminderScheduler` only triggers — logic lives in `ReminderService`. |
| **OCP** | New maintenance policies implement `MaintenancePolicyStrategy` without modifying existing evaluators. Strategy auto-discovered via Spring bean map. |
| **LSP** | `VehicleOwner`, `ServiceStaff`, `Admin` are fully substitutable as `User` in all repositories, services, and Spring Security. |
| **DIP** | Controllers depend on service interfaces. `ReminderServiceImpl` depends on `MaintenancePolicyStrategy`, not `StandardPolicyStrategy`. Spring wires concrete implementations at runtime. |

---

## Project Structure

```
src/main/
├── java/com/autocare/
│   ├── AutoCareApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── ThymeleafConfig.java
│   │   ├── SchedulerConfig.java
│   │   ├── AuthenticationHelper.java
│   │   └── DataInitializer.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── DashboardController.java
│   │   ├── VehicleController.java
│   │   ├── ReminderController.java
│   │   ├── ServiceRequestController.java
│   │   ├── BillController.java
│   │   ├── AdminController.java
│   │   └── GlobalExceptionHandler.java
│   ├── model/
│   │   ├── entity/          # JPA Entities
│   │   ├── enums/           # ReminderState, VehicleState, etc.
│   │   └── dto/             # Data Transfer Objects
│   ├── repository/          # Spring Data JPA interfaces
│   ├── service/
│   │   ├── impl/            # Service implementations
│   │   └── policy/          # Strategy pattern (MaintenancePolicyStrategy)
│   ├── factory/             # ReminderFactory
│   ├── observer/            # ServiceCompletionEvent + Listener
│   └── scheduler/           # ReminderScheduler
└── resources/
    ├── application.properties
    ├── static/css/autocare.css
    └── templates/           # 25 Thymeleaf HTML files
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/autocare.git
cd autocare
```

### 2. Create the database

```sql
CREATE DATABASE autocare_db;
```

### 3. Configure `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/autocare_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

spring.thymeleaf.cache=false
server.port=8080
app.billing.tax-rate=18.0
```

### 4. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### 5. Open in browser

```
http://localhost:8080
```

> On first run, `DataInitializer` automatically seeds the database with 3 maintenance policies (Standard, Premium, Heavy-Duty) and a default admin account.

---

## Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Administrator | `admin@autocare.com` | `Admin@1234` |

Register Vehicle Owners and Service Staff via `/auth/register`.

---

## Screenshots

> *(Add screenshots here after running the application)*

| Page | Description |
|------|-------------|
| Login | Auth page with hero panel |
| Owner Dashboard | Stat cards, vehicle list, reminder summary |
| Vehicle Detail | Odometer, reminder state, history |
| Reminder List | Filter by state, progress bars, urgency badges |
| Service Request | Complete service form with live bill preview |
| Bill View | Invoice layout with GST breakdown |
| Admin Dashboard | System reports, unpaid bills |

---

## Team

| Name | SRN | Module |
|------|-----|--------|
| Member 1 | SRN | Vehicle Registration + Reminder View |
| Member 2 | SRN | Mileage Tracking + Scheduler |
| Member 3 | SRN | Service Request Lifecycle |
| Member 4 | SRN | Billing + Admin Panel |

---

## Course Details

**Course:** UE23CS352B — Object Oriented Analysis & Design  
**Institution:** PES University, Bengaluru  
**Semester:** January – May 2026
