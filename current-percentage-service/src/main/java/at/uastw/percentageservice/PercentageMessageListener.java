package at.uastw.percentageservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PercentageMessageListener {

    private final PercentageCalculationService calculationService;

    public PercentageMessageListener(PercentageCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @RabbitListener(queues = "percentage-update-queue")
    public void receiveUpdateTrigger(Map<String, Object> message) {
        try {
            String targetHourStr = (String) message.get("targetHour");
            LocalDateTime targetHour = LocalDateTime.parse(targetHourStr);

            // Delegate to the business logic service
            calculationService.processPercentagesForHour(targetHour);

            System.out.println("[PercentageService] Updated percentages & cleared history for: " + targetHour);

        } catch (Exception e) {
            System.err.println("[PercentageService] Error processing update: " + e.getMessage());
        }
    }
}