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
            + "?latitude=48.2082&longitude=16.3738&hourly=cloudcover&daily=sunrise,sunset&timezone=UTC&start_date=%s&end_date=%s";

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

            // Pass the raw JSON and the exact current time to our logic method
            return calculateFactorFromJson(response.body(), LocalDateTime.now(ZoneOffset.UTC));

        } catch (Exception ignored) {
            System.err.println("[WeatherDataFetcher] API error, using fallback logic.");
        }

        // Failsafe fallback
        int localHour = LocalDateTime.now().getHour();
        return (localHour >= 6 && localHour <= 20) ? 0.7 + random.nextDouble() * 0.3 : 0.0;
    }

    /**
     * Package-private explicitly for Unit Testing!
     * This method contains the pure business logic without any HTTP dependencies.
     */
    double calculateFactorFromJson(String json, LocalDateTime now) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        JsonNode daily = root.path("daily");
        LocalDateTime sunrise = LocalDateTime.parse(daily.path("sunrise").get(0).asText());
        LocalDateTime sunset = LocalDateTime.parse(daily.path("sunset").get(0).asText());

        // 1. Check if it's night time
        if (now.isBefore(sunrise) || now.isAfter(sunset)) {
            return 0.0;
        }

        // 2. Check cloud cover for the exact current hour
        JsonNode times = root.path("hourly").path("time");
        JsonNode clouds = root.path("hourly").path("cloudcover");

        // Use Java Date objects for safe comparison instead of Strings
        LocalDateTime targetHour = now.truncatedTo(ChronoUnit.HOURS);

        for (int i = 0; i < times.size(); i++) {
            LocalDateTime apiTime = LocalDateTime.parse(times.get(i).asText());

            if (targetHour.equals(apiTime)) {
                double cloudCover = clouds.get(i).asDouble();
                return Math.max(0.1, (100.0 - cloudCover) / 100.0);
            }
        }
        return 0.0;
    }
}