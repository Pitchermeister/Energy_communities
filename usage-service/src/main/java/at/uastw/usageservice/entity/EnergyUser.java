package at.uastw.usageservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "energy_users")
public class EnergyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String firstname;

    @Column(nullable = false, length = 50)
    private String lastname;

    @Column(length = 255)
    private String address;

    // This is how we map your Foreign Key!
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producer_id")
    private EnergyProducer producer;

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public EnergyProducer getProducer() { return producer; }
    public void setProducer(EnergyProducer producer) { this.producer = producer; }
}