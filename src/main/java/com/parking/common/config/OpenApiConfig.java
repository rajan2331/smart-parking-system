package com.parking.common.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * Log in via POST /api/v1/auth/login, click "Authorize" and paste the accessToken.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Smart Parking API",
                version = "v1",
                description = "Register, log in, manage vehicles and parking sessions. "
                        + "Admin-only: creating parking spots and changing rates."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
