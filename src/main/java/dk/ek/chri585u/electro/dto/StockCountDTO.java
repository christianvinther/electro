package dk.ek.chri585u.electro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record StockCountDTO(
        Long id,
        Long componentId,
        String componentName,
        @Min(value = 0, message = "Antal må ikke være negativt") int actualQuantity,
        @NotBlank(message = "Optæller skal angives") String countedBy,
        LocalDateTime countedAt
) {}
