package pizza.kkomdae.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)  // HTTP 타입 인증 방식
                .in(SecurityScheme.In.HEADER)    // 헤더에 인증 정보 포함
                .name("Authorization")           // 인증 헤더 이름
                .scheme("bearer")                // 인증 스키마는 bearer
                .bearerFormat("JWT");            // 토큰 형식은 JWT

        // 보안 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");        // 보안 요구사항의 이름

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer Token", securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("API 문서")
                        .description("JWT 인증이 필요한 API 문서입니다.")
                        .version("v1.0.0"));
    }
}
