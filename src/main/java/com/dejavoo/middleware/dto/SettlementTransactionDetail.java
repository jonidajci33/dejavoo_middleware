package com.dejavoo.middleware.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementTransactionDetail {

    private String txnDate;

    private String transactionType;

    private String transactionId;

    private BigDecimal txnAmount;

    private BigDecimal baseAmount;
}
