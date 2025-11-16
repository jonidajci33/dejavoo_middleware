package com.dejavoo.middleware.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementWebhookResponse {

    private boolean success;
    private String message;
    private String settlementId;
    private String status;

    public static SettlementWebhookResponse success(String settlementId, String message) {
        return SettlementWebhookResponse.builder()
                .success(true)
                .settlementId(settlementId)
                .message(message)
                .status("PROCESSED")
                .build();
    }

    public static SettlementWebhookResponse alreadyProcessed(String settlementId) {
        return SettlementWebhookResponse.builder()
                .success(false)
                .settlementId(settlementId)
                .message("Settlement already processed")
                .status("DUPLICATE")
                .build();
    }

    public static SettlementWebhookResponse error(String message) {
        return SettlementWebhookResponse.builder()
                .success(false)
                .message(message)
                .status("ERROR")
                .build();
    }
}
