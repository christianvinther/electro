package dk.ek.chri585u.electro.mapper;

import dk.ek.chri585u.electro.model.Assembly;
import dk.ek.chri585u.electro.model.AssemblyPart;
import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.dto.AssemblyDTO;
import dk.ek.chri585u.electro.dto.AssemblyPartDTO;
import dk.ek.chri585u.electro.dto.ComponentDTO;
import dk.ek.chri585u.electro.dto.OrderDTO;
import dk.ek.chri585u.electro.dto.OrderLineDTO;
import dk.ek.chri585u.electro.dto.StockCountDTO;
import dk.ek.chri585u.electro.dto.SupplierDTO;
import dk.ek.chri585u.electro.model.StockCount;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;

import java.util.List;

public class DtoMapper {

    private DtoMapper() {}

    public static SupplierDTO toSupplierDTO(Supplier entity) {
        return new SupplierDTO(
                entity.getId(),
                entity.getName(),
                entity.getAddress()
        );
    }

    public static ComponentDTO toComponentDTO(Component entity, boolean orderable) {
        Supplier s = entity.getSupplier();
        return new ComponentDTO(
                entity.getId(),
                entity.getName(),
                entity.getInternalNumber(),
                entity.getExternalPartNumber(),
                entity.isDiscontinued(),
                s != null ? s.getId() : null,
                s != null ? s.getName() : null,
                orderable
        );
    }

    public static AssemblyPartDTO toAssemblyPartDTO(AssemblyPart part) {
        Component c = part.getComponent();
        return new AssemblyPartDTO(
                part.getId(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                part.getQuantity()
        );
    }

    public static AssemblyDTO toAssemblyDTO(Assembly entity) {
        List<AssemblyPartDTO> parts = entity.getParts().stream()
                .map(DtoMapper::toAssemblyPartDTO)
                .toList();
        Component produced = entity.getProducedComponent();
        return new AssemblyDTO(
                entity.getId(),
                produced != null ? produced.getId() : null,
                produced != null ? produced.getName() : null,
                parts
        );
    }

    public static StockCountDTO toStockCountDTO(StockCount entity) {
        Component c = entity.getComponent();
        return new StockCountDTO(
                entity.getId(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                entity.getActualQuantity(),
                entity.getCountedBy(),
                entity.getCountedAt()
        );
    }

    public static OrderLineDTO toOrderLineDTO(OrderLine line) {
        Component c = line.getComponent();
        return new OrderLineDTO(
                line.getId(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                line.getQuantity()
        );
    }

    public static OrderDTO toOrderDTO(Order entity) {
        Supplier s = entity.getSupplier();
        List<OrderLineDTO> lines = entity.getLines().stream()
                .map(DtoMapper::toOrderLineDTO)
                .toList();
        return new OrderDTO(
                entity.getId(),
                s != null ? s.getId() : null,
                s != null ? s.getName() : null,
                entity.getTrackingCode(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getSentDate(),
                entity.getExpectedDeliveryDate(),
                entity.getReceivedDate(),
                lines
        );
    }
}
