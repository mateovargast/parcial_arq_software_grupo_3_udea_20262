package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    @EntityGraph(attributePaths = "producto")
    Page<Inventario> findByAlmacenId(Long almacenId, Pageable pageable);

    @Query("""
            select i.producto.sku from Inventario i
            where i.almacen.id = :almacenId and i.producto.sku in :skus
            """)
    List<String> findSkusExistentesEnAlmacen(@Param("almacenId") Long almacenId,
                                             @Param("skus") Collection<String> skus);

    @Query("""
            select coalesce(sum(i.cantidad * i.producto.precioUnitario), 0) from Inventario i
            where i.almacen.id = :almacenId
            """)
    BigDecimal calcularValorTotal(@Param("almacenId") Long almacenId);

    @Query("""
            select count(i) from Inventario i
            where i.almacen.id = :almacenId and i.cantidad <= i.stockMinimo
            """)
    long contarStockBajo(@Param("almacenId") Long almacenId);
}
