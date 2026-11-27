package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemV2Response.EstadoStock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static co.edu.udea.arqsoftware.inventario.TestData.itemResponse;
import static org.assertj.core.api.Assertions.assertThat;

class InventarioItemV2ResponseTest {

    @Test
    void productoAgotado() {
        InventarioItemV2Response v2 = InventarioItemV2Response.from(itemResponse(1L, 0, 10));

        assertThat(v2.estadoStock()).isEqualTo(EstadoStock.AGOTADO);
        assertThat(v2.alerta()).contains("agotado");
        assertThat(v2.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void productoConStockBajoIndicaUnidadesFaltantes() {
        InventarioItemV2Response v2 = InventarioItemV2Response.from(itemResponse(1L, 3, 10));

        assertThat(v2.estadoStock()).isEqualTo(EstadoStock.BAJO);
        assertThat(v2.alerta()).contains("8 unidades");
        assertThat(v2.valorTotal()).isEqualByComparingTo("3000.00");
    }

    @Test
    void productoConStockNormalNoTieneAlerta() {
        InventarioItemV2Response v2 = InventarioItemV2Response.from(itemResponse(1L, 50, 10));

        assertThat(v2.estadoStock()).isEqualTo(EstadoStock.NORMAL);
        assertThat(v2.alerta()).isNull();
        assertThat(v2.valorTotal()).isEqualByComparingTo("50000.00");
    }
}
