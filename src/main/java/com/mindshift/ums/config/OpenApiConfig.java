package com.mindshift.ums.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for UMS Service documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI umsOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(serverList())
            .tags(tagList())
            .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("ApiKeyAuth", apiKeySecurityScheme())
                .addSecuritySchemes("HmacAuth", hmacSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
            .title("UMS (Unified Messaging Service) API")
            .description("""
                통합 메시징 서비스 API 문서

                ## 개요
                UMS는 여러 채널(Kakao Alimtalk, SMS, Email, FCM Push)을 통한
                통합 메시징 서비스를 제공합니다.

                ## 주요 기능
                - 📱 **다중 채널 지원**: Kakao, SMS, Email, FCM Push
                - 🔄 **비동기 처리**: 202 Accepted 패턴으로 즉시 응답
                - 🛡️ **보안**: HMAC-SHA256 인증 및 Rate Limiting
                - 🎯 **정책 관리**: 수신자 옵트아웃, 무음시간, 빈도 제한
                - 📊 **모니터링**: Health Check, Metrics, 상태 추적

                ## 인증 방식
                1. **API Key**: Bearer 토큰 방식 (개발/테스트용)
                2. **HMAC-SHA256**: 요청 서명 기반 (운영 권장)

                ## 메시지 발송 흐름
                1. POST /v1/messages → 202 Accepted (requestId 반환)
                2. 백그라운드에서 비동기 처리 (템플릿 렌더링, 정책 확인)
                3. 채널별 어댑터를 통해 실제 발송
                4. GET /v1/messages/{requestId} → 상태 확인
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("MindShift Development Team")
                .email("dev@mindshift.com")
                .url("https://mindshift.com"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> serverList() {
        return List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server"),
            new Server()
                .url("https://api-dev.mindshift.com")
                .description("Development Server"),
            new Server()
                .url("https://api.mindshift.com")
                .description("Production Server")
        );
    }

    private List<Tag> tagList() {
        return List.of(
            new Tag()
                .name("Messages")
                .description("메시지 발송 및 상태 관리 API"),
            new Tag()
                .name("Templates")
                .description("메시지 템플릿 관리 API"),
            new Tag()
                .name("Tenants")
                .description("테넌트 관리 API"),
            new Tag()
                .name("Monitoring")
                .description("시스템 모니터링 및 Health Check API")
        );
    }

    private SecurityScheme apiKeySecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("API Key")
            .description("""
                API Key 인증 방식

                Header: `Authorization: Bearer {your-api-key}`

                개발 및 테스트 환경에서 사용하는 간단한 인증 방식입니다.
                """);
    }

    private SecurityScheme hmacSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-Signature")
            .description("""
                HMAC-SHA256 서명 인증 방식 (운영 환경 권장)

                필요한 헤더들:
                - `Authorization: ApiKey {your-api-key}`
                - `X-Timestamp: {unix-timestamp}`
                - `X-Signature: {hmac-sha256-signature}`

                서명 생성 방법:
                1. 요청 문자열 생성: `{METHOD}|{URI}|{BODY}|{TIMESTAMP}`
                2. HMAC-SHA256으로 서명: `HMAC-SHA256(request-string, api-secret)`
                3. 결과를 hex 인코딩하여 X-Signature 헤더에 포함

                예시:
                ```
                POST|/v1/messages|{"channel":"SMS","to":{"phone":"+821012345678"}}|1635724800
                ```
                """);
    }
}