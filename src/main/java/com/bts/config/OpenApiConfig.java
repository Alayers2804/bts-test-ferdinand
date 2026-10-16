
package com.bts.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "BTS Product API",
        version = "1.0.0",
        description = "REST API for user authentication and product management"
    )
)
public class OpenApiConfig {
}
