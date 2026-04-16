package com.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(title = "Chat-backend API", version = "v1"),
        servers = {
                @Server(
                        description = "http local",
                        url = "http://localhost:8083/chat")
        }
)

public class OpenAPI30Config {
}
