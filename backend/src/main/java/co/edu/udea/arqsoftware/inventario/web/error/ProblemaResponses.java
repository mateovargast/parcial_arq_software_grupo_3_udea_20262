package co.edu.udea.arqsoftware.inventario.web.error;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ProblemDetail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ProblemaResponses {

    private static final String PROBLEM_JSON = "application/problem+json";

    private ProblemaResponses() {
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(responseCode = "400",
            description = "Solicitud inválida: falta el header X-API-Version, versión no soportada, datos o parámetros inválidos",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public @interface BadRequest {
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public @interface NotFound {
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual de los datos",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public @interface Conflict {
    }
}
