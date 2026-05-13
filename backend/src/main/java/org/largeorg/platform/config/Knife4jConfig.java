package org.largeorg.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("大型组织数字化办公与安全审计一体化平台")
                        .description("面向大型组织数字化办公与安全审计的一体化平台 API 文档")
                        .version("1.0.0"));
    }
}
