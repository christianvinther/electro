package dk.ek.chri585u.electro.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Assembly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "produced_component_id", unique = true, nullable = false)
    private Component producedComponent;

    @OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssemblyPart> parts = new HashSet<>();

    public Assembly() {}

    public Assembly(Component producedComponent) {
        this.producedComponent = producedComponent;
    }

    // Holder begge sider af JPA-relationen opdateret.
    public void addPart(AssemblyPart part) {
        parts.add(part);
        part.setAssembly(this);
    }

    public void removePart(AssemblyPart part) {
        parts.remove(part);
        part.setAssembly(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Component getProducedComponent() { return producedComponent; }
    public void setProducedComponent(Component producedComponent) { this.producedComponent = producedComponent; }
    public Set<AssemblyPart> getParts() { return parts; }
}
