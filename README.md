# Energy Communities Microservice Architecture

This project is a distributed microservice architecture simulating an energy community.

## ⚠️ Prerequisites
* **Java Version:** This project requires **Java 21** (e.g., Amazon Corretto 21 or Eclipse Temurin 21). Please ensure your IDE is utilizing Java 21 to avoid JavaFX Module compatibility issues.
* **Docker:** Required for the PostgreSQL Database and RabbitMQ message broker.
* **Build Tool:** Maven

---

## Step 1: Start the Infrastructure (Database & Message Broker)
Before starting any microservices, the underlying infrastructure must be running.

1. Open your terminal and navigate to the `docker` directory:
   ```bash 
   cd docker
   ```
2. Start the containers in the background:
   ```bash
   docker compose up -d
   ```

*(Note: This spins up PostgreSQL on port 5432 and RabbitMQ on port 5672).*

---

## Step 2: Start the Backend Microservices
Because these are independent components, they should be started via their dedicated Spring Boot Maven plugins. Because services communicate with each other, the startup order matters!

**1. Energy API (Port 8091)**
* In your IDE, run the `EnergyApiApplication.java` main class directly, OR use the Maven tool:
* energy-api -> Plugins -> spring-boot -> spring-boot:run

**2. Current Percentage Service (Port 8093)**
* current-percentage-service -> Plugins -> spring-boot -> spring-boot:run

**3. Usage Service (Port 8092)**
* *(Note: Must be started after the Percentage Service, as it sends an HTTP trigger to it).*
* usage-service -> Plugins -> spring-boot -> spring-boot:run

**4. Community Energy Producer (RabbitMQ Sender)**
* community-energy-producer -> Plugins -> spring-boot -> spring-boot:run

**5. Community Energy User (RabbitMQ Receiver)**
* community-energy-user -> Plugins -> spring-boot -> spring-boot:run

---

## Step 3: Start the Graphical User Interface (Frontend)
Once the backend services are running and listening on their respective ports, you can launch the standalone JavaFX UI.

* In the Maven tool window, navigate to:
* `energy-gui -> Plugins -> javafx -> javafx:run`

From the GUI, you can click "Refresh" to fetch data. The GUI communicates over HTTP to the independent backend APIs.

---

## Step 4: Cleanup
When finished testing, cleanly shut down the infrastructure and wipe the database volumes:
```bash
docker compose down -v
```
