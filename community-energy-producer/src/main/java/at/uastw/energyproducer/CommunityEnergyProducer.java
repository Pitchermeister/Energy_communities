package at.uastw.energyproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
public class CommunityEnergyProducer {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommunityEnergyProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommunityEnergyProducer.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Bean
    public Queue energyQueue() {
        return new Queue("energy-queue", true);
    }

    @Scheduled(fixedRate = 5000, initialDelay = 2000)
    public void publish() {
        double kwh = calculateKwh();
        String datetime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();

        Map<String, Object> message = new HashMap<>();
        message.put("type", "PRODUCER");
        message.put("association", "COMMUNITY");
        message.put("kwh", kwh);
        message.put("datetime", datetime);

        rabbitTemplate.convertAndSend("energy-queue", message);
        System.out.println("[Producer] sent=" + kwh + " kWh at " + datetime);
    }

    private double calculateKwh() {
        double sunlightFactor = getSunlightFactor();
        double base = 0.003 * sunlightFactor;
        double variation = 0.0005 * random.nextDouble();
        return Math.round((base + variation) * 100000.0) / 100000.0;
    }

    private double getSunlightFactor() {
        try {
            LocalDate date = LocalDate.now(ZoneOffset.UTC);
            String isoDate = date.format(DateTimeFormatter.ISO_DATE);
            String url = "https://api.open-meteo.com/v1/forecast?latitude=48.2082&longitude=16.3738&hourly=cloudcover&timezone=UTC&start_date=" + isoDate + "&end_date=" + isoDate;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode times = root.path("hourly").path("time");
            JsonNode clouds = root.path("hourly").path("cloudcover");
            String currentHour = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS).format(DateTimeFormatter.ISO_DATE_TIME);
            for (int i = 0; i < times.size(); i++) {
                if (currentHour.equals(times.get(i).asText())) {
                    double cloudCover = clouds.get(i).asDouble();
                    return Math.max(0.2, (100.0 - cloudCover) / 100.0);
                }
            }
        } catch (Exception ignored) {
        }
        int hour = LocalDateTime.now().getHour();
        return (hour >= 7 && hour <= 18) ? 0.7 + random.nextDouble() * 0.3 : 0.2 + random.nextDouble() * 0.2;
    }
}
