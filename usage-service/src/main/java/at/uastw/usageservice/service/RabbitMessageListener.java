package at.uastw.usageservice.service;

import at.uastw.usageservice.entity.EnergyProductionLog;
import at.uastw.usageservice.entity.EnergyUsageLog;
import at.uastw.usageservice.repository.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class RabbitMessageListener {

    private final UsageCalculationService calculationService;
    private final EnergyUserRepository userRepo;
    private final EnergyProducerRepository producerRepo;
    private final EnergyProductionLogRepository productionLogRepo;
    private final EnergyUsageLogRepository usageLogRepo;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMessageListener(UsageCalculationService calculationService,
                                 EnergyUserRepository userRepo,
                                 EnergyProducerRepository producerRepo,
                                 EnergyProductionLogRepository productionLogRepo,
                                 EnergyUsageLogRepository usageLogRepo,
                                 RabbitTemplate rabbitTemplate) {
        this.calculationService = calculationService;
        this.userRepo = userRepo;
        this.producerRepo = producerRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.productionLogRepo = productionLogRepo;
        this.usageLogRepo = usageLogRepo;
        // Forcing JSON converter for outgoing messages
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    @RabbitListener(queues = "energy-queue")
    public void receiveEnergyData(Map<String, Object> message) {
        try {
            String type = (String) message.get("type");
            Integer id = (Integer) message.get("id");
            Double kwh = (Double) message.get("kwh");
            LocalDateTime datetime = LocalDateTime.parse((String) message.get("datetime"));

            // 1. Save incoming data to logs
            if ("PRODUCER".equals(type)) {
                producerRepo.findById(id).ifPresent(producer -> {
                    EnergyProductionLog log = new EnergyProductionLog();
                    log.setProducer(producer);
                    log.setProvidedEnergy(kwh);
                    log.setRecordedAt(datetime);
                    // This performs an SQL INSERT
                    productionLogRepo.save(log);
                });
            } else if ("USER".equals(type)) {
                userRepo.findById(id).ifPresent(user -> {
                    EnergyUsageLog log = new EnergyUsageLog();
                    log.setUser(user);
                    log.setUsedEnergy(kwh);
                    log.setRecordedAt(datetime);
                    // This performs an SQL INSERT
                    usageLogRepo.save(log);
                });
            }

            // 2. Truncate minutes to get the specific hour
            LocalDateTime targetHour = datetime.truncatedTo(java.time.temporal.ChronoUnit.HOURS);

            // 3. Call the separate Transactional Service! (No more self-invocation warning)
            calculationService.calculateAndSaveHourlyUsage(targetHour);

            // 4. Send the trigger to Current Percentage Service
            Map<String, Object> updateMsg = Map.of(
                    "targetHour", targetHour.toString(),
                    "status", "UPDATED"
            );
            rabbitTemplate.convertAndSend("percentage-update-queue", updateMsg);
            System.out.println("Trigger sent to Percentage Service for hour: " + targetHour);

        } catch (Exception e) {
            System.err.println("Error while processing message: " + e.getMessage());
        }
    }
}