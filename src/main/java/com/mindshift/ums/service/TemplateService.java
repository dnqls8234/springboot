package com.mindshift.ums.service;

import com.mindshift.ums.api.exception.InvalidTemplateException;
import com.mindshift.ums.api.exception.TemplateNotFoundException;
import com.mindshift.ums.api.exception.UmsException;
import com.mindshift.ums.api.exception.ValidationException;
import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.repository.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for template management and rendering.
 */
@Service
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateRepository templateRepository;
    private final MustacheFactory mustacheFactory;

    // Pattern to find Mustache variables: {{variable}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        this.mustacheFactory = new DefaultMustacheFactory();
    }

    /**
     * Load template by code, tenant, channel and locale.
     *
     * @param tenant Tenant config
     * @param templateCode Template code
     * @param channel Message channel
     * @param locale Locale (optional)
     * @return Template entity
     * @throws TemplateNotFoundException if template not found
     */
    @Cacheable(value = "templates", key = "#tenant.tenantId + ':' + #templateCode + ':' + #channel + ':' + #locale")
    public Template loadTemplate(TenantConfig tenant, String templateCode, ChannelType channel, String locale) {
        logger.debug("Loading template: {} for tenant: {}, channel: {}, locale: {}",
            templateCode, tenant.getTenantId(), channel, locale);

        Template template = templateRepository
            .findByTenantIdAndCodeAndChannelAndLocale(tenant.getTenantId(), templateCode, channel, locale)
            .orElseGet(() -> {
                // Fallback to default locale if specific locale not found
                logger.debug("Template not found for locale {}, trying default locale", locale);
                return templateRepository
                    .findByTenantIdAndCodeAndChannelAndLocale(tenant.getTenantId(), templateCode, channel, "en")
                    .orElse(null);
            });

        if (template == null) {
            throw new TemplateNotFoundException(tenant.getTenantId(), templateCode, channel, locale);
        }

        if (!template.isActive()) {
            throw new InvalidTemplateException("Template is not active: " + templateCode);
        }

        logger.debug("Template loaded successfully: {}", template.getId());
        return template;
    }

    /**
     * Validate template variables against provided data.
     *
     * @param template Template to validate
     * @param templateData Data to validate against
     * @throws ValidationException if required variables are missing
     */
    public void validateTemplate(Template template, Map<String, Object> templateData) {
        logger.debug("Validating template: {}", template.getCode());

        Set<String> requiredVariables = extractVariables(template.getTitleTemplate());
        requiredVariables.addAll(extractVariables(template.getBodyTemplate()));

        if (templateData == null) {
            templateData = new HashMap<>();
        }

        Map<String, String> missingVariables = new HashMap<>();
        for (String variable : requiredVariables) {
            if (!templateData.containsKey(variable)) {
                missingVariables.put(variable, "Required template variable is missing");
            }
        }

        if (!missingVariables.isEmpty()) {
            logger.warn("Template validation failed for {}: missing variables {}",
                template.getCode(), missingVariables.keySet());
            throw new ValidationException("Missing required template variables", Map.of("missingVariables", missingVariables));
        }

        logger.debug("Template validation passed for: {}", template.getCode());
    }

    /**
     * Render template with provided data.
     *
     * @param template Template to render
     * @param templateData Data for rendering
     * @return Map containing rendered title and body
     * @throws InvalidTemplateException if rendering fails
     */
    public Map<String, String> renderTemplate(Template template, Map<String, Object> templateData) {
        logger.debug("Rendering template: {}", template.getCode());

        try {
            Map<String, String> rendered = new HashMap<>();

            // Render title if present
            if (template.getTitleTemplate() != null && !template.getTitleTemplate().trim().isEmpty()) {
                String renderedTitle = renderMustacheTemplate(template.getTitleTemplate(), templateData);
                rendered.put("title", renderedTitle);
            }

            // Render body
            String renderedBody = renderMustacheTemplate(template.getBodyTemplate(), templateData);
            rendered.put("body", renderedBody);

            logger.debug("Template rendered successfully: {}", template.getCode());
            return rendered;

        } catch (Exception e) {
            logger.error("Failed to render template: {}", template.getCode(), e);
            throw new InvalidTemplateException("Failed to render template: " + e.getMessage(), e);
        }
    }

    /**
     * Extract variables from template content.
     *
     * @param templateContent Template content
     * @return Set of variable names
     */
    private Set<String> extractVariables(String templateContent) {
        Set<String> variables = new java.util.HashSet<>();

        if (templateContent != null) {
            Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
            while (matcher.find()) {
                variables.add(matcher.group(1));
            }
        }

        return variables;
    }

    /**
     * Render Mustache template with data.
     *
     * @param templateContent Template content
     * @param data Template data
     * @return Rendered content
     */
    private String renderMustacheTemplate(String templateContent, Map<String, Object> data) {
        if (data == null) {
            data = new HashMap<>();
        }

        Mustache mustache = mustacheFactory.compile(new StringReader(templateContent), "template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, data);
        return writer.toString();
    }
}