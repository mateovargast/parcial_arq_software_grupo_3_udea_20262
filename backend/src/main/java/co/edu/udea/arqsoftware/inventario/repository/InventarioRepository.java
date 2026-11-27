package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            select count(i) as totalProductos,
                   coalesce(sum(i.cantidad), 0) as unidadesTotales,
                   coalesce(sum(i.cantidad * p.precioUnitario), 0) as valorTotal,
                   coalesce(sum(case when i.cantidad <= i.stockMinimo then 1 else 0 end), 0) as productosStockBajo
            from Inventario i join i.producto p
            where i.almacen.id = :almacenId
            """)
    ResumenInventarioView resumirPorAlmacen(@Param("almacenId") Long almacenId);
}
