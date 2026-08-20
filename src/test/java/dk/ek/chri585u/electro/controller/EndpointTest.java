package dk.ek.chri585u.electro.controller;

import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderStatus;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.OrderRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndpointTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ComponentRepository componentRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SupplierRepository supplierRepository;

    @Test
    void getComponents_returns200() throws Exception {
        mockMvc.perform(get("/api/components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void createComponent_returns201() throws Exception {
        Long supplierId = firstSupplier().getId();

        mockMvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Testkomponent","internalNumber":9101,"supplierId":%d}
                    """.formatted(supplierId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Testkomponent"));
    }

    @Test
    void discontinueComponent_returns200() throws Exception {
        Long componentId = orderableComponent().getId();

        mockMvc.perform(patch("/api/components/{id}/discontinue", componentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.discontinued").value(true));
    }

    @Test
    void getSuppliers_returns200() throws Exception {
        mockMvc.perform(get("/api/suppliers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void getOrders_returns200() throws Exception {
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void getOrderById_returns200() throws Exception {
        Long orderId = orderWithStatus(OrderStatus.DRAFT).getId();

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void createOrder_returns201() throws Exception {
        Long supplierId = firstSupplier().getId();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"supplierId":%d,"trackingCode":"TEST-1","lines":[]}
                    """.formatted(supplierId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void addOrderLine_returns201() throws Exception {
        Long orderId = orderWithStatus(OrderStatus.DRAFT).getId();
        Long componentId = orderableComponent().getId();

        mockMvc.perform(post("/api/orders/{id}/lines", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"componentId":%d,"quantity":2}
                    """.formatted(componentId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.lines.length()").value(2));
    }

    @Test
    void markOrderSent_returns200() throws Exception {
        Long orderId = orderWithStatus(OrderStatus.DRAFT).getId();

        mockMvc.perform(patch("/api/orders/{id}/send", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void markOrderReceived_returns200() throws Exception {
        Long orderId = orderWithStatus(OrderStatus.SENT).getId();

        mockMvc.perform(patch("/api/orders/{id}/receive", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void getInventory_returns200() throws Exception {
        mockMvc.perform(get("/api/inventory"))
            .andExpect(status().isOk());
    }

    @Test
    void recordStockCount_returns201() throws Exception {
        Long componentId = orderableComponent().getId();

        mockMvc.perform(post("/api/inventory/{id}/count", componentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"actualQuantity":42,"countedBy":"Christian"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.actualQuantity").value(42));
    }

    @Test
    void getAssemblies_returns200() throws Exception {
        mockMvc.perform(get("/api/assemblies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].parts").isArray());
    }

    @Test
    void invalidComponent_returns400() throws Exception {
        mockMvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"","internalNumber":9102}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Navn må ikke være tomt"));
    }

    @Test
    void unknownComponent_returns404() throws Exception {
        mockMvc.perform(patch("/api/components/99999/discontinue"))
            .andExpect(status().isNotFound());
    }

    @Test
    void duplicateInternalNumber_returns409() throws Exception {
        Integer existingNumber = orderableComponent().getInternalNumber();

        mockMvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Duplikat","internalNumber":%d}
                    """.formatted(existingNumber)))
            .andExpect(status().isConflict());
    }

    private Supplier firstSupplier() {
        return supplierRepository.findAll().stream().findFirst().orElseThrow();
    }

    private Component orderableComponent() {
        return componentRepository.findAll().stream()
            .filter(component -> component.getSupplier() != null && !component.isDiscontinued())
            .findFirst()
            .orElseThrow();
    }

    private Order orderWithStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream().findFirst().orElseThrow();
    }
}
