package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.common.NotFoundException;
import dk.ek.chri585u.electro.dto.InventoryRowDTO;
import dk.ek.chri585u.electro.dto.StockCountDTO;
import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;
import dk.ek.chri585u.electro.model.OrderStatus;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.OrderRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class InventoryServiceTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ComponentRepository componentRepository;
    @Autowired private OrderRepository orderRepository;

    private Component received;
    private Component countedOnly;

    @BeforeEach
    void seed() {
        Supplier supplier = supplierRepository.save(new Supplier("Lev", "Vej 1"));
        received = componentRepository.save(new Component("Modtaget", 8001, "ext", supplier));
        countedOnly = componentRepository.save(new Component("Kun talt", 8002, "ext", supplier));

        Order order = new Order(supplier);
        order.setStatus(OrderStatus.RECEIVED);
        order.addLine(new OrderLine(50, received));
        orderRepository.save(order);
    }

    @Test
    void listInventory_sumsReceivedOrderLines() {
        InventoryRowDTO row = rowFor(received);

        assertEquals(50, row.totalReceived());
    }

    @Test
    void listInventory_usesLatestPhysicalCount() {
        inventoryService.recordCount(received.getId(), count(45));
        inventoryService.recordCount(received.getId(), count(47));

        InventoryRowDTO row = rowFor(received);

        assertEquals(47, row.lastCountedQuantity());
    }

    @Test
    void listInventory_includesCountedOnlyComponentWithZeroReceived() {
        inventoryService.recordCount(countedOnly.getId(), count(12));

        InventoryRowDTO row = rowFor(countedOnly);

        assertEquals(0, row.totalReceived());
        assertEquals(12, row.lastCountedQuantity());
    }

    @Test
    void recordCount_unknownComponentIsRejected() {
        assertThrows(NotFoundException.class,
            () -> inventoryService.recordCount(99999L, count(1)));
    }

    private StockCountDTO count(int quantity) {
        return new StockCountDTO(null, null, null, quantity, "Christian", null);
    }

    private InventoryRowDTO rowFor(Component component) {
        return inventoryService.listInventory().stream()
            .filter(row -> row.componentId().equals(component.getId()))
            .findFirst()
            .orElseThrow();
    }
}
