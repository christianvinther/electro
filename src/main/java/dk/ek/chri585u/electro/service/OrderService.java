package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import dk.ek.chri585u.electro.common.NotFoundException;
import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.dto.OrderLineDTO;
import dk.ek.chri585u.electro.mapper.DtoMapper;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;
import dk.ek.chri585u.electro.model.OrderStatus;
import dk.ek.chri585u.electro.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ComponentRepository componentRepository;

    public OrderService(OrderRepository orderRepository,
                        SupplierRepository supplierRepository,
                        ComponentRepository componentRepository) {
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
        this.componentRepository = componentRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> list(String statusFilter) {
        List<Order> orders;
        if (statusFilter == null || statusFilter.isBlank()) {
            orders = orderRepository.findAll();
        } else if (statusFilter.equalsIgnoreCase("open")) {
            orders = orderRepository.findByStatusIn(List.of(OrderStatus.DRAFT, OrderStatus.SENT));
        } else {
            OrderStatus status = parseStatus(statusFilter);
            orders = orderRepository.findByStatus(status);
        }
        return orders.stream().map(DtoMapper::toOrderDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrderDTO getById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Bestilling ikke fundet med id: " + id));
        return DtoMapper.toOrderDTO(order);
    }

    @Transactional
    public OrderDTO createDraft(OrderDTO dto) {
        if (dto.lines() != null && !dto.lines().isEmpty()) {
            throw new IllegalArgumentException(
                "Linjer skal tilføjes efter bestillingen er oprettet");
        }
        Supplier supplier = supplierRepository.findById(dto.supplierId())
            .orElseThrow(() -> new NotFoundException("Leverandør ikke fundet med id: " + dto.supplierId()));
        Order order = new Order(supplier);
        order.setTrackingCode(dto.trackingCode());
        order.setExpectedDeliveryDate(dto.expectedDeliveryDate());
        return DtoMapper.toOrderDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderDTO addLine(Long orderId, OrderLineDTO dto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Bestilling ikke fundet med id: " + orderId));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException(
                "Kan ikke tilføje linjer til en bestilling med status " + order.getStatus());
        }
        order.addLine(buildLine(dto));
        return DtoMapper.toOrderDTO(order);
    }

    private OrderLine buildLine(OrderLineDTO dto) {
        Component component = componentRepository.findById(dto.componentId())
            .orElseThrow(() -> new NotFoundException("Komponent ikke fundet med id: " + dto.componentId()));
        boolean orderable = component.getSupplier() != null && !component.isDiscontinued();
        if (!orderable) {
            throw new IllegalStateException(component.isDiscontinued()
                ? "Komponenten '" + component.getName() + "' er udgået og kan ikke bestilles"
                : "Komponenten '" + component.getName() + "' kan ikke bestilles — den skal samles");
        }
        return new OrderLine(dto.quantity(), component);
    }

    @Transactional
    public OrderDTO markSent(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Bestilling ikke fundet med id: " + orderId));
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException(
                "Kun kladde-bestillinger kan markeres som sendt (nuværende status: " + order.getStatus() + ")");
        }
        if (order.getLines().isEmpty()) {
            throw new IllegalStateException("Kan ikke sende en bestilling uden linjer");
        }
        order.setStatus(OrderStatus.SENT);
        order.setSentDate(LocalDate.now());
        return DtoMapper.toOrderDTO(order);
    }

    @Transactional
    public OrderDTO markReceived(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Bestilling ikke fundet med id: " + orderId));
        if (order.getStatus() != OrderStatus.SENT) {
            throw new IllegalStateException(
                "Kun sendte bestillinger kan markeres som modtaget (nuværende status: " + order.getStatus() + ")");
        }
        order.setStatus(OrderStatus.RECEIVED);
        order.setReceivedDate(LocalDate.now());
        return DtoMapper.toOrderDTO(order);
    }

    private OrderStatus parseStatus(String raw) {
        try {
            return OrderStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Ukendt status '" + raw
                + "' — brug open, draft, sent eller received");
        }
    }
}
