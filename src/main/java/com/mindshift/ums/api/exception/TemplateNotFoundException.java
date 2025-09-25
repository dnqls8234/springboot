package com.mindshift.ums.api.exception;

import com.mindshift.ums.domain.enums.ChannelType;
import org.springframework.http.HttpStatus;

public class TemplateNotFoundException extends UmsException {

    public TemplateNotFoundException(String tenantId, String templateCode, ChannelType channel, String locale) {
        super(String.format("Template not found: %s for tenant: %s, channel: %s, locale: %s",
            templateCode, tenantId, channel, locale), "TEMPLATE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public TemplateNotFoundException(String message) {
        super(message, "TEMPLATE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}