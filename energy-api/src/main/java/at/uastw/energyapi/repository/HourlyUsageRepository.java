package at.uastw.energyapi.repository;

import at.uastw.energyapi.entity.HourlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, LocalDateTime> {
    List<HourlyUsage> findByRecordedHourBetween(LocalDateTime start, LocalDateTime end);
}
