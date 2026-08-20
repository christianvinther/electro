package dk.ek.chri585u.electro.controller;

import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.dto.OrderLineDTO;
import dk.ek.chri585u.electro.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> list(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.list(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(@Valid @RequestBody OrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createDraft(dto));
    }

    @PostMapping("/{id}/lines")
    public ResponseEntity<OrderDTO> addLine(@PathVariable Long id, @Valid @RequestBody OrderLineDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addLine(id, dto));
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<OrderDTO> markSent(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markSent(id));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<OrderDTO> markReceived(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markReceived(id));
    }
}
