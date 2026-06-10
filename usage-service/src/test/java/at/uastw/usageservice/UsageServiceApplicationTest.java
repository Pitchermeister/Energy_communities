package at.uastw.usageservice;

import at.uastw.usageservice.entity.HourlyUsage;
import at.uastw.usageservice.repository.HourlyUsageRepository;
import at.uastw.usageservice.service.UsageCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional // Ensures database rollbacks after test execution
class UsageCalculationIntegrationTest {

    @Autowired
    private UsageCalculationService calculationService;

    @Autowired
    private HourlyUsageRepository hourlyRepo;

    // Disables the real RabbitMQ connection during the test to prevent network dependencies
    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testHourlyAggregation_With4ProducedAnd5Used() {
        // 1. ARRANGE: Define the target aggregation hour
        LocalDateTime testHour = LocalDateTime.of(2026, 5, 25, 20, 0);

        // 2. ACT: Simulate incoming RabbitMQ data streams sequentially
        // Signal 1: Producer generates 4.0 kWh
        calculationService.addIncomingDataToHour(testHour, "PRODUCER", 4.0);

        // Signal 2: User consumes 5.0 kWh
        calculationService.addIncomingDataToHour(testHour, "USER", 5.0);

        // 3. ASSERT: Fetch the aggregated result from DB and verify the logic
        HourlyUsage result = hourlyRepo.findById(testHour).orElseThrow();

        assertEquals(4.0, result.getCommunityProduced(), "Error: Total production should be 4.0!");
        assertEquals(4.0, result.getCommunityUsed(), "Error: Community usage should be 4.0!");
        assertEquals(1.0, result.getGridUsed(), "Error: Grid usage should be 1.0!");

        System.out.println("✅ INTEGRATION TEST PASSED: Stream aggregation and Net Metering logic executed flawlessly!");
    }
}