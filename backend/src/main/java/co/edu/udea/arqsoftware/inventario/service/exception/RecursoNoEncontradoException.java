package co.edu.udea.arqsoftware.inventario.service.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("%s con id %s no existe".formatted(recurso, id));
    }
}
