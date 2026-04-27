package at.uastw.usageservice.repository;

import at.uastw.usageservice.entity.EnergyProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface EnergyProductionLogRepository extends JpaRepository<EnergyProductionLog, Integer> {

    @Query("SELECT SUM(e.providedEnergy) FROM EnergyProductionLog e WHERE e.recordedAt = :time")
    Double sumProductionByTime(@Param("time") LocalDateTime time);
}