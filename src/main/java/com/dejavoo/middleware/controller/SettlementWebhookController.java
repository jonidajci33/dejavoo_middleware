package com.dejavoo.middleware.controller;

import com.dejavoo.middleware.dto.SettlementWebhookRequest;
import com.dejavoo.middleware.dto.SettlementWebhookResponse;
import com.dejavoo.middleware.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class SettlementWebhookController {

    private final SettlementService settlementService;

    /**
     * Webhook endpoint for receiving settlement notifications
     * @param merchantId the merchant ID from path variable
     * @param request the settlement webhook request
     * @return ResponseEntity with settlement processing result
     */
    @PostMapping("/{merchantId}")
    public ResponseEntity<SettlementWebhookResponse> handleSettlementWebhook(
            @PathVariable String merchantId,
            @Valid @RequestBody SettlementWebhookRequest request) {

        log.info("Received settlement webhook for merchant: {}, settlement ID: {}",
                merchantId, request.getId());

        // Process the settlement
        SettlementWebhookResponse response = settlementService.processSettlement(request, merchantId);

        // Return appropriate HTTP status based on the result
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if ("DUPLICATE".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Health check endpoint
     * @return simple health check response
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Settlement Webhook Service is running");
    }
}
