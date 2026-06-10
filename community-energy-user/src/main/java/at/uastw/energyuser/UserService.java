package at.uastw.energyuser;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements SchedulingConfigurer {

    private static final String ENERGY_QUEUE = "energy-queue";
    private static final long MIN_UPDATE_DELAY_MS = 1000;
    private static final long MAX_UPDATE_DELAY_MS = 5000;

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public UserService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::publish, triggerContext -> {
            Instant lastCompletion = triggerContext.lastCompletion();
            Instant nextStart = lastCompletion != null ? lastCompletion : Instant.now();
            return nextStart.plusMillis(randomUpdateDelay());
        });
    }

    public void publish() {
        double kwh = calculateKwh();
        String datetime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();

        Map<String, Object> message = new HashMap<>();
        message.put("type", "USER");
        message.put("association", "COMMUNITY");
        message.put("kwh", kwh);
        message.put("datetime", datetime);

        rabbitTemplate.convertAndSend(ENERGY_QUEUE, message);
        System.out.println("[User] sent=" + kwh + " kWh at " + datetime);
    }

    private double calculateKwh() {
        double timeFactor = getTimeFactor();
        double kwh = 0.01 * timeFactor;
        double variation = 0.002 * random.nextDouble();
        return Math.round((kwh+variation) * 100000.0) / 100000.0;
    }

    private double getTimeFactor() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 9) {
            return 1.5;
        }
        if (hour >= 17 && hour < 21) {
            return 1.7;
        }
        return 0.8 + random.nextDouble() * 0.4;
    }

    private long randomUpdateDelay() {
        return random.nextLong(MIN_UPDATE_DELAY_MS, MAX_UPDATE_DELAY_MS + 1);
    }
}
