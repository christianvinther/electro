package dk.ek.chri585u.electro.dto;

public record AssemblyPartDTO(
        Long id,
        Long componentId,
        String componentName,
        int quantity
) {}
