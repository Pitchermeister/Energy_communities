package at.uastw.energyapi.repository;

import at.uastw.energyapi.entity.HourlyPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface HourlyPercentageRepository extends JpaRepository<HourlyPercentage, LocalDateTime> {
}
