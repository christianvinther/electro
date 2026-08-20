package dk.ek.chri585u.electro.repository;

import dk.ek.chri585u.electro.model.StockCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockCountRepository extends JpaRepository<StockCount, Long> {
    Optional<StockCount> findTopByComponentIdOrderByCountedAtDesc(Long componentId);
}
