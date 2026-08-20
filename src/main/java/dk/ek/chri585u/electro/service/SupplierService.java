package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.dto.SupplierDTO;
import dk.ek.chri585u.electro.mapper.DtoMapper;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> getAll() {
        return supplierRepository.findAll().stream()
            .map(DtoMapper::toSupplierDTO)
            .toList();
    }
}
