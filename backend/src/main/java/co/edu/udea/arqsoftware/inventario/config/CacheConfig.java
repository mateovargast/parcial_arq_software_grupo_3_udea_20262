package co.edu.udea.arqsoftware.inventario.config;

import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig {

    public static final String CACHE_INVENTARIO = "inventario";
    public static final String CACHE_RESUMEN = "resumen-inventario";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheCustomizer(JsonMapper jsonMapper,
                                                                   @Value("${app.cache.ttl:10m}") Duration ttl) {
        JavaType tipoPagina = jsonMapper.getTypeFactory()
                .constructParametricType(PageResponse.class, InventarioItemResponse.class);
        JavaType tipoResumen = jsonMapper.constructType(ResumenInventarioResponse.class);

        return builder -> builder
                .withCacheConfiguration(CACHE_INVENTARIO, configuracion(jsonMapper, tipoPagina, ttl))
                .withCacheConfiguration(CACHE_RESUMEN, configuracion(jsonMapper, tipoResumen, ttl));
    }

    private static RedisCacheConfiguration configuracion(JsonMapper jsonMapper, JavaType tipo, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("inventario-api::")
                .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(jsonMapper, tipo)));
    }
}
