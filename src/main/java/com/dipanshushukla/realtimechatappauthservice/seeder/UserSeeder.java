package com.dipanshushukla.realtimechatappauthservice.seeder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.model.Role;
import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;
import com.dipanshushukla.realtimechatappauthservice.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSeeder implements ApplicationRunner {

  private final UserCredentialRepository repository;
  private final AuthenticationService service;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    log.info("[Seeder] User seeder started!");

    if (repository.count() > 0) {

      log.info("[Seeder] Users already exist. Skipping seeding!");
      log.info("[Seeder] User seeder finished!");
      return;

    }

    for (int i = 1; i <= 10; i++) {
      UserDTO dto = UserDTO.builder()
          .fullName("Test User " + i)
          .username("user" + i)
          .email("user" + i + "@example.com")
          .password("Test" + i + "@123")
          .bio("Hey there! I'm user # " + i + ".")
          .role(Role.USER)
          .build();

      service.register(dto);

      log.info("[Seeder] ✔ Created user: {}", dto.getUsername());

    }

    log.info("[Seeder] User seeder finished!");

  }

}
