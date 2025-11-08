package com.dipanshushukla.realtimechatappauthservice.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.factory.UserDataFactory;
import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;
import com.dipanshushukla.realtimechatappauthservice.service.UsernameBloomFilterService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag("integration")
class AuthServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserCredentialRepository userRespository;
	@Autowired
	private UsernameBloomFilterService usernameBloomFilterService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	@DisplayName("Full Flow: Registering a user saves to MySQL, updates Redis Bloom Filter, and return JWTs")
	void registerUser_ShouldPersistToDatabaseAndCache() throws Exception {

		// Arrange
		UserDTO dto = UserDataFactory.createValidUserDTO();

		assertFalse(userRespository.existsByUsername(dto.getUsername()), "Database should be clean before test runs");
		assertFalse(usernameBloomFilterService.exists(dto.getUsername()),
				"Bloom filter should be clean before test runs");

		// Act
		mockMvc.perform(post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty());

		// Assert
		assertTrue(userRespository.existsByUsername(dto.getUsername()),
				"User should be successfully persisted to the MySQL database");
		assertTrue(usernameBloomFilterService.exists(dto.getUsername()),
				"Username should be successfully added to the Redis Bloom Filter");

	}

}
