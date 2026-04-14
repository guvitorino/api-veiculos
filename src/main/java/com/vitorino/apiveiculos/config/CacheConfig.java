package com.vitorino.apiveiculos.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration DOLLAR_RATE_TTL = Duration.ofMinutes(10);
    private static final Duration VEHICLE_CACHE_TTL = Duration.ofMinutes(10);

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericToStringSerializer<BigDecimal> bigDecimalSerializer = new GenericToStringSerializer<>(BigDecimal.class);
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();

        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer));

        RedisCacheConfiguration dollarRateConfiguration = defaultConfiguration
                .entryTtl(DOLLAR_RATE_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(bigDecimalSerializer));

        RedisCacheConfiguration vehicleConfiguration = defaultConfiguration
                .entryTtl(VEHICLE_CACHE_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(Map.of(
                        "dollarRate", dollarRateConfiguration,
                        "vehicleById", vehicleConfiguration,
                        "vehicleList", vehicleConfiguration,
                        "vehicleReportByBrand", vehicleConfiguration
                ))
                .transactionAware()
                .build();
    }
}
