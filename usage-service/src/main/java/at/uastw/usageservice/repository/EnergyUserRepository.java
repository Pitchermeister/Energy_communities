package at.uastw.usageservice.repository;

import at.uastw.usageservice.entity.EnergyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Tells Spring this is a database access component
public interface EnergyUserRepository extends JpaRepository<EnergyUser, Integer> {
}