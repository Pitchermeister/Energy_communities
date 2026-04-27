package at.uastw.usageservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_usage_logs")
public class EnergyUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private EnergyUser user;

    @Column(name = "used_energy", nullable = false)
    private Double usedEnergy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EnergyUser getUser() { return user; }
    public void setUser(EnergyUser user) { this.user = user; }
    public Double getUsedEnergy() { return usedEnergy; }
    public void setUsedEnergy(Double usedEnergy) { this.usedEnergy = usedEnergy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}