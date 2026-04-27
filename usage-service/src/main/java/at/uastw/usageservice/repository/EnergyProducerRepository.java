package at.uastw.usageservice.repository;

import at.uastw.usageservice.entity.EnergyProducer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnergyProducerRepository extends JpaRepository<EnergyProducer, Integer> {
}