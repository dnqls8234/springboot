package com.mindshift.ums.api.controller;

import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.service.AuthenticationService;
import com.mindshift.ums.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/templates")
@Tag(name = "Templates", description = "템플릿 관리 및 미리보기 API")
public class TemplateController {

    private final TemplateService templateService;
    private final AuthenticationService authenticationService;

    @Autowired
    public TemplateController(TemplateService templateService,
                             AuthenticationService authenticationService) {
        this.templateService = templateService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/render")
    @Operation(
        summary = "템플릿 렌더링 미리보기",
        description = "실제 발송 전에 템플릿이 어떻게 렌더링되는지 미리 확인할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "템플릿 렌더링 성공",
            content = @Content(schema = @Schema(implementation = TemplateRenderResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 템플릿 또는 데이터"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "템플릿을 찾을 수 없음"
        )
    })
    public ResponseEntity<TemplateRenderResponse> renderTemplate(
            @Valid @RequestBody TemplateRenderRequest request,
            @Parameter(description = "API 인증 토큰")
            @RequestHeader("Authorization") String authorization) {

        var tenant = authenticationService.authenticateTenant(authorization);

        Template template = templateService.loadTemplate(
            tenant, request.getTemplateCode(), request.getChannel(), request.getLocale());

        templateService.validateTemplate(template, request.getTemplateData());

        Map<String, String> rendered = templateService.renderTemplate(template, request.getTemplateData());

        TemplateRenderResponse response = new TemplateRenderResponse();
        response.setTemplateCode(request.getTemplateCode());
        response.setChannel(request.getChannel());
        response.setLocale(request.getLocale());
        response.setRenderedTitle(rendered.get("title"));
        response.setRenderedBody(rendered.get("body"));
        response.setOriginalTitle(template.getTitleTemplate());
        response.setOriginalBody(template.getBodyTemplate());

        return ResponseEntity.ok(response);
    }

    /**
     * 템플릿 렌더링 요청 DTO
     */
    public static class TemplateRenderRequest {
        @Parameter(description = "템플릿 코드", example = "welcome_sms")
        private String templateCode;

        @Parameter(description = "채널 타입", example = "SMS")
        private ChannelType channel;

        @Parameter(description = "로케일", example = "ko")
        private String locale = "ko";

        @Parameter(description = "템플릿 변수 데이터")
        private Map<String, Object> templateData;

        // Getters and setters
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

        public ChannelType getChannel() { return channel; }
        public void setChannel(ChannelType channel) { this.channel = channel; }

        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }

        public Map<String, Object> getTemplateData() { return templateData; }
        public void setTemplateData(Map<String, Object> templateData) { this.templateData = templateData; }
    }

    /**
     * 템플릿 렌더링 응답 DTO
     */
    public static class TemplateRenderResponse {
        @Parameter(description = "템플릿 코드")
        private String templateCode;

        @Parameter(description = "채널 타입")
        private ChannelType channel;

        @Parameter(description = "로케일")
        private String locale;

        @Parameter(description = "렌더링된 제목")
        private String renderedTitle;

        @Parameter(description = "렌더링된 본문")
        private String renderedBody;

        @Parameter(description = "원본 제목 템플릿")
        private String originalTitle;

        @Parameter(description = "원본 본문 템플릿")
        private String originalBody;

        // Getters and setters
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

        public ChannelType getChannel() { return channel; }
        public void setChannel(ChannelType channel) { this.channel = channel; }

        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }

        public String getRenderedTitle() { return renderedTitle; }
        public void setRenderedTitle(String renderedTitle) { this.renderedTitle = renderedTitle; }

        public String getRenderedBody() { return renderedBody; }
        public void setRenderedBody(String renderedBody) { this.renderedBody = renderedBody; }

        public String getOriginalTitle() { return originalTitle; }
        public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

        public String getOriginalBody() { return originalBody; }
        public void setOriginalBody(String originalBody) { this.originalBody = originalBody; }
    }
}