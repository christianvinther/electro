package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static dk.ek.chri585u.electro.service.TestDataFactory.draftOrder;
import static dk.ek.chri585u.electro.service.TestDataFactory.line;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest extends AbstractServiceTest {

    @Autowired private OrderService orderService;

    private Supplier supplier;
    private Component component;

    @BeforeEach
    void seed() {
        supplier = supplierRepository.save(new Supplier("Test-leverandør", "Vej 1"));
        component = componentRepository.save(new Component("Test-komponent", 5001, "ext", supplier));
    }

    @Test
    void createDraft_startsWithDraftStatus() {
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));

        assertNotNull(created.id());
        assertEquals("DRAFT", created.status());
        assertEquals(0, created.lines().size());
    }

    @Test
    void addLine_toDraftAddsTheLine() {
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));

        OrderDTO updated = orderService.addLine(created.id(), line(component.getId(), 10));

        assertEquals(1, updated.lines().size());
        assertEquals(10, updated.lines().getFirst().quantity());
    }

    @Test
    void sentOrder_canBeMarkedReceived() {
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));
        orderService.addLine(created.id(), line(component.getId(), 1));

        OrderDTO sent = orderService.markSent(created.id());
        OrderDTO received = orderService.markReceived(created.id());

        assertEquals("SENT", sent.status());
        assertNotNull(sent.sentDate());
        assertEquals("RECEIVED", received.status());
        assertNotNull(received.receivedDate());
    }

    @Test
    void addLine_afterOrderIsSentIsBlocked() {
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));
        orderService.addLine(created.id(), line(component.getId(), 1));
        orderService.markSent(created.id());

        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(), line(component.getId(), 1)));
    }

    @Test
    void sendEmptyOrder_isBlocked() {
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));

        assertThrows(IllegalStateException.class,
            () -> orderService.markSent(created.id()));
    }

    @Test
    void addLine_withNonOrderableComponentsIsBlocked() {
        Component discontinued = new Component("Udgået", 5002, "ext2", supplier);
        discontinued.setDiscontinued(true);
        discontinued = componentRepository.save(discontinued);
        Component assembledOnly = componentRepository.save(new Component("Samlesæt", 5003, null, null));
        OrderDTO created = orderService.createDraft(draftOrder(supplier.getId()));

        Long discontinuedId = discontinued.getId();
        Long assembledOnlyId = assembledOnly.getId();
        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(), line(discontinuedId, 1)));
        assertThrows(IllegalStateException.class,
            () -> orderService.addLine(created.id(), line(assembledOnlyId, 1)));
    }
}
