package co.edu.udea.arqsoftware.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import co.edu.udea.arqsoftware.inventario.repository.InventoryRepository;
import co.edu.udea.arqsoftware.inventario.repository.ProductRepository;
import co.edu.udea.arqsoftware.inventario.repository.WarehouseRepository;
import co.edu.udea.arqsoftware.inventario.web.ApiException;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductBatchItemRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductBatchRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void rejectsDuplicateSkusInBatchBeforeWriting() {
        when(warehouseRepository.findById(3L)).thenReturn(Optional.of(mock(Warehouse.class)));
        ProductBatchItemRequest item = new ProductBatchItemRequest(
                "SKU-1", "Producto", null, "General", "UNIDAD",
                new BigDecimal("10.00"), 4, 1);
        ProductBatchRequest request = new ProductBatchRequest(3L, List.of(item, item));

        ApiException exception = assertThrows(ApiException.class, () -> productService.registerBatch(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(productRepository, never()).saveAllAndFlush(org.mockito.ArgumentMatchers.anyList());
        verify(inventoryRepository, never()).saveAllAndFlush(org.mockito.ArgumentMatchers.anyList());
    }
}
