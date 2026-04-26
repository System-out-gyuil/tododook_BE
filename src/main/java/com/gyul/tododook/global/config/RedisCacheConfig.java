package com.gyul.tododook.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis 캐시 설정 클래스
 * - Configuration 어노테이션을 통해 스프링 컨텍스트에 등록된다.
 * - @EnableCaching 을 통해 @Cacheable, @CacheEvict, @CachePut 등 캐시 어노테이션을 활성화한다.
 * - CacheManager 빈을 등록하여 캐시 직렬화 방식과 TTL(Time-To-Live)을 관리한다.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Redis 기반의 CacheManager 빈을 생성하여 등록한다.
     *
     * @param connectionFactory Spring Data Redis가 자동 구성한 Redis 연결 팩토리
     * @return 설정이 적용된 RedisCacheManager 인스턴스
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Jackson ObjectMapper에 기본 타입 정보 포함 설정
        // NON_FINAL 옵션을 사용하면 JSON에 "@class" 필드가 추가되어
        // 역직렬화 시 원래 객체 타입으로 정확하게 복원할 수 있다.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, // 모든 서브타입을 허용하는 기본 validator
                ObjectMapper.DefaultTyping.NON_FINAL   // final이 아닌 모든 타입에 타입 정보 포함
        );

        // 위에서 설정한 ObjectMapper를 사용하는 JSON 직렬화기
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 모든 캐시에 공통으로 적용되는 기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 캐시 키를 일반 문자열(String)로 직렬화
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 캐시 값을 JSON 형태로 직렬화 (타입 정보 포함)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                // null 값은 캐시에 저장하지 않음 (불필요한 캐시 공간 낭비 및 오류 방지)
                .disableCachingNullValues();

        // 캐시 이름별로 TTL을 개별 지정
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                // 카테고리 목록은 변경 빈도가 낮으므로 기본값보다 긴 10분으로 설정
                "categories", defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(connectionFactory)
                // 별도 설정이 없는 캐시의 기본 TTL을 5분으로 지정
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(5)))
                // 위에서 정의한 캐시별 개별 설정 적용
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
