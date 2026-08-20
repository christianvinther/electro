package dk.ek.chri585u.electro.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_count")
public class StockCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(name = "actual_quantity", nullable = false)
    private int actualQuantity;

    @Column(name = "counted_by", nullable = false, length = 100)
    private String countedBy;

    @Column(name = "counted_at", nullable = false)
    private LocalDateTime countedAt;

    public StockCount() {}

    public StockCount(Component component, int actualQuantity, String countedBy, LocalDateTime countedAt) {
        this.component = component;
        this.actualQuantity = actualQuantity;
        this.countedBy = countedBy;
        this.countedAt = countedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }
    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
    public String getCountedBy() { return countedBy; }
    public void setCountedBy(String countedBy) { this.countedBy = countedBy; }
    public LocalDateTime getCountedAt() { return countedAt; }
    public void setCountedAt(LocalDateTime countedAt) { this.countedAt = countedAt; }
}
