package co.edu.udea.arqsoftware.inventario.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiVersionFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-API-Version";
    public static final String SUPPORTED_VERSION = "1";

    private final JsonMapper jsonMapper;

    public ApiVersionFilter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String apiRoot = request.getContextPath() + "/api";
        if (!isApiEndpoint(path, apiRoot) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String version = request.getHeader(HEADER_NAME);
        boolean missing = version == null || version.isBlank();
        boolean compatibilityEndpoint = isCompatibilityEndpoint(path, apiRoot);
        if ((compatibilityEndpoint && !SUPPORTED_VERSION.equals(version))
                || (!compatibilityEndpoint && missing)) {
            HttpStatus status = missing ? HttpStatus.BAD_REQUEST : HttpStatus.NOT_ACCEPTABLE;
            String detail = missing
                    ? "El header X-API-Version es obligatorio."
                    : "La versión solicitada no está soportada. Versión disponible: 1.";
            String title = status.getReasonPhrase();
            if (missing && !compatibilityEndpoint) {
                title = "Versión de API requerida";
                detail = "Debe enviar el header X-API-Version con una versión soportada: 1, 2";
            }
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
            problem.setTitle(title);
            problem.setInstance(URI.create(path));
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            jsonMapper.writeValue(response.getOutputStream(), problem);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isApiEndpoint(String path, String apiRoot) {
        return path.equals(apiRoot) || path.startsWith(apiRoot + "/");
    }

    private static boolean isCompatibilityEndpoint(String path, String apiRoot) {
        return isInApiFamily(path, apiRoot, "dashboard")
                || isInApiFamily(path, apiRoot, "inventory")
                || isInApiFamily(path, apiRoot, "products")
                || isInApiFamily(path, apiRoot, "warehouses");
    }

    private static boolean isInApiFamily(String path, String apiRoot, String family) {
        String prefix = apiRoot + "/" + family;
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
