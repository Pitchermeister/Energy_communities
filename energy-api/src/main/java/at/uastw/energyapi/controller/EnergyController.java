package at.uastw.energyapi.controller;

import at.uastw.energyapi.dto.EnergyDto;
import at.uastw.energyapi.entity.HourlyPercentage;
import at.uastw.energyapi.entity.HourlyUsage;
import at.uastw.energyapi.repository.HourlyPercentageRepository;
import at.uastw.energyapi.repository.HourlyUsageRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/energy")
// PETER'S DING!! VON DER ENDABGABE LÖSCHEN!
@CrossOrigin(origins = "http://localhost:4200")
// !!!
public class EnergyController {

    private final HourlyUsageRepository usageRepo;
    private final HourlyPercentageRepository percentageRepo;

    // Spring automatically injects the repositories here
    public EnergyController(HourlyUsageRepository usageRepo, HourlyPercentageRepository percentageRepo) {
        this.usageRepo = usageRepo;
        this.percentageRepo = percentageRepo;
    }

    @GetMapping("/current")
    public EnergyDto getCurrent() {
        // Fetch all hourly usages ordered, or just find the latest one.
        // For simplicity, let's take the first one available or fallback to a dummy if empty.
        List<HourlyUsage> usages = usageRepo.findAll();
        if (usages.isEmpty()) {
            return new EnergyDto(LocalDateTime.now().toString(), 0, 0, 0, 0, 0);
        }

        HourlyUsage latestUsage = usages.getLast();
        Optional<HourlyPercentage> percentageOpt = percentageRepo.findById(latestUsage.getRecordedHour());

        double depleted = percentageOpt.map(HourlyPercentage::getCommunityDepleted).orElse(0.0);
        double gridPortion = percentageOpt.map(HourlyPercentage::getGridPortion).orElse(0.0);

        return new EnergyDto(
                latestUsage.getRecordedHour().toString(),
                latestUsage.getCommunityProduced(),
                latestUsage.getCommunityUsed(),
                latestUsage.getGridUsed(),
                depleted,
                gridPortion
        );
    }

    @GetMapping("/historical")
    public List<EnergyDto> getHistorical(@RequestParam String start, @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        List<EnergyDto> resultList = new ArrayList<>();
        List<HourlyUsage> usages = usageRepo.findByRecordedHourBetween(startTime, endTime);

        // Loop through all usage records and map them with their corresponding percentages
        for (HourlyUsage usage : usages) {
            Optional<HourlyPercentage> percentageOpt = percentageRepo.findById(usage.getRecordedHour());

            double depleted = percentageOpt.map(HourlyPercentage::getCommunityDepleted).orElse(0.0);
            double gridPortion = percentageOpt.map(HourlyPercentage::getGridPortion).orElse(0.0);

            resultList.add(new EnergyDto(
                    usage.getRecordedHour().toString(),
                    usage.getCommunityProduced(),
                    usage.getCommunityUsed(),
                    usage.getGridUsed(),
                    depleted,
                    gridPortion
            ));
        }
        return resultList;
    }
}