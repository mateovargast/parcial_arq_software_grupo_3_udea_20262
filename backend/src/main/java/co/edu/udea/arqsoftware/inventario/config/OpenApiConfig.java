package co.edu.udea.arqsoftware.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String HEADER_VERSION = "X-API-Version";

    @Bean
    public OpenAPI inventarioOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Inventario por Almacén")
                .description("""
                        API RESTful para consultar y registrar el inventario de productos por almacén (sede).

                        * **Versionamiento** por el header `X-API-Version` (obligatorio): versiones `1` y `2`.
                        * Respuestas en **JSON + HAL** (HATEOAS) con enlaces de navegación.
                        * Las consultas de colecciones son **paginadas** (`page`, `size`, `sort`).
                        * Los errores siguen el formato **Problem Details (RFC 9457)**.
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Equipo 3 - Arquitectura de Software UdeA 2026-2"))
                .license(new License().name("Uso académico")));
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return grupo("1");
    }

    @Bean
    public GroupedOpenApi apiV2() {
        return grupo("2");
    }

    private GroupedOpenApi grupo(String version) {
        return GroupedOpenApi.builder()
                .group("v" + version)
                .displayName("API v" + version)
                .pathsToMatch("/api/**")
                .addOpenApiMethodFilter(metodo -> aplicaAVersion(metodo, version))
                .addOperationCustomizer((operacion, handlerMethod) -> {
                    List<Parameter> parametros = operacion.getParameters();
                    if (parametros != null) {
                        parametros.removeIf(p -> HEADER_VERSION.equalsIgnoreCase(p.getName()));
                    }
                    return operacion.addParametersItem(headerVersion(version));
                })
                .build();
    }

    private static boolean aplicaAVersion(Method metodo, String version) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class);
        return mapping == null || mapping.version().isEmpty() || mapping.version().equals(version);
    }

    private static Parameter headerVersion(String version) {
        StringSchema schema = new StringSchema();
        schema.setEnum(List.of(version));
        schema.setDefault(version);
        return new HeaderParameter()
                .name(HEADER_VERSION)
                .required(true)
                .description("Versión de la API (versionamiento por Custom Request Header)")
                .schema(schema);
    }
}
