package com.dipanshushukla.realtimechatappauthservice.service;

import com.dipanshushukla.realtimechatappauthservice.dto.JwtResponseDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UserLoginCredentialsDTO;
import com.dipanshushukla.realtimechatappauthservice.dto.UsernameExistsResponseDTO;

public interface AuthenticationService {

  JwtResponseDTO register(UserDTO request);

  JwtResponseDTO authenticate(UserLoginCredentialsDTO request);

  JwtResponseDTO refreshToken(String refreshToken);

  UsernameExistsResponseDTO userExistsByUsername(String username);

}