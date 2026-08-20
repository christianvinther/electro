package dk.ek.chri585u.electro.service;

import dk.ek.chri585u.electro.repository.AssemblyRepository;
import dk.ek.chri585u.electro.dto.AssemblyDTO;
import dk.ek.chri585u.electro.mapper.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssemblyService {

    private final AssemblyRepository assemblyRepository;

    public AssemblyService(AssemblyRepository assemblyRepository) {
        this.assemblyRepository = assemblyRepository;
    }

    @Transactional(readOnly = true)
    public List<AssemblyDTO> getAll() {
        return assemblyRepository.findAll().stream()
            .map(DtoMapper::toAssemblyDTO)
            .toList();
    }
}
