package at.uastw.usageservice.service;

import at.uastw.usageservice.entity.HourlyUsage;
import at.uastw.usageservice.repository.EnergyProductionLogRepository;
import at.uastw.usageservice.repository.EnergyUsageLogRepository;
import at.uastw.usageservice.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service // Tells Spring this class holds business logic
public class UsageCalculationService {

    private final EnergyProductionLogRepository productionRepo;
    private final EnergyUsageLogRepository usageRepo;
    private final HourlyUsageRepository hourlyRepo;

    // Spring Boot automatically injects the repositories here
    public UsageCalculationService(EnergyProductionLogRepository productionRepo,
                                   EnergyUsageLogRepository usageRepo,
                                   HourlyUsageRepository hourlyRepo) {
        this.productionRepo = productionRepo;
        this.usageRepo = usageRepo;
        this.hourlyRepo = hourlyRepo;
    }

    @Transactional // Ensures the whole calculation saves perfectly, or rolls back if there's an error
    public void calculateAndSaveHourlyUsage(LocalDateTime hourToCalculate) {
        System.out.println("Calculating data for hour: " + hourToCalculate);

        // 1. Fetch the sums using Option B!
        Double totalProduced = productionRepo.sumProductionByTime(hourToCalculate);
        Double totalUsed = usageRepo.sumUsageByTime(hourToCalculate);

        // Handle nulls (just in case there are no logs for this hour)
        if (totalProduced == null) totalProduced = 0.0;
        if (totalUsed == null) totalUsed = 0.0;

        // 2. The Logic: Did we use more than we produced?
        double communityUsed = Math.min(totalProduced, totalUsed);
        double gridUsed = Math.max(0, totalUsed - totalProduced);

        // 3. Create the new Hourly record and save it!
        HourlyUsage newHourlyUsage = new HourlyUsage();
        newHourlyUsage.setRecordedHour(hourToCalculate);
        newHourlyUsage.setCommunityProduced(totalProduced);
        newHourlyUsage.setCommunityUsed(communityUsed);
        newHourlyUsage.setGridUsed(gridUsed);

        hourlyRepo.save(newHourlyUsage);
        System.out.println("SUCCESS! Saved Hourly Usage...");

        // --- NEW: Send HTTP Request to Current Percentage Service (Simulating RabbitMQ) ---


        System.out.println("SUCCESS! Saved Hourly Usage: Produced=" + totalProduced +
                ", CommunityUsed=" + communityUsed + ", GridUsed=" + gridUsed);
    }
}