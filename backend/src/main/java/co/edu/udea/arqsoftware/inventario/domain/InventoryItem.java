package co.edu.udea.arqsoftware.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "inventario")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "fecha_ingreso", insertable = false, updatable = false)
    private Instant fechaIngreso;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    protected InventoryItem() {
    }

    public InventoryItem(Warehouse warehouse, Product product, int cantidad, int stockMinimo) {
        this.warehouse = warehouse;
        this.product = product;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public Long getId() {
        return id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public Instant getFechaIngreso() {
        return fechaIngreso;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
