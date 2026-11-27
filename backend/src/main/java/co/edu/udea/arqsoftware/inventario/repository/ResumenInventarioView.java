package co.edu.udea.arqsoftware.inventario.repository;

import java.math.BigDecimal;

public interface ResumenInventarioView {

    Long getTotalProductos();

    Long getUnidadesTotales();

    BigDecimal getValorTotal();

    Long getProductosStockBajo();
}
