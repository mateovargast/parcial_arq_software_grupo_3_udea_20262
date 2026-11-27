package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.repository.AlmacenRepository;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlmacenService {

    private final AlmacenRepository almacenRepository;

    @Transactional
    public AlmacenResponse crear(AlmacenRequest request) {
        if (almacenRepository.existsByCodigoSede(request.codigoSede())) {
            throw new ConflictoException("Ya existe un almacén con código de sede " + request.codigoSede());
        }
        Almacen almacen = new Almacen();
        almacen.setCodigoSede(request.codigoSede());
        almacen.setNombre(request.nombre());
        almacen.setCiudad(request.ciudad());
        almacen.setDireccion(request.direccion());
        almacen.setTelefono(request.telefono());
        return AlmacenResponse.from(almacenRepository.save(almacen));
    }

    public PageResponse<AlmacenResponse> listar(Pageable pageable) {
        return PageResponse.from(almacenRepository.findAll(pageable).map(AlmacenResponse::from));
    }

    public AlmacenResponse obtener(Long sedeId) {
        return almacenRepository.findById(sedeId)
                .map(AlmacenResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Almacén", sedeId));
    }
}
