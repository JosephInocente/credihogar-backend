package com.abarrotes.sistema.auth.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
}
