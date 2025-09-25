package com.mindshift.ums.api.controller;

import com.mindshift.ums.api.dto.ErrorDto;
import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/v1/messages")
@Tag(name = "Messages", description = "Message sending and status APIs")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(
        summary = "Send a message",
        description = "Submit a message for asynchronous processing. Returns 202 Accepted immediately."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Message accepted for processing",
            content = @Content(schema = @Schema(implementation = SendMessageDto.AcceptResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorDto.ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(schema = @Schema(implementation = ErrorDto.ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorDto.ErrorResponse.class))
        )
    })
    public ResponseEntity<SendMessageDto.AcceptResponse> sendMessage(
            @Valid @RequestBody SendMessageDto.SendMessageRequest request,
            @Parameter(description = "Idempotency key for duplicate prevention")
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Parameter(description = "Tenant API key")
            @RequestHeader("Authorization") String authorization) {

        // Set idempotency key in request if not already set
        if (request.getIdempotencyKey() == null) {
            request.setIdempotencyKey(idempotencyKey);
        }

        String requestId = messageService.acceptMessage(request, idempotencyKey, authorization);

        SendMessageDto.AcceptResponse response = new SendMessageDto.AcceptResponse(
            requestId,
            "ACCEPTED",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{requestId}")
    @Operation(
        summary = "Get message status",
        description = "Retrieve the current status of a message by request ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Message status retrieved",
            content = @Content(schema = @Schema(implementation = SendMessageDto.MessageStatusResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Message not found",
            content = @Content(schema = @Schema(implementation = ErrorDto.ErrorResponse.class))
        )
    })
    public ResponseEntity<SendMessageDto.MessageStatusResponse> getMessageStatus(
            @Parameter(description = "Message request ID")
            @PathVariable String requestId) {

        SendMessageDto.MessageStatusResponse status = messageService.getMessageStatus(requestId);
        return ResponseEntity.ok(status);
    }

    @GetMapping
    @Operation(
        summary = "List messages",
        description = "List messages for the authenticated tenant with pagination"
    )
    public ResponseEntity<Map<String, Object>> listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String authorization) {

        Map<String, Object> result = messageService.listMessages(authorization, page, size, status);
        return ResponseEntity.ok(result);
    }
}