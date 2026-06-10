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

            // 1. Calculate and save (Upsert) the data for the current hour
            repository.calculateAndSavePercentages(targetHour);

            // 2. Delete any historical records older than the current hour
            repository.deleteOldRecords(targetHour);

            System.out.println("[PercentageService] Updated percentages & cleared history for: " + targetHour);

        } catch (Exception e) {
            System.err.println("[PercentageService] Error processing update: " + e.getMessage());
        }
    }
}