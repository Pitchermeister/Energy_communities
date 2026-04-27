package at.uastw.usageservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_usage")
public class HourlyUsage {

    @Id // The timestamp itself is our unique identifier!
    @Column(name = "recorded_hour")
    private LocalDateTime recordedHour;

    @Column(name = "community_produced", nullable = false)
    private Double communityProduced;

    @Column(name = "community_used", nullable = false)
    private Double communityUsed;

    @Column(name = "grid_used", nullable = false)
    private Double gridUsed;

    // --- Getters and Setters ---
    public LocalDateTime getRecordedHour() { return recordedHour; }
    public void setRecordedHour(LocalDateTime recordedHour) { this.recordedHour = recordedHour; }
    public Double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(Double communityProduced) { this.communityProduced = communityProduced; }
    public Double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(Double communityUsed) { this.communityUsed = communityUsed; }
    public Double getGridUsed() { return gridUsed; }
    public void setGridUsed(Double gridUsed) { this.gridUsed = gridUsed; }
}