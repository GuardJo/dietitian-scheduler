package org.github.guardjo.dientitian.scheduler.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String SERVER_URL = "/";

    /**
     * Swagger UI의 요청 대상 서버 URL을 상대 경로("/")로 고정한다.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server().url(SERVER_URL));
    }
}
