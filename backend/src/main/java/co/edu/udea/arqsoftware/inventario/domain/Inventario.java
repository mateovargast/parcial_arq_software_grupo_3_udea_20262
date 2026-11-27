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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventario_almacen_producto",
                columnNames = {"almacen_id", "producto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @CreationTimestamp
    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private Instant fechaIngreso;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Inventario(Almacen almacen, Producto producto, Integer cantidad, Integer stockMinimo) {
        this.almacen = almacen;
        this.producto = producto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public boolean isStockBajo() {
        return cantidad <= stockMinimo;
    }
}
