package at.uastw.energyapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_usage")
public class HourlyUsage {
    @Id
    @Column(name = "recorded_hour")
    private LocalDateTime recordedHour;
    @Column(name = "community_produced")
    private double communityProduced;
    @Column(name = "community_used")
    private double communityUsed;
    @Column(name = "grid_used")
    private double gridUsed;

    // Getters
    public LocalDateTime getRecordedHour() { return recordedHour; }
    public double getCommunityProduced() { return communityProduced; }
    public double getCommunityUsed() { return communityUsed; }
    public double getGridUsed() { return gridUsed; }
}