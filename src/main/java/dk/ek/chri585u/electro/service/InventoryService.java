package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.common.NotFoundException;
import dk.ek.chri585u.electro.dto.InventoryRowDTO;
import dk.ek.chri585u.electro.dto.StockCountDTO;
import dk.ek.chri585u.electro.model.StockCount;
import dk.ek.chri585u.electro.repository.StockCountRepository;
import dk.ek.chri585u.electro.mapper.DtoMapper;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;
import dk.ek.chri585u.electro.model.OrderStatus;
import dk.ek.chri585u.electro.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final OrderRepository orderRepository;
    private final StockCountRepository stockCountRepository;
    private final ComponentRepository componentRepository;

    public InventoryService(OrderRepository orderRepository,
                            StockCountRepository stockCountRepository,
                            ComponentRepository componentRepository) {
        this.orderRepository = orderRepository;
        this.stockCountRepository = stockCountRepository;
        this.componentRepository = componentRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryRowDTO> listInventory() {
        Map<Long, Integer> receivedByComponent = new LinkedHashMap<>();
        Map<Long, String> nameByComponent = new LinkedHashMap<>();

        // Lageret bygger kun på bestillinger, der er modtaget.
        List<Order> receivedOrders = orderRepository.findByStatus(OrderStatus.RECEIVED);
        for (Order order : receivedOrders) {
            for (OrderLine line : order.getLines()) {
                Component c = line.getComponent();
                if (c == null) continue;
                receivedByComponent.merge(c.getId(), line.getQuantity(), Integer::sum);
                nameByComponent.putIfAbsent(c.getId(), c.getName());
            }
        }

        // En optalt komponent skal også vises, selv om modtaget antal er nul.
        for (StockCount sc : stockCountRepository.findAll()) {
            Component c = sc.getComponent();
            if (c == null) continue;
            receivedByComponent.putIfAbsent(c.getId(), 0);
            nameByComponent.putIfAbsent(c.getId(), c.getName());
        }

        return receivedByComponent.entrySet().stream()
            .map(e -> buildRow(e.getKey(), nameByComponent.get(e.getKey()), e.getValue()))
            .sorted(Comparator.comparing(InventoryRowDTO::componentName))
            .toList();
    }

    @Transactional
    public StockCountDTO recordCount(Long componentId, StockCountDTO dto) {
        if (dto.componentId() != null && !dto.componentId().equals(componentId)) {
            throw new IllegalArgumentException(
                "componentId i body (" + dto.componentId() + ") matcher ikke URL'en (" + componentId + ")");
        }
        Component component = componentRepository.findById(componentId)
            .orElseThrow(() -> new NotFoundException("Komponent ikke fundet med id: " + componentId));
        StockCount entity = new StockCount(
            component,
            dto.actualQuantity(),
            dto.countedBy(),
            LocalDateTime.now()
        );
        return DtoMapper.toStockCountDTO(stockCountRepository.save(entity));
    }

    private InventoryRowDTO buildRow(Long componentId, String componentName, int totalReceived) {
        // Den nyeste optælling bruges som det faktiske antal.
        StockCount latest = stockCountRepository
            .findTopByComponentIdOrderByCountedAtDesc(componentId)
            .orElse(null);
        return new InventoryRowDTO(
            componentId,
            componentName,
            totalReceived,
            latest != null ? latest.getActualQuantity() : null,
            latest != null ? latest.getCountedAt() : null,
            latest != null ? latest.getCountedBy() : null
        );
    }
}
