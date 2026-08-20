package dk.ek.chri585u.electro.model;

import jakarta.persistence.*;

@Entity
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "internal_number", nullable = false, unique = true)
    private Integer internalNumber;

    @Column(name = "external_part_number", length = 60)
    private String externalPartNumber;

    @Column(nullable = false)
    private boolean discontinued = false;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public Component() {}

    public Component(String name, Integer internalNumber, String externalPartNumber, Supplier supplier) {
        this.name = name;
        this.internalNumber = internalNumber;
        this.externalPartNumber = externalPartNumber;
        this.supplier = supplier;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getInternalNumber() { return internalNumber; }
    public void setInternalNumber(Integer internalNumber) { this.internalNumber = internalNumber; }
    public String getExternalPartNumber() { return externalPartNumber; }
    public void setExternalPartNumber(String externalPartNumber) { this.externalPartNumber = externalPartNumber; }
    public boolean isDiscontinued() { return discontinued; }
    public void setDiscontinued(boolean discontinued) { this.discontinued = discontinued; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

}
