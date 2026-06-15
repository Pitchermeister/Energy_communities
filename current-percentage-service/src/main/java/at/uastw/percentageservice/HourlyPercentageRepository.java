package at.uastw.percentageservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface HourlyPercentageRepository extends JpaRepository<HourlyPercentage, LocalDateTime> {

    // The calculation logic has been moved to PercentageCalculationService!

    // Deletes all historical data older than the currently processed hour
    @Modifying
    @Transactional
    @Query("DELETE FROM HourlyPercentage h WHERE h.recordedHour < :time")
    void deleteOldRecords(@Param("time") LocalDateTime time);
}