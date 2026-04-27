package at.uastw.usageservice.entity;

import jakarta.persistence.*;

@Entity // Tells Spring Boot: "This class represents a database table"
@Table(name = "energy_producers") // The exact name of the table in Postgres
public class EnergyProducer {

    @Id // This is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tells Spring to use the SERIAL auto-increment
    private Integer id;

    @Column(name = "max_capacity", nullable = false)
    private Double maxCapacity;

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Double getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Double maxCapacity) { this.maxCapacity = maxCapacity; }
}