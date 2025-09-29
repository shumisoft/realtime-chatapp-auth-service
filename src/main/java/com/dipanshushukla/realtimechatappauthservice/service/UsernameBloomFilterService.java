package com.dipanshushukla.realtimechatappauthservice.service;

public interface UsernameBloomFilterService {

  void init();

  void resetAndRehydrate();

  boolean exists(String username);

  boolean add(String username);

}