package dk.ek.chri585u.electro.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OrderDTO(
        Long id,
        @NotNull(message = "Leverandør-id skal angives") Long supplierId,
        String supplierName,
        String trackingCode,
        String status,
        LocalDate sentDate,
        LocalDate expectedDeliveryDate,
        LocalDate receivedDate,
        List<OrderLineDTO> lines
) {}
