package com.dejavoo.middleware.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementWebhookRequest {

    @NotBlank(message = "ID is required")
    private String id;

    @NotBlank(message = "Event type is required")
    private String eventType;

    private String subEventType;

    private String requestType;

    private String signature;

    private String version;

    private String versionCreatedDt;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotBlank(message = "Settlement date is required")
    private String settlementDate;

    @NotNull(message = "Settlement count is required")
    private Integer settlementCount;

    @NotNull(message = "Settlement amount is required")
    private BigDecimal settlementAmount;

    private String tpn;

    @JsonProperty("settlementTxnDetails")
    private List<SettlementTransactionDetail> settlementTxnDetails;

    private Map<String, String> headers;
}
