package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import co.edu.udea.arqsoftware.inventario.repository.WarehouseRepository;
import co.edu.udea.arqsoftware.inventario.web.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public Page<Warehouse> findAll(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return warehouseRepository.findAll(pageable);
        }
        return warehouseRepository.search(query.trim(), pageable);
    }

    public Warehouse getById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el almacén solicitado."));
    }
}
