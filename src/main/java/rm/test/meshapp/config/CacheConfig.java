package rm.test.meshapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USER_CACHE = "user";
    public static final String USERS_SEARCH_CACHE = "usersSearch";

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration userCache = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .computePrefixWith(cacheName -> "user:")
                .disableCachingNullValues();

        RedisCacheConfiguration searchCache = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(45))
                .computePrefixWith(cacheName -> "users:search:")
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(60))
                        .disableCachingNullValues())
                .withInitialCacheConfigurations(Map.of(
                        USER_CACHE, userCache,
                        USERS_SEARCH_CACHE, searchCache
                ))
                .transactionAware()
                .build();
    }
}
