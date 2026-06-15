package at.uastw.percentageservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, LocalDateTime> {
}