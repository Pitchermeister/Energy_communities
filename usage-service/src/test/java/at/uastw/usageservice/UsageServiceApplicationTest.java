package at.uastw.usageservice;

import at.uastw.usageservice.entity.*;
import at.uastw.usageservice.repository.*;
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
@Transactional // Magic trick: Rolls back all database changes after the test finishes!
class UsageCalculationIntegrationTest {

    @Autowired
    private UsageCalculationService calculationService;

    @Autowired
    private EnergyProducerRepository producerRepo;

    @Autowired
    private EnergyUserRepository userRepo;

    @Autowired
    private EnergyProductionLogRepository prodLogRepo;

    @Autowired
    private EnergyUsageLogRepository usageLogRepo;

    @Autowired
    private HourlyUsageRepository hourlyRepo;

    // Disables the real RabbitMQ connection during the test (Mocking)
    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void testHourlyCalculation_With4ProducedAnd5Used() {
        // 1. ARRANGE: Set up the test scenario
        LocalDateTime testHour = LocalDateTime.of(2026, 5, 25, 20, 0);

        // A. Create a Dummy Producer
        EnergyProducer dummyProducer = new EnergyProducer();
        dummyProducer.setMaxCapacity(10.0);
        producerRepo.save(dummyProducer);

        // B. Create a Dummy User
        EnergyUser dummyUser = new EnergyUser();
        dummyUser.setFirstname("Test");
        dummyUser.setLastname("User");
        userRepo.save(dummyUser);

        // C. Simulate 4 kWh production (e.g., signal received at 20:10)
        EnergyProductionLog pLog = new EnergyProductionLog();
        pLog.setProducer(dummyProducer);
        pLog.setProvidedEnergy(4.0);
        pLog.setRecordedAt(testHour.plusMinutes(10));
        prodLogRepo.save(pLog);

        // D. Simulate 5 kWh usage (e.g., signal received at 20:15)
        EnergyUsageLog uLog = new EnergyUsageLog();
        uLog.setUser(dummyUser);
        uLog.setUsedEnergy(5.0);
        uLog.setRecordedAt(testHour.plusMinutes(15));
        usageLogRepo.save(uLog);

        // 2. ACT: Trigger the target calculation service
        calculationService.calculateAndSaveHourlyUsage(testHour);

        // 3. ASSERT: Fetch the result from DB and verify the math
        HourlyUsage result = hourlyRepo.findById(testHour).orElseThrow();

        assertEquals(4.0, result.getCommunityProduced(), "Error: Total production should be 4.0!");
        assertEquals(4.0, result.getCommunityUsed(), "Error: Community usage should be 4.0!");
        assertEquals(1.0, result.getGridUsed(), "Error: Grid usage should be 1.0!");

        System.out.println("✅ INTEGRATION TEST PASSED: The 4/5 kWh calculation logic executed flawlessly!");
    }
}