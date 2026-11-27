package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.InventoryItem;
import co.edu.udea.arqsoftware.inventario.repository.InventoryRepository;
import co.edu.udea.arqsoftware.inventario.web.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            WarehouseService warehouseService,
            ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    public Page<InventoryItem> findByWarehouse(Long warehouseId, String query, Pageable pageable) {
        warehouseService.getById(warehouseId);
        if (query == null || query.isBlank()) {
            return inventoryRepository.findByWarehouse_Id(warehouseId, pageable);
        }
        return inventoryRepository.searchByWarehouse(
                warehouseId,
                query.trim(),
                pageable);
    }

    public Page<InventoryItem> findByProduct(Long productId, Pageable pageable) {
        productService.getById(productId);
        return inventoryRepository.findByProduct_Id(productId, pageable);
    }
}
