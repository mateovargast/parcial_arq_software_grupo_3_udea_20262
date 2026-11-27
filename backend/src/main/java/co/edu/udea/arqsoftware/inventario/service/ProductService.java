package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.config.CacheConfig;
import co.edu.udea.arqsoftware.inventario.domain.InventoryItem;
import co.edu.udea.arqsoftware.inventario.domain.Product;
import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import co.edu.udea.arqsoftware.inventario.repository.InventoryRepository;
import co.edu.udea.arqsoftware.inventario.repository.ProductRepository;
import co.edu.udea.arqsoftware.inventario.repository.WarehouseRepository;
import co.edu.udea.arqsoftware.inventario.web.ApiException;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductBatchItemRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductBatchRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Page<Product> findAll(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.search(query.trim(), pageable);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el producto solicitado."));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_DASHBOARD, allEntries = true)
    public List<Product> registerBatch(ProductBatchRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.almacenId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el almacén seleccionado."));

        Set<String> skus = new HashSet<>();
        for (ProductBatchItemRequest item : request.productos()) {
            String normalizedSku = item.sku().trim().toUpperCase(Locale.ROOT);
            if (!skus.add(normalizedSku)) {
                throw new ApiException(HttpStatus.CONFLICT, "El lote contiene SKU duplicados.");
            }
        }
        if (productRepository.existsByNormalizedSkuIn(skus)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Uno o más SKU ya existen. El lote no modificó productos ni existencias.");
        }

        List<Product> products = new ArrayList<>();
        List<InventoryItem> inventory = new ArrayList<>();
        for (ProductBatchItemRequest item : request.productos()) {
            Product product = new Product(
                    item.sku().trim().toUpperCase(Locale.ROOT),
                    item.nombre().trim(),
                    item.descripcion() == null || item.descripcion().isBlank() ? null : item.descripcion().trim(),
                    item.categoria().trim(),
                    item.unidadMedida().trim(),
                    item.precioUnitario());
            products.add(product);
        }
        products = productRepository.saveAllAndFlush(products);
        for (int i = 0; i < products.size(); i++) {
            ProductBatchItemRequest item = request.productos().get(i);
            inventory.add(new InventoryItem(warehouse, products.get(i), item.cantidadInicial(), item.stockMinimo()));
        }
        inventoryRepository.saveAllAndFlush(inventory);
        return products;
    }
}
