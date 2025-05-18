package com.dipanshushukla.realtimechatappauthservice.dto;

import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    @NotNull(message = "fullName must not be null")
    @NotBlank(message = "fullName must not be blank")
    @Size(min = 2, message = "fullName must be at least 2 characters long")
    private String fullName;

    @NotNull(message = "username must not be null")
    @NotBlank(message = "username must not be blank")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "username must be 3–32 characters, contain only letters or digits, and only be seperated by underscore (_)")
    private String username;

    @NotNull(message = "password must not be null")
    @NotBlank(message = "password must not be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$", message = "password must be 8–64 characters, contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotNull(message = "email must not be null")
    @NotBlank(message = "email must not be blank")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email address")
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
