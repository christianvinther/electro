package dk.ek.chri585u.electro.repository;

import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(Collection<OrderStatus> statuses);
}
