package at.uastw.percentageservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentageCalculationServiceTest {

    // No need for Repositories to test the pure math logic!
    private final PercentageCalculationService service = new PercentageCalculationService(null, null);

    @Test
    void testCalculatePercentages_StandardScenario() {
        // Produced: 10, CommunityUsed: 5, GridUsed: 15 (Total used = 20)
        double[] result = service.calculatePercentages(10.0, 5.0, 15.0);

        assertEquals(50.0, result[0], "Community should be 50% depleted (5 out of 10)");
        assertEquals(75.0, result[1], "Grid should provide 75% of total used (15 out of 20)");
    }

    @Test
    void testCalculatePercentages_ZeroDivisionProtection() {
        // Zero production and Zero usage (e.g. at night with no one home)
        double[] result = service.calculatePercentages(0.0, 0.0, 0.0);

        assertEquals(0.0, result[0], "Depleted should be 0.0 to prevent NaN");
        assertEquals(0.0, result[1], "Grid portion should be 0.0 to prevent NaN");
    }
}