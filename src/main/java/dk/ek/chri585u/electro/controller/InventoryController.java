package dk.ek.chri585u.electro.controller;

import dk.ek.chri585u.electro.dto.InventoryRowDTO;
import dk.ek.chri585u.electro.dto.StockCountDTO;
import dk.ek.chri585u.electro.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryRowDTO>> list() {
        return ResponseEntity.ok(inventoryService.listInventory());
    }

    @PostMapping("/{componentId}/count")
    public ResponseEntity<StockCountDTO> recordCount(@PathVariable Long componentId,
                                                     @Valid @RequestBody StockCountDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.recordCount(componentId, dto));
    }
}
