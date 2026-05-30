package at.uastw.energyproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class WeatherDataFetcher {

    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=48.2082&longitude=16.3738&hourly=cloudcover&timezone=UTC&start_date=%s&end_date=%s";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public WeatherDataFetcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public double getSunlightFactor() {
        try {
            LocalDate date = LocalDate.now(ZoneOffset.UTC);
            String isoDate = date.format(DateTimeFormatter.ISO_DATE);
            String url = String.format(WEATHER_URL, isoDate, isoDate);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode times = root.path("hourly").path("time");
            JsonNode clouds = root.path("hourly").path("cloudcover");
            String currentHour = LocalDateTime.now(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.HOURS)
                    .format(DateTimeFormatter.ISO_DATE_TIME);

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
