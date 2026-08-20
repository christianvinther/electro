package dk.ek.chri585u.electro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    public OrderLine() {}

    public OrderLine(int quantity, Component component) {
        this.quantity = quantity;
        this.component = component;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }
}
