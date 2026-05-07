# Smart Soil Nutrient Monitoring & Crop Advisory System
## AUCA SENG 8240 — Final Exam Project | All Phases
### Student: Benjamin Mugisha Prince | ID: 26979
### Instructor: RUTARINDWA JEAN PIERRE

---

## Project Overview
The **Smart Soil Nutrient Monitoring and Crop Advisory System** is a Spring Boot REST API
built for the Rwanda Agriculture and Animal Resources Development Board (RAB).
It digitizes soil health analysis, generates personalized crop advisories, and sends
automated disease alerts — solving 6 critical problems identified in Phase 1.

---

## Design Patterns Implemented
| Pattern | Class | Purpose |
|---|---|---|
| **Singleton** | `DatabaseConnectionManager` | Single shared DB connection |
| **Factory Method** | `SoilReportFactory` | Dynamic report type creation |
| **Observer** | `DiseaseAlertSystem` | Multi-actor disease notifications |
| **Strategy** | `FertilizerStrategyFactory` | Soil-type-specific fertilizer algorithms |
| **Facade** | `SmartSoilFacade` | Unified API gateway for all subsystems |
| **Template Method** | `DailyReportTemplate` | Standardized report generation pipeline |

---

## Project Structure
```
smart-soil-system/
├── src/main/java/com/rab/smartsoil/
│   ├── SmartSoilApplication.java       # Spring Boot entry point
│   ├── model/                          # Domain entities (User, Farmer, SoilSample...)
│   ├── service/                        # Business logic (NutrientAnalyzer, Strategies...)
│   ├── alerts/                         # Observer pattern (DiseaseAlertSystem)
│   ├── facade/                         # Facade pattern (SmartSoilFacade)
│   ├── controller/                     # REST controllers
│   ├── repository/                     # Spring Data JPA repositories
│   ├── dto/                            # Request/Response DTOs
│   └── config/                         # Spring Security config
├── src/test/java/com/rab/smartsoil/
│   ├── service/NutrientAnalyzerTest.java   # 22 unit tests
│   └── service/DesignPatternTest.java      # Pattern verification tests
├── Dockerfile                          # Multi-stage Docker build
├── docker-compose.yml                  # Full stack orchestration
├── .gitignore                          # Git ignore rules
└── pom.xml                             # Maven dependencies
```

---

## Quick Start — Run with Docker
```bash
# 1. Clone the repository
git clone https://github.com/kamanzi26979/smart-soil-system.git
cd smart-soil-system

# 2. Start all containers
docker-compose up -d

# 3. Test the API
curl http://localhost:8080/api/health
curl http://localhost:8080/api/demo

# 4. Open H2 console (dev mode)
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:smart_soil_db
# User: rab_admin | Password: rab_pass
```

---

## API Endpoints
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/health` | System health check | Public |
| GET | `/api/demo` | Demo soil analysis | Public |
| POST | `/api/soil/submit` | Submit soil data → get advisory | Required |
| GET | `/api/soil/history/{plotId}` | Soil sample history | Required |
| GET | `/api/advisory/{plotId}` | Get advisories for plot | Required |
| GET | `/api/alerts/district/{district}` | Active disease alerts | Required |
| PATCH | `/api/alerts/{alertId}/treat` | Mark alert as treated | Required |

---

## Run Tests
```bash
# Run all JUnit 5 tests
mvn test

# Run standalone test suite (no Spring context needed)
javac -d classes src/main/java/com/rab/smartsoil/model/*.java \
                  src/main/java/com/rab/smartsoil/service/*.java \
                  src/main/java/com/rab/smartsoil/alerts/*.java
java -cp classes com.rab.smartsoil.test.TestRunner
```

---

## Git Version Control
```bash
git init
git remote add origin https://github.com/kamanzi26979/smart-soil-system.git
git add .
git commit -m "feat: initial commit — Smart Soil System all phases"
git push -u origin main
```
