package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;

import java.time.Instant;

public record AlmacenResponse(
        Long id,
        String codigoSede,
        String nombre,
        String ciudad,
        String direccion,
        String telefono,
        boolean activo,
        Instant createdAt
) {

    public static AlmacenResponse from(Almacen almacen) {
        return new AlmacenResponse(
                almacen.getId(),
                almacen.getCodigoSede(),
                almacen.getNombre(),
                almacen.getCiudad(),
                almacen.getDireccion(),
                almacen.getTelefono(),
                almacen.isActivo(),
                almacen.getCreatedAt());
    }
}
