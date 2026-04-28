package at.uastw.energyuser;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
public class CommunityEnergyUser {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public CommunityEnergyUser(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommunityEnergyUser.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Bean
    public Queue energyQueue() {
        return new Queue("energy-queue", true);
    }

    @Scheduled(fixedRate = 5000, initialDelay = 3000)
    public void publish() {
        double kwh = calculateKwh();
        String datetime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();

        Map<String, Object> message = new HashMap<>();
        message.put("type", "USER");
        message.put("association", "COMMUNITY");
        message.put("kwh", kwh);
        message.put("datetime", datetime);

        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("[User] sent=" + kwh + " kWh at " + datetime);
    }

    private double calculateKwh() {
        double timeFactor = getTimeFactor();
        double kwh = 0.001 * timeFactor;
        return Math.round(kwh * 100000.0) / 100000.0;
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
}
