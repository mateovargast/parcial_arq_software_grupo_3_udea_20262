package co.edu.udea.arqsoftware.inventario.observability;

import co.edu.udea.arqsoftware.inventario.config.OpenApiConfig;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ApiVersionObservationConvention extends DefaultServerRequestObservationConvention {

    private static final Set<String> VERSIONES_CONOCIDAS = Set.of("1", "2");

    @Override
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        String header = context.getCarrier().getHeader(OpenApiConfig.HEADER_VERSION);
        String version = header == null ? "ninguna"
                : VERSIONES_CONOCIDAS.contains(header.trim()) ? header.trim()
                : "invalida";
        return super.getLowCardinalityKeyValues(context).and(KeyValue.of("api.version", version));
    }
}
