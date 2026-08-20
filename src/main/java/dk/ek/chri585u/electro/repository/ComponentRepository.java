package dk.ek.chri585u.electro.repository;

import dk.ek.chri585u.electro.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<Component, Long> {
    boolean existsByInternalNumber(Integer internalNumber);
}
