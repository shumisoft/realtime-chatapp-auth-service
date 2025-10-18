package com.dipanshushukla.realtimechatappauthservice.seeder;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.model.Role;
import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;
import com.dipanshushukla.realtimechatappauthservice.service.AuthenticationService;
import com.dipanshushukla.realtimechatappauthservice.service.UsernameBloomFilterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSeeder implements ApplicationRunner {

  private final UserCredentialRepository repository;
  private final AuthenticationService service;
  private final UsernameBloomFilterService bloomFilterService;

  private static final List<UserDTO> DEMO_USERS = List.of(
      UserDTO.builder()
          .fullName("Demo User 1")
          .username("demo-user-1")
          .email("demo.user1@example.com")
          .password("DemoUser1@123")
          .bio("Hey there! I'm Demo User 1.")
          .role(Role.USER)
          .build(),

      UserDTO.builder()
          .fullName("Demo User 2")
          .username("demo-user-2")
          .email("demo.user2@example.com")
          .password("DemoUser2@123")
          .bio("Hey there! I'm Demo User 2.")
          .role(Role.USER)
          .build());

  @Override
  public void run(ApplicationArguments args) throws Exception {

    log.info("[Seeder] User seeder started!");

    seedSampleUsers();
    seedDemoUsers();

    log.info("[Seeder] User seeder finished!");

  }

  private void seedSampleUsers() {

    if (repository.count() > 0) {

      log.info("[Seeder] Sample users already exist. Skipping.");

      return;

    }

    bloomFilterService.resetAndRehydrate();

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

  }

  private void seedDemoUsers() {

    for (UserDTO demo : DEMO_USERS) {

      if (bloomFilterService.exists(demo.getUsername()) && repository.existsByUsername(demo.getUsername())) {
        log.info("[Seeder] Demo user '{}' already exists. Skipping.", demo.getUsername());
        continue;
      }

      service.register(demo);

      log.info("[Seeder] ✔ Created demo user: {}", demo.getUsername());

    }

  }

}
