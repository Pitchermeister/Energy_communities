package at.uastw.percentageservice;

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

    @Column(name = "community_depleted", nullable = false)
    private Double communityDepleted;

    @Column(name = "grid_portion", nullable = false)
    private Double gridPortion;

    // --- Getters and Setters ---
    public LocalDateTime getRecordedHour() { return recordedHour; }
    public void setRecordedHour(LocalDateTime recordedHour) { this.recordedHour = recordedHour; }
    public Double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(Double communityDepleted) { this.communityDepleted = communityDepleted; }
    public Double getGridPortion() { return gridPortion; }
    public void setGridPortion(Double gridPortion) { this.gridPortion = gridPortion; }
}