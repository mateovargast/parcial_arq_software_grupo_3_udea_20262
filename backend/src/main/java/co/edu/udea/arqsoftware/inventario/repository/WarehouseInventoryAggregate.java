package co.edu.udea.arqsoftware.inventario.repository;

public interface WarehouseInventoryAggregate {
    Long getAlmacenId();

    String getAlmacen();

    Long getTotalUnidades();

    Long getProductos();

    Long getStockBajo();
}
