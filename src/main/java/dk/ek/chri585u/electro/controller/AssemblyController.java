package dk.ek.chri585u.electro.controller;

import dk.ek.chri585u.electro.service.AssemblyService;
import dk.ek.chri585u.electro.dto.AssemblyDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assemblies")
public class AssemblyController {

    private final AssemblyService assemblyService;

    public AssemblyController(AssemblyService assemblyService) {
        this.assemblyService = assemblyService;
    }

    @GetMapping
    public ResponseEntity<List<AssemblyDTO>> getAll() {
        return ResponseEntity.ok(assemblyService.getAll());
    }
}
