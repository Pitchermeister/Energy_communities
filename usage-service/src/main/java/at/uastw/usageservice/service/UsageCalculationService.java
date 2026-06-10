package at.uastw.usageservice.service;

import at.uastw.usageservice.entity.HourlyUsage;
import at.uastw.usageservice.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UsageCalculationService {

    private final HourlyUsageRepository hourlyRepo;

    public UsageCalculationService(HourlyUsageRepository hourlyRepo) {
        this.hourlyRepo = hourlyRepo;
    }

    @Transactional
    public void addIncomingDataToHour(LocalDateTime targetHour, String type, Double kwh) {

        // Retrieve the existing record for the specified hour, or initialize a new one
        HourlyUsage usage = hourlyRepo.findById(targetHour).orElseGet(() -> {
            HourlyUsage newUsage = new HourlyUsage();
            newUsage.setRecordedHour(targetHour);
            newUsage.setCommunityProduced(0.0);
            newUsage.setCommunityUsed(0.0);
            newUsage.setGridUsed(0.0);
            return newUsage;
        });

        // Current totals before applying the incoming delta
        double currentProduced = usage.getCommunityProduced();
        double currentUsedTotal = usage.getCommunityUsed() + usage.getGridUsed();

        // Apply incoming message payload
        if ("PRODUCER".equals(type)) {
            currentProduced += kwh;
        } else if ("USER".equals(type)) {
            currentUsedTotal += kwh;
        }

        // Recalculate energy distribution using the Net Metering approach
        double newCommunityUsed = Math.min(currentProduced, currentUsedTotal);
        double newGridUsed = Math.max(0, currentUsedTotal - currentProduced);

        // Update the entity, rounding to 5 decimal places to prevent floating-point precision issues
        usage.setCommunityProduced(Math.round(currentProduced * 100000.0) / 100000.0);
        usage.setCommunityUsed(Math.round(newCommunityUsed * 100000.0) / 100000.0);
        usage.setGridUsed(Math.round(newGridUsed * 100000.0) / 100000.0);

        // Persist the updated aggregate
        hourlyRepo.save(usage);

        System.out.println("[UsageService] Aggregation updated for " + targetHour +
                ": Produced=" + usage.getCommunityProduced() +
                ", CommunityUsed=" + usage.getCommunityUsed() +
                ", GridUsed=" + usage.getGridUsed());
    }
}