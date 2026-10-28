package br.com.schedule.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Schedule Patrol API")
                        .description("API REST de agendamento de rondas de inspeção")
                        .version("0.0.1-SNAPSHOT"));
    }
}
