# Bank Account API

A Spring Boot REST API for managing bank accounts. It supports creating accounts, crediting and debiting balances in different currencies, performing currency exchanges, and querying balances.

---

## Table of contents

1. [Features](#features)
2. [Tech stack](#tech-stack)
3. [Getting started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Run with Docker Compose](#run-with-docker-compose)
   - [Run locally with Maven](#run-locally-with-maven)
4. [Configuration](#configuration)
   - [Environment variables](#environment-variables)
   - [Spring profiles](#spring-profiles)
5. [API reference](#api-reference)
   - [Create account](#post-accounts)
   - [Credit account](#post-accountsidentificationcredit)
   - [Debit account](#post-accountsidentificationdebit)
   - [Exchange currency](#post-accountsidentificationexchange)
   - [Get balance](#get-accountsidentificationbalance)
   - [Error responses](#error-responses)
6. [Testing](#testing)
7. [License](#license)

---

## Features

- RESTful API for working with bank accounts.
- Operations:
  - Create a new account.
  - Credit and debit an account in a specific currency.
  - Exchange funds between currencies on an account.
  - Retrieve the current balance for a given currency.
- PostgreSQL as a database.
- Separate **dev** and **prod** profiles.
- Docker and Docker Compose support.
- Automated tests (unit, controller, integration).

---

## Tech stack

- **Language:** Java (Spring Boot)
- **Build tool:** Maven (with wrapper `mvnw` / `mvnw.cmd`)
- **Frameworks/Libraries:**
  - Spring Boot
  - Spring Web
  - Spring Data JPA
  - Jakarta Validation
  - Lombok
- **Database:** PostgreSQL
- **Containerization:** Docker, Docker Compose

---

## Getting started

### Prerequisites

- **Java:** JDK version configured in `pom.xml` (typically 17+).
- **Maven:** Optional; the Maven wrapper can be used.
- **Docker & Docker Compose:** Recommended for running app + DB together.
- **PostgreSQL:** Only required if you run everything locally without Docker.

---

### Run with Docker Compose

From the project root:

```powershell
docker-compose up
```
 or
```powershell
docker-compose up --build
```

This will:

- Build the application image using the `Dockerfile`. (if `--build` is used, otherwise it uses the existing image if available)
- Start PostgreSQL and the bank-account application according to `docker-compose.yml`.

After startup, the API is typically available at:

- `http://localhost:8080`

To stop and remove containers:

```powershell
docker-compose down
```

---

### Run locally with Maven

To run the application directly on your machine without using Docker:

1. Start a PostgreSQL instance and ensure it is reachable.
2. Set the environment variables for your chosen profile (see below).
3. From the project root, run:

```powershell
set SPRING_PROFILES_ACTIVE=dev
# example values – adjust to your setup
set DATASOURCE_URL_DEV=jdbc:postgresql://localhost:5432/bank-account
set DATASOURCE_USERNAME_DEV=bank_user
set DATASOURCE_PASSWORD_DEV=bank_password

.\mvnw.cmd spring-boot:run
```

On Unix-like systems:

```bash
export SPRING_PROFILES_ACTIVE=dev
# export DATASOURCE_URL_DEV=jdbc:postgresql://localhost:5432/bank-account
# export DATASOURCE_USERNAME_DEV=bank_user
# export DATASOURCE_PASSWORD_DEV=bank_password

./mvnw spring-boot:run
```

By default, the application listens on port `8080` unless overridden in configuration.

---

## Configuration

### Environment variables

These environment variables are used (directly or via Spring property binding) to configure database connections and active profile.

#### `DATASOURCE_URL_DEV`

JDBC URL for the **dev** datasource.

- Example:

```text
DATASOURCE_URL_DEV=jdbc:postgresql://localhost:5432/bank-account
```

#### `DATASOURCE_USERNAME_DEV`

Username for the dev PostgreSQL database.

- Example:

```text
DATASOURCE_USERNAME_DEV=bank_user
```

#### `DATASOURCE_PASSWORD_DEV`

Password for the dev PostgreSQL database.

- Example:

```text
DATASOURCE_PASSWORD_DEV=bank_password
```

#### `DATASOURCE_URL_PROD`

JDBC URL for the **prod** datasource.

- Example (Docker network):

```text
DATASOURCE_URL_PROD=jdbc:postgresql://db:5432/bank-account
```

#### `DATASOURCE_USERNAME_PROD`

Username for the prod PostgreSQL database.

- Example:

```text
DATASOURCE_USERNAME_PROD=bank_prod_user
```

#### `DATASOURCE_PASSWORD_PROD`

Password for the prod PostgreSQL database.

- Example:

```text
DATASOURCE_PASSWORD_PROD=bank_prod_password
```

#### `SPRING_PROFILES_ACTIVE`

Controls which Spring profile is active.

- Possible values: `dev`, `prod`
- Example (Windows PowerShell):

```powershell
set SPRING_PROFILES_ACTIVE=dev
```

If not set, Spring Boot falls back to its default profile and only `application.yml` is used.

---

### Spring profiles

- **dev**
  - Uses `application-dev.yml`.
- **prod**
  - Uses `application-prod.yml`.

Profiles can also be set via JVM arguments:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## API reference

All endpoints are under the `/accounts` base path, as defined in `AccountController`.

Unless otherwise noted:

- Request and response bodies are JSON.
- Validation is performed using Jakarta Validation annotations. Invalid requests return a 400 Bad Request with an error payload.

### `POST /accounts`

Create a new account.

**Request body** – `CreateAccountRequest`:

```json
{
  "identification": "1234567"
}
```

- `identification` (string, required): unique account identifier. Must not be null or blank and contain exactly 7 digits.

**Response body** – `AccountResponse`:

```json
{
  "identification": "1234567"
}
```

- Errors:
    - `400 Bad Request` for validation errors.

### `POST /accounts/{identification}/credit`

Credit an amount in a given currency to an existing account.

**Path variable**:

- `identification` (string, required): account identifier.

**Request body** – `Money`:

```json
{
  "currency": "EUR",
  "amount": 100.50
}
```

- `currency` (string, required): one of the values from the `Currency` enum (e.g. `EUR`, `USD`, `GBP`).
- `amount` (number, required): must be positive.

**Response**:

- `200 OK` on success.
- Errors:
  - `404 Not Found` if the account does not exist.
  - `400 Bad Request` for validation errors.

### `POST /accounts/{identification}/debit`

Debit an amount in a given currency from an existing account.

**Path variable**:

- `identification` (string, required): account identifier.

**Request body** – `Money`:

```json
{
  "currency": "EUR",
  "amount": 25.00
}
```

**Response**:

- `200 OK` on success.
- Errors:
  - `404 Not Found` if the account does not exist.
  - `400 Bad Request` for validation errors or is there are insufficient funds (as handled by `InsufficientFundsException`).

### `POST /accounts/{identification}/exchange`

Exchange funds between currencies within an account.

**Path variable**:

- `identification` (string, required): account identifier.

**Request body** – `ExchangeRequest`:

```json
{
  "from": "EUR",
  "to": "USD",
  "amount": 50.00
}
```

- `from` (string, required): source currency (enum `Currency`).
- `to` (string, required): target currency (enum `Currency`).
- `amount` (number, required): amount to exchange, must be positive.

**Response**:

- `200 OK` on success.
- Errors:
  - `404 Not Found` if the account does not exist.
  - `400 Bad Request` for validation errors or if there are insufficient funds in the source currency.

### `GET /accounts/{identification}/balance`

Get the balance of an account in a specific currency.

**Path variable**:

- `identification` (string, required): account identifier.

**Query parameter**:

- `currency` (string, required): desired currency (enum `Currency`).

**Example request**:

```text
GET /accounts/1234567/balance?currency=EUR
```

**Response body** – `Money`:

```json
{
  "currency": "EUR",
  "amount": 75.50
}
```

**Errors**:

- `404 Not Found` if the account does not exist.

### Error responses

Errors are handled centrally by `GlobalExceptionHandler` and typically return a JSON payload like `ErrorResponse`:

```json
{
  "message": "Account not found",
  "timestamp": "2026-03-11T12:34:56.789Z",
  "path": "/accounts/UNKNOWN/balance"
}
```

Exact fields may vary; inspect `ErrorResponse` and `GlobalExceptionHandler` for details.

Common HTTP status codes:

- `400 Bad Request` – validation errors or malformed requests.
- `404 Not Found` – account does not exist.
- `500 Internal Server Error` – unexpected server errors.

---

## Testing

To run the test suite:

```powershell
cd C:\sandbox\Projects\other\bank-account
.\mvnw.cmd test
```

If Maven is installed globally:

```powershell
cd C:\sandbox\Projects\other\bank-account
mvn test
```

---

## License

This project is provided for learning and demonstration purposes. If you plan to distribute or open-source it, add an explicit license (e.g. MIT, Apache 2.0) and reference it here.
