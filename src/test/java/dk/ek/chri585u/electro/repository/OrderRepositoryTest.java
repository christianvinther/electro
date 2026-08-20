package dk.ek.chri585u.electro.repository;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;
import dk.ek.chri585u.electro.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ComponentRepository componentRepository;

    @Test
    void savedOrderRetainsAllLines() {
        Supplier s = supplierRepository.save(new Supplier("S", "V"));
        Component c = componentRepository.save(new Component("C", 100, "ext", s));
        Order o = new Order(s);
        o.addLine(new OrderLine(5, c));
        o.addLine(new OrderLine(7, c));
        o.addLine(new OrderLine(9, c));

        Order saved = orderRepository.save(o);
        Order reloaded = orderRepository.findById(saved.getId()).orElseThrow();

        assertEquals(3, reloaded.getLines().size());
    }

    @Test
    void findByStatusIn_returnsOnlyOpenOrders() {
        Supplier s = supplierRepository.save(new Supplier("S", "V"));
        Order draft = new Order(s); draft.setStatus(OrderStatus.DRAFT);
        Order sent  = new Order(s); sent.setStatus(OrderStatus.SENT);
        Order recv  = new Order(s); recv.setStatus(OrderStatus.RECEIVED);
        orderRepository.save(draft);
        orderRepository.save(sent);
        orderRepository.save(recv);

        List<Order> open = orderRepository.findByStatusIn(List.of(OrderStatus.DRAFT, OrderStatus.SENT));

        assertEquals(2, open.size());
        assertTrue(open.stream().noneMatch(o -> o.getStatus() == OrderStatus.RECEIVED));
    }
}
