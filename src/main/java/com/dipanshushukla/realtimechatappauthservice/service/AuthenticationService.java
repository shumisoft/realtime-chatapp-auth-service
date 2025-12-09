package com.dipanshushukla.realtimechatappauthservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.exception.UsernameAlreadyExistsException;
import com.dipanshushukla.realtimechatappauthservice.repository.UserCredentialRepository;

@Service
public class AuthenticationService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImp userDetailsService;

    public JwtResponseDTO register(UserDTO request) {

        if (userExistsByUsername(request.getUsername()))
            throw new UsernameAlreadyExistsException(
                    "User already exists with username: " + request.getUsername());

        User user = request.toEntity(
                passwordEncoder.encode(request.getPassword()));

        user = repository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getUsername());

        return new JwtResponseDTO(accessToken, refreshToken);
    }

    public JwtResponseDTO authenticate(UserLoginCredentialsDTO request) {

        userDetailsService.loadUserByUsername(request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getUsername());
        return new JwtResponseDTO(accessToken, refreshToken);
    }

    public JwtResponseDTO refreshToken(String refreshToken) {

        String username = jwtService.extractUsername(refreshToken);
        if (username == null)
            throw new IllegalArgumentException("Invalid refresh token");

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!jwtService.isValid(refreshToken, userDetailsService.loadUserByUsername(username)))
            throw new IllegalArgumentException("Invalid refresh token");

        String newAccessToken = jwtService.generateAccessToken(user.getUserId(), user.getUsername());

        return new JwtResponseDTO(newAccessToken, refreshToken);
    }

    public boolean userExistsByUsername(String username) {
        return repository.existsByUsername(username);
    }
}
