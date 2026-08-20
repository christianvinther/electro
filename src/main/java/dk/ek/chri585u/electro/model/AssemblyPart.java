package dk.ek.chri585u.electro.model;

import jakarta.persistence.*;

@Entity
public class AssemblyPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "assembly_id", nullable = false)
    private Assembly assembly;

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    public AssemblyPart() {}

    public AssemblyPart(int quantity, Component component) {
        this.quantity = quantity;
        this.component = component;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Assembly getAssembly() { return assembly; }
    public void setAssembly(Assembly assembly) { this.assembly = assembly; }
    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }
}
