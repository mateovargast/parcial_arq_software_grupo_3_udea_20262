package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.repository.ProductoRepository;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoResponse obtener(Long productoId) {
        return productoRepository.findById(productoId)
                .map(ProductoResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
    }
}
