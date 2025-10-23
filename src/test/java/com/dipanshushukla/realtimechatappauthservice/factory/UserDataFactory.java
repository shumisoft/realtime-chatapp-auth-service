package com.dipanshushukla.realtimechatappauthservice.factory;

import java.util.UUID;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UsernameExistsResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.model.Role;

public class UserDataFactory {

  public static final UUID DEFAULT_USER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  public static final String DEFAULT_USERNAME = "test_user";
  public static final String DEFAULT_PASSWORD = "StrongPassword@123";
  public static final String DEFAULT_ENCODED_PASSWORD = "encoded_password_hash";
  public static final String DEFAULT_EMAIL = "test@example.com";
  public static final String MOCK_ACCESS_TOKEN = "mock-access-token";
  public static final String MOCK_REFRESH_TOKEN = "mock-refresh-token";
  public static final String MOCK_KEY_DATA = "mock-key-data";

  public static User createValidUser() {

    return User.builder()
        .userId(DEFAULT_USER_UUID)
        .fullName("Test User")
        .username(DEFAULT_USERNAME)
        .password(DEFAULT_ENCODED_PASSWORD)
        .email(DEFAULT_EMAIL)
        .bio("I wrote this test!")
        .role(Role.USER)
        .build();

  }

  public static UserDTO createValidUserDTO() {

    return UserDTO.builder()
        .fullName("Test SDE User")
        .username(DEFAULT_USERNAME)
        .password(DEFAULT_PASSWORD)
        .email(DEFAULT_EMAIL)
        .bio("I write scalable distributed systems.")
        .role(Role.USER)
        .build();

  }

  public static UserDTO createInvalidUserDTO() {
    return UserDTO.builder()
        .fullName("")
        .username("")
        .password("")
        .email("invalid-email")
        .build();
  }

  public static UserLoginCredentialsDTO createValidLoginDTO() {
    return new UserLoginCredentialsDTO(DEFAULT_USERNAME, DEFAULT_PASSWORD);
  }

  public static JwtResponseDTO createValidRefreshTokenResponseDTO() {
    return new JwtResponseDTO(MOCK_ACCESS_TOKEN, MOCK_REFRESH_TOKEN);
  }

  public static UsernameExistsResponseDTO createUsernameExistsResponse(boolean exists) {
    return UsernameExistsResponseDTO.builder().exists(exists).build();
  }

  public static UserLoginCredentialsDTO createInvalidLoginDTO() {
    return new UserLoginCredentialsDTO("", "");
  }

}
