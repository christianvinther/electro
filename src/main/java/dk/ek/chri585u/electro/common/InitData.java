package dk.ek.chri585u.electro.common;

import dk.ek.chri585u.electro.model.Assembly;
import dk.ek.chri585u.electro.model.AssemblyPart;
import dk.ek.chri585u.electro.model.Component;
import dk.ek.chri585u.electro.model.Supplier;
import dk.ek.chri585u.electro.repository.AssemblyRepository;
import dk.ek.chri585u.electro.repository.ComponentRepository;
import dk.ek.chri585u.electro.repository.SupplierRepository;
import dk.ek.chri585u.electro.model.StockCount;
import dk.ek.chri585u.electro.repository.StockCountRepository;
import dk.ek.chri585u.electro.model.Order;
import dk.ek.chri585u.electro.model.OrderLine;
import dk.ek.chri585u.electro.model.OrderStatus;
import dk.ek.chri585u.electro.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Fully qualified annotation because the model class Component is imported —
// the domain type wins the short name, Spring's stereotype takes the long one.
@org.springframework.stereotype.Component
public class InitData implements CommandLineRunner {

    private final SupplierRepository supplierRepository;
    private final ComponentRepository componentRepository;
    private final AssemblyRepository assemblyRepository;
    private final OrderRepository orderRepository;
    private final StockCountRepository stockCountRepository;

    public InitData(SupplierRepository supplierRepository,
                    ComponentRepository componentRepository,
                    AssemblyRepository assemblyRepository,
                    OrderRepository orderRepository,
                    StockCountRepository stockCountRepository) {
        this.supplierRepository = supplierRepository;
        this.componentRepository = componentRepository;
        this.assemblyRepository = assemblyRepository;
        this.orderRepository = orderRepository;
        this.stockCountRepository = stockCountRepository;
    }

    @Override
    public void run(String... args) {
        // Guard: seed only an empty database. With H2 create-drop this is always
        // empty; with MySQL and ddl-auto=update it prevents duplicate seed data.
        if (supplierRepository.count() > 0) {
            return;
        }

        // Suppliers
        Supplier elfa = supplierRepository.save(new Supplier("Elfa Distrelec", "Industrivej 14, 2600 Glostrup"));
        Supplier rs = supplierRepository.save(new Supplier("RS Components", "Lautrupvang 4, 2750 Ballerup"));
        Supplier farnell = supplierRepository.save(new Supplier("Farnell Danmark", "Strandvejen 60, 2900 Hellerup"));

        // Orderable components — 10 rows including the four Lysende LED parts
        Component led = componentRepository.save(new Component("LED 5mm rød", 1001, "L-7104ID", elfa));
        Component resistor = componentRepository.save(new Component("Modstand 1kΩ", 1002, "CFR-25JB-52-1K", rs));
        Component holder = componentRepository.save(new Component("Batteriholder 9V", 1003, "BH9V-PC", farnell));
        Component battery = componentRepository.save(new Component("9V batteri", 1004, "MN1604", elfa));
        Component greenLed = componentRepository.save(new Component("LED 5mm grøn", 1005, "L-7104GD", elfa));
        Component resistor220 = componentRepository.save(new Component("Modstand 220Ω", 1006, "CFR-25JB-52-220R", rs));
        componentRepository.save(new Component("Kondensator 10µF", 1007, "ECA-1HM100", farnell));
        componentRepository.save(new Component("Transistor BC547", 1008, "BC547B", rs));
        Component trykknap = componentRepository.save(new Component("Trykknap", 1009, "B3F-1000", elfa));
        Component print = componentRepository.save(new Component("Print 5x7cm", 1010, "PCB-5x7", farnell));

        // Assembled-only components (no supplier — not orderable)
        Component ledKitProduct = componentRepository.save(new Component("Lysende LED", 2001, null, null));
        Component flashlightProduct = componentRepository.save(new Component("Lommelampe (samlesæt)", 2002, null, null));

        // Inner assembly — the brief's "Lysende LED" kit
        Assembly ledKit = new Assembly(ledKitProduct);
        ledKit.addPart(new AssemblyPart(1, led));
        ledKit.addPart(new AssemblyPart(1, resistor));
        ledKit.addPart(new AssemblyPart(1, holder));
        ledKit.addPart(new AssemblyPart(1, battery));
        assemblyRepository.save(ledKit);

        // Outer assembly — Lommelampe includes the assembled-only ledKitProduct as a part.
        // This is the recursion proof: a stykliste's produced component is itself a part of another stykliste.
        Assembly flashlight = new Assembly(flashlightProduct);
        flashlight.addPart(new AssemblyPart(1, ledKitProduct));
        flashlight.addPart(new AssemblyPart(1, trykknap));
        flashlight.addPart(new AssemblyPart(1, print));
        assemblyRepository.save(flashlight);

        // Draft order — being formulated, no dates set yet
        Order draftOrder = new Order(farnell);
        draftOrder.addLine(new OrderLine(25, holder));
        orderRepository.save(draftOrder);

        // Active sent order — 3 lines × 10 stk., status SENT
        Order activeOrder = new Order(elfa);
        activeOrder.setTrackingCode("DK1234567890");
        activeOrder.setStatus(OrderStatus.SENT);
        activeOrder.setSentDate(LocalDate.now().minusDays(5));
        activeOrder.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
        activeOrder.addLine(new OrderLine(10, led));
        activeOrder.addLine(new OrderLine(10, resistor));
        activeOrder.addLine(new OrderLine(10, greenLed));
        orderRepository.save(activeOrder);

        // Completed received order — 1 line × 100 stk., status RECEIVED
        Order completedOrder = new Order(rs);
        completedOrder.setTrackingCode("DK9876543210");
        completedOrder.setStatus(OrderStatus.RECEIVED);
        completedOrder.setSentDate(LocalDate.now().minusDays(20));
        completedOrder.setExpectedDeliveryDate(LocalDate.now().minusDays(12));
        completedOrder.setReceivedDate(LocalDate.now().minusDays(10));
        completedOrder.addLine(new OrderLine(100, resistor220));
        orderRepository.save(completedOrder);

        // Initial optællinger so the inventory page has something interesting on first load
        // 1) resistor220: received 100, counted 95 — to show counted < received discrepancy
        stockCountRepository.save(new StockCount(resistor220, 95, "Christian", LocalDateTime.now().minusDays(2)));
        // 2) led: counted 7 without ever being received — demonstrates count-without-receipt
        stockCountRepository.save(new StockCount(led, 7, "Christian", LocalDateTime.now().minusDays(1)));
    }
}
