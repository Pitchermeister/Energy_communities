package at.uastw.usageservice.service;

import at.uastw.usageservice.entity.HourlyUsage;
import at.uastw.usageservice.repository.EnergyProductionLogRepository;
import at.uastw.usageservice.repository.EnergyUsageLogRepository;
import at.uastw.usageservice.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UsageCalculationService {

    private final EnergyProductionLogRepository productionRepo;
    private final EnergyUsageLogRepository usageRepo;
    private final HourlyUsageRepository hourlyRepo;

    public UsageCalculationService(EnergyProductionLogRepository productionRepo,
                                   EnergyUsageLogRepository usageRepo,
                                   HourlyUsageRepository hourlyRepo) {
        this.productionRepo = productionRepo;
        this.usageRepo = usageRepo;
        this.hourlyRepo = hourlyRepo;
    }

    @Transactional
    public void calculateAndSaveHourlyUsage(LocalDateTime hourToCalculate) {
        LocalDateTime startOfHour = hourToCalculate;
        LocalDateTime endOfHour = hourToCalculate.plusHours(1);

        // Call the updated repository methods with the range
        Double totalProduced = productionRepo.sumProductionInHourRange(startOfHour, endOfHour);
        Double totalUsed = usageRepo.sumUsageInHourRange(startOfHour, endOfHour);

        // Handle null values if the database returns nothing
        if (totalProduced == null) totalProduced = 0.0;
        if (totalUsed == null) totalUsed = 0.0;

        // Calculate distribution
        double communityUsed = Math.min(totalProduced, totalUsed);
        double gridUsed = Math.max(0, totalUsed - totalProduced);

        // Save or update the hourly record
        HourlyUsage newHourlyUsage = new HourlyUsage();
        newHourlyUsage.setRecordedHour(hourToCalculate);
        newHourlyUsage.setCommunityProduced(totalProduced);
        newHourlyUsage.setCommunityUsed(communityUsed);
        newHourlyUsage.setGridUsed(gridUsed);

        hourlyRepo.save(newHourlyUsage);
        System.out.println("SUCCESS! Hourly Usage Updated: Produced=" + totalProduced +
                ", CommunityUsed=" + communityUsed + ", GridUsed=" + gridUsed);
    }
}