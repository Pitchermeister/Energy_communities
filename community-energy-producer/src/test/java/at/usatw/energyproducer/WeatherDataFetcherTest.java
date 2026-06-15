package at.uastw.energyproducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherDataFetcherTest {

    // Instantiate directly, no Spring Context needed for pure unit tests
    private final WeatherDataFetcher fetcher = new WeatherDataFetcher(new ObjectMapper());

    // Mock API JSON Response: Sunrise at 05:00, Sunset at 20:00
    private final String MOCK_JSON_RESPONSE = """
            {
              "daily": {
                "sunrise": ["2026-06-15T05:00"],
                "sunset": ["2026-06-15T20:00"]
              },
              "hourly": {
                "time": [
                  "2026-06-15T00:00",
                  "2026-06-15T06:00",
                  "2026-06-15T12:00",
                  "2026-06-15T18:00"
                ],
                "cloudcover": [
                  0.0,
                  50.0,
                  0.0,
                  100.0
                ]
              }
            }
            """;

    @Test
    void testSunlightFactor_AtMidnight_ShouldBeZero() throws Exception {
        // 00:00 - Night time (before 05:00 sunrise)
        LocalDateTime mockTime = LocalDateTime.parse("2026-06-15T00:00");
        double factor = fetcher.calculateFactorFromJson(MOCK_JSON_RESPONSE, mockTime);
        assertEquals(0.0, factor, "Night time should yield 0.0 factor.");
    }

    @Test
    void testSunlightFactor_AtMorning_With50PercentClouds() throws Exception {
        // 06:00 - Day time, 50% cloud cover
        LocalDateTime mockTime = LocalDateTime.parse("2026-06-15T06:00");
        double factor = fetcher.calculateFactorFromJson(MOCK_JSON_RESPONSE, mockTime);
        assertEquals(0.5, factor, "50% clouds should yield 0.5 factor.");
    }

    @Test
    void testSunlightFactor_AtNoon_WithClearSky() throws Exception {
        // 12:00 - Day time, 0% cloud cover
        LocalDateTime mockTime = LocalDateTime.parse("2026-06-15T12:00");
        double factor = fetcher.calculateFactorFromJson(MOCK_JSON_RESPONSE, mockTime);
        assertEquals(1.0, factor, "0% clouds should yield 1.0 factor.");
    }

    @Test
    void testSunlightFactor_AtEvening_With100PercentClouds() throws Exception {
        // 18:00 - Day time, 100% cloud cover
        LocalDateTime mockTime = LocalDateTime.parse("2026-06-15T18:00");
        double factor = fetcher.calculateFactorFromJson(MOCK_JSON_RESPONSE, mockTime);
        assertEquals(0.1, factor, "100% clouds during the day should yield baseline 0.1 factor.");
    }
}