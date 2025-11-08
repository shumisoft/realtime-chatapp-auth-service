package com.dipanshushukla.realtimechatappauthservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.exception.UsernameAlreadyExistsException;
import com.dipanshushukla.realtimechatappauthservice.factory.UserDataFactory;
import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;
import com.dipanshushukla.realtimechatappauthservice.service.impl.AuthenticationServiceImpl;
import com.dipanshushukla.realtimechatappauthservice.service.impl.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  // Mock instances of the dependencies
  @Mock
  private UserCredentialRepository repository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtService;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private UserDetailsServiceImpl userDetailsService;
  @Mock
  private UsernameBloomFilterService bloomFilterService;

  // Create real instace of the service and inject the mocks into it
  @InjectMocks
  private AuthenticationServiceImpl authService;

  // Class-level variables for DRY setup
  private User mockUser;
  private UserDTO requestDto;
  private String username;
  private String fakeAccessToken;
  private String fakeRefreshToken;

  @BeforeEach
  void setUp() {
    // Initialize common objects once per test
    mockUser = UserDataFactory.createValidUser();
    requestDto = UserDataFactory.createValidUserDTO();
    username = UserDataFactory.DEFAULT_USERNAME;
    fakeAccessToken = "fake_access_token";
    fakeRefreshToken = "fake_refresh_token";
  }

  @Test
  @DisplayName("register: Should successfully register a new user and return JWTs")
  void register_ShouldReturnJwts_WhenUserIsNew() {

    when(bloomFilterService.exists(requestDto.getUsername())).thenReturn(false);
    when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(UserDataFactory.DEFAULT_ENCODED_PASSWORD);
    when(repository.save(any(User.class))).thenReturn(mockUser);

    when(jwtService.generateAccessToken(mockUser.getUserId(), mockUser.getUsername())).thenReturn(fakeAccessToken);
    when(jwtService.generateRefreshToken(mockUser.getUserId(), mockUser.getUsername())).thenReturn(fakeRefreshToken);

    JwtResponseDTO response = authService.register(requestDto);

    assertNotNull(response, "Response should not be null");
    assertEquals(fakeAccessToken, response.getAccessToken(), "Access token should match mocked output");
    assertEquals(fakeRefreshToken, response.getRefreshToken(), "Refresh token should match mocked output");

    verify(bloomFilterService).add(mockUser.getUsername());
  }

  @Test
  @DisplayName("register: Should throw exception when username already exists in Bloom Filter & DB")
  void register_ShouldThrowException_WhenUsernameExists() {

    when(bloomFilterService.exists(requestDto.getUsername())).thenReturn(true);
    when(repository.existsByUsername(requestDto.getUsername())).thenReturn(true);

    UsernameAlreadyExistsException exception = assertThrows(
        UsernameAlreadyExistsException.class,
        () -> authService.register(requestDto),
        "Expected register() to throw, but it didn't");

    assertEquals("User already exists with username: " + requestDto.getUsername(), exception.getMessage());
    verify(repository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("authenticate: Should return JWTs for valid credentials")
  void authenticate_ShouldReturnJwts_WhenCredentialsAreValid() {

    UserLoginCredentialsDTO loginRequest = new UserLoginCredentialsDTO(username, UserDataFactory.DEFAULT_PASSWORD);

    when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUser);
    when(repository.findByUsername(username)).thenReturn(java.util.Optional.of(mockUser));
    when(jwtService.generateAccessToken(mockUser.getUserId(), mockUser.getUsername())).thenReturn(fakeAccessToken);
    when(jwtService.generateRefreshToken(mockUser.getUserId(), mockUser.getUsername())).thenReturn(fakeRefreshToken);

    JwtResponseDTO response = authService.authenticate(loginRequest);

    assertNotNull(response);
    assertEquals(fakeAccessToken, response.getAccessToken());
    assertEquals(fakeRefreshToken, response.getRefreshToken());
    verify(authenticationManager).authenticate(any());
  }

  @Test
  @DisplayName("authenticate: Should throw BadCredentialsException when user not found in DB")
  void authenticate_ShouldThrowException_WhenUserNotFoundInDb() {

    UserLoginCredentialsDTO loginRequest = new UserLoginCredentialsDTO(username, "password");

    when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUser);
    when(repository.findByUsername(username)).thenReturn(java.util.Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginRequest));
  }

  @Test
  @DisplayName("refreshToken: Should return new access token for valid refresh token")
  void refreshToken_ShouldReturnNewAccessToken_WhenRefreshTokenIsValid() {

    String newAccessToken = "new_fresh_access_token";

    when(jwtService.extractUsername(fakeRefreshToken)).thenReturn(username);
    when(repository.findByUsername(username)).thenReturn(java.util.Optional.of(mockUser));
    when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUser);
    when(jwtService.isValid(fakeRefreshToken, mockUser)).thenReturn(true);
    when(jwtService.generateAccessToken(mockUser.getUserId(), username)).thenReturn(newAccessToken);

    JwtResponseDTO response = authService.refreshToken(fakeRefreshToken);

    assertNotNull(response);
    assertEquals(newAccessToken, response.getAccessToken());
    assertEquals(fakeRefreshToken, response.getRefreshToken(), "Refresh token should remain the same");
  }

  @Test
  @DisplayName("refreshToken: Should throw Exception when refresh token is invalid")
  void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {

    when(jwtService.extractUsername(fakeRefreshToken)).thenReturn(username);
    when(repository.findByUsername(username)).thenReturn(java.util.Optional.of(mockUser));
    when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUser);
    when(jwtService.isValid(fakeRefreshToken, mockUser)).thenReturn(false);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> authService.refreshToken(fakeRefreshToken));

    assertEquals("Invalid refresh token", exception.getMessage());
  }

  @Test
  @DisplayName("refreshToken: Should throw Exception when user not found in DB")
  void refreshToken_ShouldThrowException_WhenUserNotFound() {

    when(jwtService.extractUsername(fakeRefreshToken)).thenReturn(username);
    when(repository.findByUsername(username)).thenReturn(java.util.Optional.empty());

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> authService.refreshToken(fakeRefreshToken));

    assertEquals("Invalid refresh token", exception.getMessage());
  }

  @Test
  @DisplayName("userExistsByUsername: Should return false immediately if not in Bloom Filter")
  void userExistsByUsername_ShouldReturnFalse_WhenNotInBloomFilter() {

    when(bloomFilterService.exists(username)).thenReturn(false);

    var response = authService.userExistsByUsername(username);

    assertFalse(response.isExists());
    verify(repository, never()).existsByUsername(any());
  }

  @Test
  @DisplayName("userExistsByUsername: Should check DB if Bloom Filter returns true")
  void userExistsByUsername_ShouldCheckDB_WhenInBloomFilter() {

    when(bloomFilterService.exists(username)).thenReturn(true);
    when(repository.existsByUsername(username)).thenReturn(true);

    var response = authService.userExistsByUsername(username);

    assertTrue(response.isExists());
    verify(repository).existsByUsername(username);
  }

}
