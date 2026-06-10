package at.uastw.usageservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class RabbitMessageListener {

    private final UsageCalculationService calculationService;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMessageListener(UsageCalculationService calculationService,
                                 RabbitTemplate rabbitTemplate) {
        this.calculationService = calculationService;
        this.rabbitTemplate = rabbitTemplate;
        // Enforce JSON serialization for outgoing AMQP messages
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @RabbitListener(queues = "energy-queue")
    public void receiveEnergyData(Map<String, Object> message) {
        try {
            String type = (String) message.get("type");
            Double kwh = (Double) message.get("kwh");
            LocalDateTime datetime = LocalDateTime.parse((String) message.get("datetime"));

            // Truncate timestamp to the start of the hour for proper aggregation
            LocalDateTime targetHour = datetime.truncatedTo(java.time.temporal.ChronoUnit.HOURS);

            // Delegate data processing to the transactional service layer
            calculationService.addIncomingDataToHour(targetHour, type, kwh);

            // Notify Percentage Service via message broker to update relative metrics
            Map<String, Object> updateMsg = Map.of(
                    "targetHour", targetHour.toString(),
                    "status", "UPDATED"
            );
            rabbitTemplate.convertAndSend("percentage-update-queue", updateMsg);

        } catch (Exception e) {
            System.err.println("[UsageService] Message processing failed: " + e.getMessage());
        }
    }
}