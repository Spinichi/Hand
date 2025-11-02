package com.finger.hand_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정 클래스
 * - Swagger UI에서 API 문서를 자동으로 생성
 * - JWT 인증을 지원하도록 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 이름 정의
        String jwt = "JWT";

        // Swagger UI에서 "Authorize" 버튼 클릭 시 JWT 토큰 입력할 수 있도록 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        // JWT 인증 방식 정의
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)                          // 보안 스키마 이름
                .type(SecurityScheme.Type.HTTP)     // HTTP 인증 방식
                .scheme("bearer")                   // Bearer 토큰 사용
                .bearerFormat("JWT")                // JWT 형식
        );

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .components(components)              // 보안 스키마 컴포넌트 등록
                .info(apiInfo())                     // API 기본 정보 설정
                .addSecurityItem(securityRequirement); // 전역적으로 JWT 인증 적용
    }

    /**
     * API 문서 기본 정보
     */
    private Info apiInfo() {
        return new Info()
                .title("Have A Nice Day API")                        // API 문서 제목
                .description("Mental Care Backend API Documentation") // API 설명
                .version("1.0.0");                                   // API 버전
    }
}
