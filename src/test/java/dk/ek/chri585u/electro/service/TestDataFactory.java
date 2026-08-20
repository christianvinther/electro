package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.dto.OrderLineDTO;

import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static OrderDTO draftOrder(Long supplierId) {
        return draftOrder(supplierId, null, null);
    }

    public static OrderDTO draftOrder(Long supplierId, String trackingCode, List<OrderLineDTO> lines) {
        return new OrderDTO(null, supplierId, null, trackingCode, null, null, null, null, lines);
    }

    public static OrderLineDTO line(Long componentId, int quantity) {
        return new OrderLineDTO(null, componentId, null, quantity);
    }
}
