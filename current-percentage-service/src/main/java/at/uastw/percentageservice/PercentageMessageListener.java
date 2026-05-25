package at.uastw.percentageservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PercentageMessageListener {

    private final HourlyPercentageRepository repository;

    public PercentageMessageListener(HourlyPercentageRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "percentage-update-queue")
    public void receiveUpdateTrigger(Map<String, Object> message) {
        try {
            String targetHourStr = (String) message.get("targetHour");
            LocalDateTime targetHour = LocalDateTime.parse(targetHourStr);

            System.out.println("Trigger received! Calculating percentages for: " + targetHour);

            repository.calculateAndSavePercentages(targetHour);

            System.out.println("SUCCESS: Percentages updated in database!");

        } catch (Exception e) {
            System.err.println("Error processing percentage update: " + e.getMessage());
        }
    }
}