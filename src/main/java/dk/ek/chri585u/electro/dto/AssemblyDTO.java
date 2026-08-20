package dk.ek.chri585u.electro.dto;

import java.util.List;

public record AssemblyDTO(
        Long id,
        Long producedComponentId,
        String producedComponentName,
        List<AssemblyPartDTO> parts
) {}
