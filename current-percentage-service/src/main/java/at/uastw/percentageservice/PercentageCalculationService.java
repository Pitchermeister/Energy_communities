package at.uastw.percentageservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PercentageCalculationService {

    private final HourlyUsageRepository usageRepo;
    private final HourlyPercentageRepository percentageRepo;

    public PercentageCalculationService(HourlyUsageRepository usageRepo, HourlyPercentageRepository percentageRepo) {
        this.usageRepo = usageRepo;
        this.percentageRepo = percentageRepo;
    }

    @Transactional
    public void processPercentagesForHour(LocalDateTime targetHour) {
        // 1. Fetch raw data from the DB (populated by Usage Service)
        HourlyUsage usage = usageRepo.findById(targetHour).orElse(null);
        if (usage == null) {
            System.err.println("[PercentageService] No usage data found for hour: " + targetHour);
            return;
        }

        // 2. Perform the Business Logic in Java (Testable!)
        double percentages[] = calculatePercentages(
                usage.getCommunityProduced(),
                usage.getCommunityUsed(),
                usage.getGridUsed()
        );

        // 3. Upsert the result into the database
        HourlyPercentage percentageEntity = percentageRepo.findById(targetHour).orElseGet(HourlyPercentage::new);
        percentageEntity.setRecordedHour(targetHour);
        percentageEntity.setCommunityDepleted(percentages[0]);
        percentageEntity.setGridPortion(percentages[1]);

        percentageRepo.save(percentageEntity);

        // 4. Clean up historical data
        percentageRepo.deleteOldRecords(targetHour);
    }

    /**
     * Pure business logic extracted for Unit Testing.
     * Returns an array: [0] = communityDepleted, [1] = gridPortion
     */
    double[] calculatePercentages(double produced, double commUsed, double gridUsed) {
        double communityDepleted = (produced == 0.0) ? 0.0 : (commUsed / produced) * 100.0;

        double totalUsed = gridUsed + commUsed;
        double gridPortion = (totalUsed == 0.0) ? 0.0 : (gridUsed / totalUsed) * 100.0;

        // Round to 2 decimal places for cleaner data
        communityDepleted = Math.round(communityDepleted * 100.0) / 100.0;
        gridPortion = Math.round(gridPortion * 100.0) / 100.0;

        return new double[]{communityDepleted, gridPortion};
    }
}