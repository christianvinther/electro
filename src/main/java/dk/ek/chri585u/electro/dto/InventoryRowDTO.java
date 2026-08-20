package dk.ek.chri585u.electro.dto;

import java.time.LocalDateTime;

public record InventoryRowDTO(
        Long componentId,
        String componentName,
        int totalReceived,
        Integer lastCountedQuantity,
        LocalDateTime lastCountedAt,
        String lastCountedBy
) {}
