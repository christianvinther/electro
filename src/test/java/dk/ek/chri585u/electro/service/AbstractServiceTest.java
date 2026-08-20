package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.repository.AssemblyRepository;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.OrderRepository;
import dk.ek.chri585u.electro.repository.StockCountRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class AbstractServiceTest {

    @Autowired protected SupplierRepository supplierRepository;
    @Autowired protected ComponentRepository componentRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected AssemblyRepository assemblyRepository;
    @Autowired protected StockCountRepository stockCountRepository;

    @BeforeEach
    void wipeDatabase() {
        stockCountRepository.deleteAll();
        orderRepository.deleteAll();
        assemblyRepository.deleteAll();
        componentRepository.deleteAll();
        supplierRepository.deleteAll();
    }
}
