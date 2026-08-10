# Vilicus — Self-hosted Personal Assistant

**Vilicus** (lat. *Vilicus* = Estate Manager) is a self-hosted personal assistant system for managing everyday challenges—finances, tasks, calendars—on Kubernetes.

## Quick Start

### Prerequisites
- Java 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 18 (via Docker)

### Local Development Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ityreh/vilicus.git
   cd vilicus
   ```

2. **Start PostgreSQL locally:**
   ```bash
   docker-compose up -d
   ```

3. **Build and run:**
   ```bash
   mvn clean spring-boot:run
   ```

4. **Access the application:**
   - Backend API: `http://localhost:8080`
   - PgAdmin: `http://localhost:5050` (admin@example.com / admin)

### Running Tests

```bash
mvn test
```

### Build Docker Image

```bash
mvn clean package
docker build -t vilicus-backend:latest .
```

## Project Structure

```
vilicus/
├── .github/
│   └── workflows/        # CI/CD pipelines
├── docs/                 # Documentation
├── src/
│   ├── main/
│   │   ├── java/com/vilicus/finance/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── entity/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/changelog/   # Liquibase migrations
│   └── test/
├── docker-compose.yml    # Local development environment
├── pom.xml              # Maven configuration
└── README.md
```

## Technology Stack

- **Backend:** Spring Boot 4.1
- **Language:** Java 25
- **Database:** PostgreSQL 18
- **Migrations:** Liquibase
- **Build:** Maven
- **Testing:** JUnit 5, Mockito, TestContainers
- **Deployment:** Kubernetes, Docker
- **Authentication:** JWT (15min expiration, refresh tokens)

## Documentation

- **[SETUP.md](docs/SETUP.md)** — Detailed setup guide
- **[Architecture](docs/ARCHITECTURE.md)** — System design & decisions
- **[API Reference](docs/API.md)** — REST endpoints
- **[Database Schema](docs/DATABASE.md)** — Entity relationships

## Development Workflow

**Branch Strategy:**
- `main` — Production-ready code
- `develop` — Integration branch for features
- `feature/*` — Feature branches (from develop)

**Commit Messages:** Follow [Conventional Commits](https://www.conventionalcommits.org/)

**CI/CD:** GitHub Actions runs tests on push to main/develop

## MVP Roadmap

### Phase 1: Foundation (Auth + DB) — Aug 10–24
- User authentication (JWT)
- PostgreSQL 18 schema & Liquibase migrations
- Account management setup

### Phase 2–6: See [Implementation Roadmap](docs/ROADMAP.md)

## License

GNU Affero General Public License v3.0 — See [LICENSE](LICENSE) file for details

## Contact

Yannick Rehberger — [yr@ityreh.de](mailto:yr@ityreh.de)
