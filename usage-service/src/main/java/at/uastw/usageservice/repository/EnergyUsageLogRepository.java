package at.uastw.usageservice.repository;

import at.uastw.usageservice.entity.EnergyUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface EnergyUsageLogRepository extends JpaRepository<EnergyUsageLog, Integer> {

    // OPTION B: Let the database do the math!
    @Query("SELECT SUM(e.usedEnergy) FROM EnergyUsageLog e WHERE e.recordedAt = :time")
    Double sumUsageByTime(@Param("time") LocalDateTime time);
}