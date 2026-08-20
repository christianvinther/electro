package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.dto.OrderLineDTO;
import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ComponentRepository componentRepository;

    private Supplier supplier;
    private Component component;

    @BeforeEach
    void seed() {
        supplier = supplierRepository.save(new Supplier("Test-leverandør", "Vej 1"));
        component = componentRepository.save(new Component("Test-komponent", 5001, "ext", supplier));
    }

    @Test
    void createDraft_startsEmptyAndRejectsLines() {
        OrderDTO withLine = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of(new OrderLineDTO(null, component.getId(), null, 4)));
        assertThrows(IllegalArgumentException.class, () -> orderService.createDraft(withLine));

        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);

        assertNotNull(created.id());
        assertEquals("DRAFT", created.status());
        assertEquals(0, created.lines().size());
    }

    @Test
    void addLine_toDraftAddsTheLine() {
        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);

        OrderLineDTO line = new OrderLineDTO(null, component.getId(), null, 10);
        OrderDTO updated = orderService.addLine(created.id(), line);

        assertEquals(1, updated.lines().size());
        assertEquals(10, updated.lines().getFirst().quantity());
    }

    @Test
    void sentOrder_canBeMarkedReceived() {
        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);
        orderService.addLine(created.id(), new OrderLineDTO(null, component.getId(), null, 1));

        OrderDTO sent = orderService.markSent(created.id());
        OrderDTO received = orderService.markReceived(created.id());

        assertEquals("SENT", sent.status());
        assertNotNull(sent.sentDate());
        assertEquals("RECEIVED", received.status());
        assertNotNull(received.receivedDate());
    }

    @Test
    void addLine_afterOrderIsSentIsBlocked() {
        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);
        orderService.addLine(created.id(), new OrderLineDTO(null, component.getId(), null, 1));
        orderService.markSent(created.id());

        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(),
                new OrderLineDTO(null, component.getId(), null, 1)));
    }

    @Test
    void sendEmptyOrder_isBlocked() {
        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);

        assertThrows(IllegalStateException.class,
            () -> orderService.markSent(created.id()));
    }

    @Test
    void addLine_withNonOrderableComponentsIsBlocked() {
        Component discontinued = new Component("Udgået", 5002, "ext2", supplier);
        discontinued.setDiscontinued(true);
        discontinued = componentRepository.save(discontinued);
        Component assembledOnly = componentRepository.save(new Component("Samlesæt", 5003, null, null));
        OrderDTO input = new OrderDTO(null, supplier.getId(), null, null, null,
            null, null, null, List.of());
        OrderDTO created = orderService.createDraft(input);

        Long discontinuedId = discontinued.getId();
        Long assembledOnlyId = assembledOnly.getId();
        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(),
                new OrderLineDTO(null, discontinuedId, null, 1)));
        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(),
                new OrderLineDTO(null, assembledOnlyId, null, 1)));
    }
}
