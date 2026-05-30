package at.uastw.energyproducer;

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
public class ProducerService implements SchedulingConfigurer {

    private static final String ENERGY_QUEUE = "energy-queue";
    private static final long MIN_UPDATE_DELAY_MS = 1000;
    private static final long MAX_UPDATE_DELAY_MS = 5000;

    private final RabbitTemplate rabbitTemplate;
    private final WeatherDataFetcher weatherDataFetcher;
    private final Random random = new Random();

    public ProducerService(RabbitTemplate rabbitTemplate, WeatherDataFetcher weatherDataFetcher) {
        this.rabbitTemplate = rabbitTemplate;
        this.weatherDataFetcher = weatherDataFetcher;
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
        int producerId = random.nextInt(5) + 1;

        Map<String, Object> message = new HashMap<>();
        message.put("type", "PRODUCER");
        message.put("association", "COMMUNITY");
        message.put("id", producerId);
        message.put("kwh", kwh);
        message.put("datetime", datetime);

        rabbitTemplate.convertAndSend(ENERGY_QUEUE, message);
        System.out.println("[Producer " + producerId + "] sent=" + kwh + " kWh at " + datetime);
    }

    private double calculateKwh() {
        double sunlightFactor = weatherDataFetcher.getSunlightFactor();
        double base = 0.003 * sunlightFactor;
        double variation = 0.0005 * random.nextDouble();
        return Math.round((base + variation) * 100000.0) / 100000.0;
    }

    private long randomUpdateDelay() {
        return random.nextLong(MIN_UPDATE_DELAY_MS, MAX_UPDATE_DELAY_MS + 1);
    }
}
