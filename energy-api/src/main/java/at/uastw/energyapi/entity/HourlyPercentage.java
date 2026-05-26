package at.uastw.energyapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_percentages")
public class HourlyPercentage {
    @Id
    @Column(name = "recorded_hour")
    private LocalDateTime recordedHour;
    @Column(name = "community_depleted")
    private double communityDepleted;
    @Column(name = "grid_portion")
    private double gridPortion;

    // Getters
    public LocalDateTime getRecordedHour() { return recordedHour; }
    public double getCommunityDepleted() { return communityDepleted; }
    public double getGridPortion() { return gridPortion; }
}