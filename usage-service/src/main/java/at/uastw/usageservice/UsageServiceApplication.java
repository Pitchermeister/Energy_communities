package at.uastw.usageservice;

import at.uastw.usageservice.entity.EnergyUser;
import at.uastw.usageservice.repository.EnergyUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UsageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }

    // This Bean runs automatically when the application starts
    @Bean
    public CommandLineRunner simulate1500Data(
            at.uastw.usageservice.repository.EnergyUserRepository userRepo,
            at.uastw.usageservice.repository.EnergyProducerRepository producerRepo,
            at.uastw.usageservice.repository.EnergyUsageLogRepository usageLogRepo,
            at.uastw.usageservice.repository.EnergyProductionLogRepository productionLogRepo,
            at.uastw.usageservice.service.UsageCalculationService calculationService,
            at.uastw.usageservice.repository.HourlyUsageRepository hourlyRepo) {

        return args -> {
            System.out.println("--- STARTING 15:00:00 SIMULATION ---");
            // Define our target hour
            java.time.LocalDateTime targetHour = java.time.LocalDateTime.of(2025, 1, 10, 15, 0);

            // 1. Generate Production Logs (5 producers * 3.21)
            producerRepo.findAll().forEach(producer -> {
                at.uastw.usageservice.entity.EnergyProductionLog pLog = new at.uastw.usageservice.entity.EnergyProductionLog();
                pLog.setProducer(producer);
                pLog.setProvidedEnergy(3.21);
                pLog.setRecordedAt(targetHour);
                productionLogRepo.save(pLog);
            });
            System.out.println("Saved 5 Production Logs.");

            // 2. Generate Usage Logs (10 users * 1.78)
            userRepo.findAll().forEach(user -> {
                at.uastw.usageservice.entity.EnergyUsageLog uLog = new at.uastw.usageservice.entity.EnergyUsageLog();
                uLog.setUser(user);
                uLog.setUsedEnergy(1.78);
                uLog.setRecordedAt(targetHour);
                usageLogRepo.save(uLog);
            });
            System.out.println("Saved 10 Usage Logs.");

            // 3. Trigger the Calculation Service!
            System.out.println("Triggering UsageCalculationService...");
            calculationService.calculateAndSaveHourlyUsage(targetHour);

            // --- THE FIX: Send the HTTP Request AFTER the transaction has committed! ---
            try {
                System.out.println("Sending update trigger to Current Percentage Service...");
                String targetUrl = "http://localhost:8093/api/percentages/calculate?time=" + targetHour.toString();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(targetUrl))
                        .POST(java.net.http.HttpRequest.BodyPublishers.noBody())
                        .build();
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                System.out.println("Trigger sent successfully!");
            } catch (Exception e) {
                System.err.println("Failed to contact Percentage Service: " + e.getMessage());
            }

            // 4. Verify it worked by fetching the new row back from the DB
            System.out.println("--- VERIFICATION ---");
            hourlyRepo.findById(targetHour).ifPresent(hourly -> {
                System.out.println("New Record Found in DB for: " + hourly.getRecordedHour());
                System.out.println("Community Produced: " + hourly.getCommunityProduced()); // Should be 16.05
                System.out.println("Community Used: "     + hourly.getCommunityUsed());     // Should be 16.05
                System.out.println("Grid Used: "          + hourly.getGridUsed());          // Should be 1.75
            });
            System.out.println("------------------------------------");
        };
    }
}