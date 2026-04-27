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

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO hourly_percentages (recorded_hour, community_depleted, grid_portion) " +
            "SELECT recorded_hour, (community_used / community_produced) * 100, (grid_used / (grid_used + community_used)) * 100 " +
            "FROM hourly_usage WHERE recorded_hour = :time", nativeQuery = true)
    void calculateAndSavePercentages(@Param("time") LocalDateTime time);
}