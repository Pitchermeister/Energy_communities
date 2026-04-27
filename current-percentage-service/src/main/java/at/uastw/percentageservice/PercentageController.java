package at.uastw.percentageservice;

import at.uastw.percentageservice.HourlyPercentageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/percentages")
public class PercentageController {
    private final HourlyPercentageRepository repository;

    public PercentageController(HourlyPercentageRepository repository) {
        this.repository = repository;
    }

    // This endpoint acts as our "Message Queue Receiver"
    @PostMapping("/calculate")
    public String triggerCalculation(@RequestParam String time) {
        System.out.println("Received trigger to calculate percentages for: " + time);

        // Convert the string back to a LocalDateTime
        LocalDateTime targetTime = LocalDateTime.parse(time);

        // Run your native SQL Query
        repository.calculateAndSavePercentages(targetTime);

        return "Success";
    }
}
