package com.dipanshushukla.realtimechatappauthservice.dto;

import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    @NotNull
    @NotBlank
    private String fullName;

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String password;

    @NotNull
    @NotBlank
    private String email;

    private Role role;

    /** Convert Entity → DTO */
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .fullName(user.getFullName())
                .username(user.getUsername())
                .password(null) // do NOT expose password
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /** Convert DTO → Entity */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .fullName(fullName)
                .username(username)
                .password(encodedPassword)
                .email(email)
                .role(role != null ? role : Role.USER)
                .build();
    }
}
