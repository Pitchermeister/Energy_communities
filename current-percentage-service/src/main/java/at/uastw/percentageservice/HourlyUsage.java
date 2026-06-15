package at.uastw.percentageservice;

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
    private Double communityProduced;

    @Column(name = "community_used")
    private Double communityUsed;

    @Column(name = "grid_used")
    private Double gridUsed;

    // --- Getters --- (Setters not needed, we only read this data here)
    public LocalDateTime getRecordedHour() { return recordedHour; }
    public Double getCommunityProduced() { return communityProduced; }
    public Double getCommunityUsed() { return communityUsed; }
    public Double getGridUsed() { return gridUsed; }
}