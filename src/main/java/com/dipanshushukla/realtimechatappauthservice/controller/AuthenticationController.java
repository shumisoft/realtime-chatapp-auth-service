package com.dipanshushukla.realtimechatappauthservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UsernameExistsResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.service.AuthenticationService;
import com.dipanshushukla.realtimechatappauthservice.service.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody UserDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody UserLoginCredentialsDTO request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Object> getJwks() {
        return ResponseEntity.ok(jwtService.getJwks());

    }

    @GetMapping("/exists")
    public ResponseEntity<UsernameExistsResponseDTO> existByUsername(
            @RequestParam @NotBlank(message = "Username must not be empty or null") String username) {
        return ResponseEntity.ok(authService.userExistsByUsername(username));
    }

}
