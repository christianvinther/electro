package dk.ek.chri585u.electro.repository;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.model.StockCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StockCountRepositoryTest {

    @Autowired private StockCountRepository stockCountRepository;
    @Autowired private ComponentRepository componentRepository;
    @Autowired private SupplierRepository supplierRepository;

    @Test
    void findTopByComponentIdOrderByCountedAtDesc_returnsLatest() {
        Supplier s = supplierRepository.save(new Supplier("Lev", "Vej 1"));
        Component c = componentRepository.save(new Component("LED", 9001, "ext", s));

        LocalDateTime t1 = LocalDateTime.now().minusHours(2);
        LocalDateTime t2 = LocalDateTime.now().minusHours(1);
        LocalDateTime t3 = LocalDateTime.now();
        stockCountRepository.save(new StockCount(c, 10, "Anne", t1));
        stockCountRepository.save(new StockCount(c, 15, "Bo", t2));
        stockCountRepository.save(new StockCount(c, 12, "Christian", t3));

        Optional<StockCount> latest = stockCountRepository.findTopByComponentIdOrderByCountedAtDesc(c.getId());

        assertTrue(latest.isPresent());
        assertEquals(12, latest.get().getActualQuantity());
        assertEquals("Christian", latest.get().getCountedBy());
    }
}
