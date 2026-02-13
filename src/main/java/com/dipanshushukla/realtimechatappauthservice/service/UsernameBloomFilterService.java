package com.dipanshushukla.realtimechatappauthservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsernameBloomFilterService {

    private final UserCredentialRepository repository;

    private final RedissonClient redissonClient;
    private RBloomFilter<String> usernameBloomFilter;

    private static final String FILTER_NAME = "realtime-chatapp-username-bloom-filter";
    private static final long EXPECTED_INSERTIONS = 1_000_000L; // Expected user base
    private static final double FALSE_PROBABILITY = 0.01; // 1% false positive rate

    @PostConstruct
    public void init() {
        this.usernameBloomFilter = redissonClient.getBloomFilter(FILTER_NAME);

        boolean isNew = usernameBloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);

        if (isNew) {
            log.info("Bloom Filter created new.\nHydrating usernames from the DB.");
            List<String> usernames = repository.findAllUsernames();
            for (String username : usernames) {
                add(username);
            }
            log.info("Hydration completed successfully!");
        }
    }

    public boolean exists(String username) {
        return usernameBloomFilter.contains(username);
    }

    public boolean add(String username) {
        return usernameBloomFilter.add(username);
    }
}