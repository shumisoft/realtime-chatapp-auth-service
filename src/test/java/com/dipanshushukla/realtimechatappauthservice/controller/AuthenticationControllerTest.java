package com.dipanshushukla.realtimechatappauthservice.controller;

import static com.dipanshushukla.realtimechatappauthservice.factory.UserDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UsernameExistsResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.factory.UserDataFactory;
import com.dipanshushukla.realtimechatappauthservice.service.AuthenticationService;
import com.dipanshushukla.realtimechatappauthservice.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private AuthenticationService authService;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthenticationController controller;

	private JwtResponseDTO mockJwtResponseDTO;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(controller)
				.build();

		mockJwtResponseDTO = createValidRefreshTokenResponseDTO();
	}

	@Test
	@DisplayName("Slice Test: POST /register - Should reutrn 201 Created and JWTs")
	void register_ShouldReturnTokens_WhenValidRequest() throws Exception {

		// Arrange
		UserDTO request = createValidUserDTO();
		when(authService.register(any(UserDTO.class))).thenReturn(mockJwtResponseDTO);

		// Act & Assert
		mockMvc.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").value(MOCK_ACCESS_TOKEN))
				.andExpect(jsonPath("$.refreshToken").value(MOCK_REFRESH_TOKEN));

	}

	@Test
	@DisplayName("Slice Test: POST /register - Should return 400 Bad Request on validation failure")
	void register_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
		// Arrange
		UserDTO invalidDto = createInvalidUserDTO();

		// Act & Assert
		mockMvc.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		// Prove that the service was never called because the Controller blocked it!
		verifyNoInteractions(authService);

	}

	@Test
	@DisplayName("Slice Test: POST /login - Should reutrn 200 OK and JWTs")
	void login_ShouldReturnTokens_WhenValidCredentials() throws Exception {

		// Arrange
		UserLoginCredentialsDTO credentials = createValidLoginDTO();
		when(authService.authenticate(any(UserLoginCredentialsDTO.class))).thenReturn(mockJwtResponseDTO);

		// Act & Assert
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(credentials)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(MOCK_ACCESS_TOKEN))
				.andExpect(jsonPath("$.refreshToken").value(MOCK_REFRESH_TOKEN));

	}

	@Test
	@DisplayName("Slice Test: POST /login - Should return 400 Bad Request on missing credentials")
	void login_ShouldReturn400_WhenCredentialsAreInvalid() throws Exception {

		UserLoginCredentialsDTO invalidCredentials = createInvalidLoginDTO();

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidCredentials)))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);

	}

	@Test
	@DisplayName("Slice Test: POST /refresh-token - Should return 200 OK and new JWTs")
	void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() throws Exception {

		// Arrange
		String requestJson = "{\"refreshToken\": \"old-refresh-token\"}";
		when(authService.refreshToken(anyString())).thenReturn(mockJwtResponseDTO);

		// Act & Assert
		mockMvc.perform(post("/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(MOCK_ACCESS_TOKEN))
				.andExpect(jsonPath("$.refreshToken").value(MOCK_REFRESH_TOKEN));
	}

	@Test
	@DisplayName("Slice Test: GET /.well-known/jwks.json - Should return 200 OK and JWKS payload")
	void getJwks_ShouldReturnJwksPayload() throws Exception {

		// Arrange
		Map<String, String> mockJwksMap = Map.of("keys", MOCK_KEY_DATA);
		when(jwtService.getJwks()).thenReturn(mockJwksMap);

		// Act & Assert
		mockMvc.perform(get("/.well-known/jwks.json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.keys").value(MOCK_KEY_DATA));
	}

	@Test
	@DisplayName("Slice Test: GET /exists - Should return 200 OK when checking username")
	void existByUsername_ShouldReturn200OK() throws Exception {

		String targetUsername = DEFAULT_USERNAME;
		UsernameExistsResponseDTO mockResponse = UserDataFactory.createUsernameExistsResponse(true);

		when(authService.userExistsByUsername(targetUsername)).thenReturn(mockResponse);

		mockMvc.perform(get("/exists")
				.param("username", targetUsername))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Slice Test: GET /exists - Should return 400 Bad Request when username param is missing")
	void existByUsername_ShouldReturn400_WhenParamIsMissing() throws Exception {

		mockMvc.perform(get("/exists"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(authService);

	}

}
