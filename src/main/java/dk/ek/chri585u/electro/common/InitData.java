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

// Det fulde navn bruges, fordi vores modelklasse også hedder Component.
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
        // Startdata skal kun sættes ind i en tom database.
        if (supplierRepository.count() > 0) {
            return;
        }

        // Leverandører
        Supplier elfa = supplierRepository.save(new Supplier("Elfa Distrelec", "Industrivej 14, 2600 Glostrup"));
        Supplier rs = supplierRepository.save(new Supplier("RS Components", "Lautrupvang 4, 2750 Ballerup"));
        Supplier farnell = supplierRepository.save(new Supplier("Farnell Danmark", "Strandvejen 60, 2900 Hellerup"));

        // Bestilbare komponenter
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

        Component ledKitProduct = componentRepository.save(new Component("Lysende LED", 2001, null, null));
        Component flashlightProduct = componentRepository.save(new Component("Lommelampe (samlesæt)", 2002, null, null));

        // Styklister
        Assembly ledKit = new Assembly(ledKitProduct);
        ledKit.addPart(new AssemblyPart(1, led));
        ledKit.addPart(new AssemblyPart(1, resistor));
        ledKit.addPart(new AssemblyPart(1, holder));
        ledKit.addPart(new AssemblyPart(1, battery));
        assemblyRepository.save(ledKit);

        Assembly flashlight = new Assembly(flashlightProduct);
        flashlight.addPart(new AssemblyPart(1, ledKitProduct));
        flashlight.addPart(new AssemblyPart(1, trykknap));
        flashlight.addPart(new AssemblyPart(1, print));
        assemblyRepository.save(flashlight);

        // Bestillinger
        Order draftOrder = new Order(farnell);
        draftOrder.addLine(new OrderLine(25, holder));
        orderRepository.save(draftOrder);

        Order activeOrder = new Order(elfa);
        activeOrder.setTrackingCode("DK1234567890");
        activeOrder.setStatus(OrderStatus.SENT);
        activeOrder.setSentDate(LocalDate.now().minusDays(5));
        activeOrder.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
        activeOrder.addLine(new OrderLine(10, led));
        activeOrder.addLine(new OrderLine(10, resistor));
        activeOrder.addLine(new OrderLine(10, greenLed));
        orderRepository.save(activeOrder);

        Order completedOrder = new Order(rs);
        completedOrder.setTrackingCode("DK9876543210");
        completedOrder.setStatus(OrderStatus.RECEIVED);
        completedOrder.setSentDate(LocalDate.now().minusDays(20));
        completedOrder.setExpectedDeliveryDate(LocalDate.now().minusDays(12));
        completedOrder.setReceivedDate(LocalDate.now().minusDays(10));
        completedOrder.addLine(new OrderLine(100, resistor220));
        orderRepository.save(completedOrder);

        // Lageroptællinger
        stockCountRepository.save(new StockCount(resistor220, 95, "Christian", LocalDateTime.now().minusDays(2)));
        stockCountRepository.save(new StockCount(led, 7, "Christian", LocalDateTime.now().minusDays(1)));
    }
}
