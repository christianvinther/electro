package dk.ek.chri585u.electro.controller;

import dk.ek.chri585u.electro.service.ComponentService;
import dk.ek.chri585u.electro.dto.ComponentDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentService componentService;

    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @GetMapping
    public ResponseEntity<List<ComponentDTO>> getAll() {
        return ResponseEntity.ok(componentService.getAll());
    }

    @PostMapping
    public ResponseEntity<ComponentDTO> create(@Valid @RequestBody ComponentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentService.create(dto));
    }

    @PatchMapping("/{id}/discontinue")
    public ResponseEntity<ComponentDTO> discontinue(@PathVariable Long id) {
        return ResponseEntity.ok(componentService.discontinue(id));
    }
}
