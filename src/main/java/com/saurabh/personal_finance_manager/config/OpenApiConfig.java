package com.saurabh.personal_finance_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI personalFinanceOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Personal Finance Manager API")
                .description("REST API documentation for Personal Finance Manager application.")
                .version("1.0.0"))
        .servers(
            List.of(
                new Server()
                    .url("https://personal-finance-manager-hbrz.onrender.com")
                    .description("Live Render Deployment"),
                new Server().url("http://localhost:8080").description("Local Development")));
  }
}
