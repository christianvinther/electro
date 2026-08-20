package dk.ek.chri585u.electro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineDTO(
        Long id,
        @NotNull(message = "Komponent-id skal angives") Long componentId,
        String componentName,
        @Min(value = 1, message = "Antal skal være mindst 1") int quantity
) {}
