package co.edu.udea.arqsoftware.inventario.web.error;

import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.service.exception.SolicitudInvalidaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String HEADER_VERSION = "X-API-Version";
    private static final String VERSIONES_SOPORTADAS = "1, 2";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail handleConflicto(ConflictoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflicto",
                "La operación viola una restricción de integridad de los datos");
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail handleSolicitudInvalida(SolicitudInvalidaException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropiedadInvalida(PropertyReferenceException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parámetro de ordenamiento inválido",
                "No se puede ordenar por la propiedad '" + ex.getPropertyName() + "'");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'");
    }

    @ExceptionHandler(MissingApiVersionException.class)
    public ProblemDetail handleVersionFaltante(MissingApiVersionException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Versión de API requerida",
                "Debe enviar el header " + HEADER_VERSION + " con una versión soportada: " + VERSIONES_SOPORTADAS);
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ProblemDetail handleVersionInvalida(InvalidApiVersionException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Versión de API no soportada",
                "La versión '" + ex.getVersion() + "' no es soportada. Versiones disponibles: " + VERSIONES_SOPORTADAS);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado procesando la solicitud");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos",
                "La solicitud contiene campos con errores de validación");
        List<Map<String, Object>> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::detalleCampo)
                .toList();
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
                                                                    HttpHeaders headers,
                                                                    HttpStatusCode status,
                                                                    WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problema(HttpStatus.NOT_FOUND, "Recurso no encontrado",
                "No existe el recurso /" + ex.getResourcePath() + " para la versión de API solicitada"));
    }

    private Map<String, Object> detalleCampo(FieldError error) {
        return Map.of(
                "campo", error.getField(),
                "mensaje", String.valueOf(error.getDefaultMessage()),
                "valorRechazado", String.valueOf(error.getRejectedValue()));
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
