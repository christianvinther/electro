package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import dk.ek.chri585u.electro.common.NotFoundException;
import dk.ek.chri585u.electro.dto.ComponentDTO;
import dk.ek.chri585u.electro.mapper.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final SupplierRepository supplierRepository;

    public ComponentService(ComponentRepository componentRepository, SupplierRepository supplierRepository) {
        this.componentRepository = componentRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<ComponentDTO> getAll() {
        return componentRepository.findAll().stream()
            .map(c -> DtoMapper.toComponentDTO(c, isOrderable(c)))
            .toList();
    }

    @Transactional
    public ComponentDTO create(ComponentDTO dto) {
        Supplier supplier = null;
        if (dto.supplierId() != null) {
            supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new NotFoundException("Leverandør ikke fundet med id: " + dto.supplierId()));
        }
        if (componentRepository.existsByInternalNumber(dto.internalNumber())) {
            throw new IllegalStateException(
                "En komponent med internt nummer " + dto.internalNumber() + " findes allerede");
        }
        Component entity = new Component(
            dto.name(),
            dto.internalNumber(),
            dto.externalPartNumber(),
            supplier
        );
        Component saved = componentRepository.save(entity);
        return DtoMapper.toComponentDTO(saved, isOrderable(saved));
    }

    @Transactional
    public ComponentDTO discontinue(Long id) {
        Component entity = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Komponent ikke fundet med id: " + id));
        entity.setDiscontinued(true);
        return DtoMapper.toComponentDTO(entity, isOrderable(entity));
    }

    private boolean isOrderable(Component component) {
        return component.getSupplier() != null && !component.isDiscontinued();
    }
}
