package com.mindshift.ums.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.api.exception.MessageNotFoundException;
import com.mindshift.ums.api.exception.RateLimitExceededException;
import com.mindshift.ums.api.exception.ValidationException;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.MessagePriority;
import com.mindshift.ums.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private SendMessageDto.SendMessageRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new SendMessageDto.SendMessageRequest();
        testRequest.setChannel(ChannelType.SMS);
        testRequest.setTemplateCode("welcome_sms");
        testRequest.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        testRequest.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        testRequest.setRouting(routing);

        testRequest.setTemplateData(Map.of("username", "김철수"));
    }

    @Test
    void sendMessage_Success() throws Exception {
        // Given
        String requestId = "req_123456789";
        when(messageService.acceptMessage(any(), anyString(), anyString()))
            .thenReturn(requestId);

        // When & Then
        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "test-idempotency-key")
                .header("Authorization", "Bearer test-api-key")
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(messageService).acceptMessage(any(), eq("test-idempotency-key"), eq("Bearer test-api-key"));
    }

    @Test
    void sendMessage_ValidationError() throws Exception {
        // Given
        when(messageService.acceptMessage(any(), anyString(), anyString()))
            .thenThrow(new ValidationException("Validation failed"));

        // When & Then
        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "test-idempotency-key")
                .header("Authorization", "Bearer test-api-key")
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(messageService).acceptMessage(any(), anyString(), anyString());
    }

    @Test
    void sendMessage_RateLimitExceeded() throws Exception {
        // Given
        when(messageService.acceptMessage(any(), anyString(), anyString()))
            .thenThrow(new RateLimitExceededException("Rate limit exceeded", 0, 3600));

        // When & Then
        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "test-idempotency-key")
                .header("Authorization", "Bearer test-api-key")
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpected(status().isTooManyRequests())
                .andExpected(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpected(header().string("X-RateLimit-Remaining", "0"))
                .andExpected(header().string("X-RateLimit-Reset", "3600"));

        verify(messageService).acceptMessage(any(), anyString(), anyString());
    }

    @Test
    void sendMessage_MissingIdempotencyKey() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-api-key")
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpected(status().isBadRequest());

        verify(messageService, never()).acceptMessage(any(), anyString(), anyString());
    }

    @Test
    void getMessageStatus_Success() throws Exception {
        // Given
        String requestId = "req_123456789";
        SendMessageDto.MessageStatusResponse response = new SendMessageDto.MessageStatusResponse();
        response.setRequestId(requestId);
        response.setStatus("PENDING");
        response.setChannel(ChannelType.SMS);

        when(messageService.getMessageStatus(requestId))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/v1/messages/{requestId}", requestId))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.requestId").value(requestId))
                .andExpected(jsonPath("$.status").value("PENDING"))
                .andExpected(jsonPath("$.channel").value("SMS"));

        verify(messageService).getMessageStatus(requestId);
    }

    @Test
    void getMessageStatus_NotFound() throws Exception {
        // Given
        String requestId = "nonexistent";
        when(messageService.getMessageStatus(requestId))
            .thenThrow(new MessageNotFoundException(requestId));

        // When & Then
        mockMvc.perform(get("/v1/messages/{requestId}", requestId))
                .andExpected(status().isNotFound())
                .andExpected(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));

        verify(messageService).getMessageStatus(requestId);
    }

    @Test
    void listMessages_Success() throws Exception {
        // Given
        Map<String, Object> response = Map.of(
            "messages", java.util.List.of(),
            "pagination", Map.of(
                "page", 0,
                "size", 20,
                "totalElements", 0L,
                "totalPages", 0,
                "hasNext", false,
                "hasPrevious", false
            )
        );

        when(messageService.listMessages(anyString(), anyInt(), anyInt(), any()))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/v1/messages")
                .header("Authorization", "Bearer test-api-key")
                .param("page", "0")
                .param("size", "20"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.messages").exists())
                .andExpected(jsonPath("$.pagination").exists())
                .andExpected(jsonPath("$.pagination.page").value(0))
                .andExpected(jsonPath("$.pagination.size").value(20));

        verify(messageService).listMessages("Bearer test-api-key", 0, 20, null);
    }

    @Test
    void listMessages_WithStatusFilter() throws Exception {
        // Given
        Map<String, Object> response = Map.of(
            "messages", java.util.List.of(),
            "pagination", Map.of(
                "page", 0,
                "size", 20,
                "totalElements", 0L,
                "totalPages", 0,
                "hasNext", false,
                "hasPrevious", false
            )
        );

        when(messageService.listMessages(anyString(), anyInt(), anyInt(), eq("SENT")))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/v1/messages")
                .header("Authorization", "Bearer test-api-key")
                .param("page", "0")
                .param("size", "20")
                .param("status", "SENT"))
                .andExpected(status().isOk());

        verify(messageService).listMessages("Bearer test-api-key", 0, 20, "SENT");
    }
}