# Vilicus — Local Development Setup

## Prerequisites

- **Java 25** (Oracle JDK 25)
- **Maven 3.9+**
- **Docker** & **Docker Compose**
- **Git**

### Verify Installations

```bash
java -version         # Should show Java 25.x
mvn -version          # Should show Maven 3.9+
docker --version      # Should show Docker version
docker-compose --version  # Should show Docker Compose version
```

---

## 1. Clone the Repository

```bash
git clone https://github.com/ityreh/vilicus.git
cd vilicus
git checkout develop  # Ensure on develop branch
```

---

## 2. Setup Environment Variables

Copy the example environment file:

```bash
cp .env.local.example .env.local
```

Edit `.env.local` with your local credentials (optional for local dev):

```
DB_PASSWORD=postgres
PGADMIN_PASSWORD=admin
JWT_SECRET=your-dev-secret-key-at-least-32-chars
```

**Important:** Never commit `.env.local` to Git (it's in `.gitignore`)

---

## 3. Start PostgreSQL + PgAdmin

```bash
docker-compose up -d
```

Verify services are running:

```bash
docker-compose ps
```

You should see:
```
NAME               STATUS
vilicus-postgres   Up (healthy)
vilicus-pgadmin    Up
```

### Access PgAdmin

- URL: `http://localhost:5050`
- Email: `admin@example.com` (or value in `.env.local`)
- Password: `admin` (or value in `.env.local`)

**Add PostgreSQL Server in PgAdmin:**
1. Right-click "Servers" → "Register" → "Server"
2. **Name:** Vilicus Local
3. **Connection tab:**
   - Host: `vilicus-postgres`
   - Port: `5432`
   - Username: `postgres`
   - Password: `postgres`
4. Click "Save"

---

## 4. Build the Backend

```bash
mvn clean install
```

This will:
- Download dependencies
- Compile code
- Run tests (requires Docker service running)

---

## 5. Run the Application

### Option A: From Maven

```bash
mvn spring-boot:run
```

### Option B: From IDE

- Open project in IntelliJ IDEA or VS Code
- Configure Run Configuration:
  - Main class: `com.vilicus.finance.VilicusApplication`
  - VM options: (empty)
- Run

### Option C: From Built JAR

```bash
mvn clean package
java -jar target/vilicus-backend-1.0.0.jar
```

---

## 6. Verify the Backend

Backend should start on `http://localhost:8080`

Test the API:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## 7. Run Tests

### All Tests

```bash
mvn test
```

### Specific Test Class

```bash
mvn test -Dtest=UserEntityTest
```

### With Coverage

```bash
mvn test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

---

## 8. Useful Docker Commands

### Stop Services

```bash
docker-compose down
```

### Remove Volumes (reset database)

```bash
docker-compose down -v
```

### View Logs

```bash
docker-compose logs -f postgres
docker-compose logs -f pgadmin
```

### Access Postgres CLI

```bash
docker-compose exec postgres psql -U postgres -d vilicus
```

---

## 9. IDE Setup (IntelliJ IDEA)

1. **Open Project:**
   - File → Open → select `vilicus` folder
   - Trust project when prompted

2. **Configure JDK:**
   - Settings → Project Structure → Project
   - Set SDK to Java 25

3. **Run Configurations:**
   - Edit Configurations → + Maven
   - Name: `Spring Boot Dev`
   - Working directory: `$PROJECT_DIR`
   - Command: `spring-boot:run`
   - Profiles: (leave empty)

4. **Lombok Annotation Processing:**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Enable annotation processing: ✅

---

## 10. VS Code Setup

1. **Install Extensions:**
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (VMware)
   - Maven for Java (Microsoft)

2. **Configure JDK:**
   - Ctrl+Shift+P → "Java: Configure Runtime"
   - Select Java 25

3. **Run Spring Boot:**
   - Ctrl+Shift+D → Create launch.json (if not exists)
   - Use default Spring Boot launch config

---

## Common Issues

### Issue: `docker-compose: command not found`

**Solution:**
```bash
# Install Docker Compose v2
docker compose version

# If old version, use:
docker compose up -d  # v2 syntax
```

### Issue: `java.sql.SQLException: No suitable driver found`

**Solution:** Ensure PostgreSQL is running:
```bash
docker-compose ps
docker-compose logs postgres
```

### Issue: Port 5432 Already in Use

**Solution:** Change port in `docker-compose.yml`:
```yaml
postgres:
  ports:
    - "5433:5432"  # Use 5433 locally
```

Then update `SPRING_DATASOURCE_URL` in `.env.local`:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/vilicus
```

### Issue: Tests Fail with TestContainers

**Solution:** Ensure Docker daemon is running:
```bash
docker ps  # Should work without errors
```

---

## Next Steps

1. Read the [Architecture](ARCHITECTURE.md) document
2. Review the [API Specification](API.md)
3. Check the [Implementation Roadmap](ROADMAP.md) for Phase 1 tasks
4. Create a feature branch: `git checkout -b feature/your-feature develop`

---

## Support

For issues or questions:
- Check existing issues on GitHub
- Create a new issue with logs and environment details
- Contact: yr@ityreh.de
