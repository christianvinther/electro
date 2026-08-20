package dk.ek.chri585u.electro.dto;

// Kun læse-DTO: der findes intet skrive-endpoint for leverandører,
// så validering hører ikke hjemme her endnu.
public record SupplierDTO(
        Long id,
        String name,
        String address
) {}
