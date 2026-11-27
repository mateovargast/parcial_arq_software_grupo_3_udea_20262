package co.edu.udea.arqsoftware.inventario.repository;

import co.edu.udea.arqsoftware.inventario.domain.InventoryItem;
import co.edu.udea.arqsoftware.inventario.web.dto.WarehouseInventorySummary;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    @EntityGraph(attributePaths = "product")
    Page<InventoryItem> findByWarehouse_Id(Long warehouseId, Pageable pageable);

    @EntityGraph(attributePaths = "product")
    @Query("""
            select i from InventoryItem i
            where i.warehouse.id = :warehouseId
              and (lower(i.product.sku) like lower(concat('%', :term, '%'))
                   or lower(i.product.nombre) like lower(concat('%', :term, '%'))
                   or lower(i.product.categoria) like lower(concat('%', :term, '%')))
            """)
    Page<InventoryItem> searchByWarehouse(
            @Param("warehouseId") Long warehouseId,
            @Param("term") String term,
            Pageable pageable);

    @EntityGraph(attributePaths = {"warehouse", "product"})
    Page<InventoryItem> findByProduct_Id(Long productId, Pageable pageable);

    @Query("select coalesce(sum(i.cantidad), 0) from InventoryItem i")
    long sumAllUnits();

    @Query("select count(i) from InventoryItem i where i.cantidad <= i.stockMinimo")
    long countLowStock();

    @Query(value = """
            select a.id as "almacenId",
                   a.nombre as "almacen",
                   coalesce(sum(i.cantidad), 0) as "totalUnidades",
                   count(i.id) as "productos",
                   coalesce(sum(case when i.cantidad <= i.stock_minimo then 1 else 0 end), 0) as "stockBajo"
            from almacen a
            left join inventario i on i.almacen_id = a.id
            group by a.id, a.nombre
            order by a.nombre
            """, nativeQuery = true)
    List<WarehouseInventoryAggregate> summarizeByWarehouse();
}
