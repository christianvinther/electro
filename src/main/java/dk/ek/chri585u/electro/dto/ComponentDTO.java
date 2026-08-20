package dk.ek.chri585u.electro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComponentDTO(
        Long id,
        @NotBlank(message = "Navn må ikke være tomt") String name,
        @NotNull(message = "Internt nummer skal angives") Integer internalNumber,
        String externalPartNumber,
        boolean discontinued,
        Long supplierId,
        String supplierName,
        boolean orderable
) {}
