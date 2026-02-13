package com.dipanshushukla.realtimechatappauthservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonAppConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonConfig redissonConfig) {
        Config config = new Config();

        config.useSingleServer()
                .setAddress(redissonConfig.getRedisUrl())
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(50);

        return Redisson.create(config);
    }
}