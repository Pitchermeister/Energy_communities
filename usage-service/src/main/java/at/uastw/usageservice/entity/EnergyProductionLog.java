package at.uastw.usageservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_production_logs")
public class EnergyProductionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_id", nullable = false)
    private EnergyProducer producer;

    @Column(name = "provided_energy", nullable = false)
    private Double providedEnergy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EnergyProducer getProducer() { return producer; }
    public void setProducer(EnergyProducer producer) { this.producer = producer; }
    public Double getProvidedEnergy() { return providedEnergy; }
    public void setProvidedEnergy(Double providedEnergy) { this.providedEnergy = providedEnergy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}