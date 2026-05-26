package at.uastw.energyapi.repository;

import at.uastw.energyapi.entity.HourlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, LocalDateTime> {
}
