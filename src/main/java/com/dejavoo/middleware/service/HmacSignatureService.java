package com.dejavoo.middleware.service;

import com.dejavoo.middleware.dto.SettlementTransactionDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HmacSignatureService {

    private static final String HMAC_SHA512_ALGORITHM = "HmacSHA512";

    /**
     * Generate HMAC-SHA512 signature for settlement webhook
     * @param id Settlement ID
     * @param tpn Terminal ID
     * @param version Version
     * @param versionCreatedDt Version created date
     * @param eventType Event type
     * @param subEventType Sub event type
     * @param requestType Request type
     * @param batchNumber Batch number
     * @param settlementDate Settlement date
     * @param settlementCount Settlement count
     * @param settlementAmount Settlement amount
     * @param settlementTxnDetails Settlement transaction details
     * @param secretKey Secret key for HMAC
     * @return HMAC signature as hex string
     */
    public String generateSignature(
            String id,
            String tpn,
            String version,
            String versionCreatedDt,
            String eventType,
            String subEventType,
            String requestType,
            String batchNumber,
            String settlementDate,
            Integer settlementCount,
            Object settlementAmount,
            List<SettlementTransactionDetail> settlementTxnDetails,
            String secretKey) {

        // Use TreeMap to maintain sorted order of fields
        Map<String, String> fields = new TreeMap<>();
        fields.put("id", id != null ? id : "");
        fields.put("tpn", tpn != null ? tpn : "");
        fields.put("version", version != null ? version : "");
        fields.put("createdDt", versionCreatedDt != null ? versionCreatedDt : "");
        fields.put("eventType", eventType != null ? eventType : "");
        fields.put("subEventType", subEventType != null ? subEventType : "");
        fields.put("requestType", requestType != null ? requestType : "");
        fields.put("batchNumber", batchNumber != null ? batchNumber : "");
        fields.put("settlementDate", settlementDate != null ? settlementDate : "");
        fields.put("settlementCount", settlementCount != null ? String.valueOf(settlementCount) : "");
        fields.put("settlementAmount", settlementAmount != null ? String.valueOf(settlementAmount) : "");
        fields.put("settlementTxnDetails", convertSettlementTxnDetailsToString(settlementTxnDetails));

        // Concatenate fields with | as delimiter
        String concatenatedString = fields.values().stream()
                .collect(Collectors.joining("|"));

        log.debug("HMAC concatenated string: {}", concatenatedString);

        try {
            Mac sha512Hmac = Mac.getInstance(HMAC_SHA512_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512_ALGORITHM);
            sha512Hmac.init(secretKeySpec);

            byte[] hashBytes = sha512Hmac.doFinal(concatenatedString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error while generating HMAC-SHA512", e);
            throw new RuntimeException("Error while generating HMAC-SHA512", e);
        }
    }

    /**
     * Verify HMAC signature
     * @param expectedSignature The signature from the request
     * @param id Settlement ID
     * @param tpn Terminal ID
     * @param version Version
     * @param versionCreatedDt Version created date
     * @param eventType Event type
     * @param subEventType Sub event type
     * @param requestType Request type
     * @param batchNumber Batch number
     * @param settlementDate Settlement date
     * @param settlementCount Settlement count
     * @param settlementAmount Settlement amount
     * @param settlementTxnDetails Settlement transaction details
     * @param secretKey Secret key for HMAC
     * @return true if signature is valid, false otherwise
     */
    public boolean verifySignature(
            String expectedSignature,
            String id,
            String tpn,
            String version,
            String versionCreatedDt,
            String eventType,
            String subEventType,
            String requestType,
            String batchNumber,
            String settlementDate,
            Integer settlementCount,
            Object settlementAmount,
            List<SettlementTransactionDetail> settlementTxnDetails,
            String secretKey) {

        if (expectedSignature == null || expectedSignature.isEmpty()) {
            log.warn("No signature provided in request");
            return false;
        }

        String computedSignature = generateSignature(
                id, tpn, version, versionCreatedDt, eventType, subEventType,
                requestType, batchNumber, settlementDate, settlementCount,
                settlementAmount, settlementTxnDetails, secretKey
        );

        boolean isValid = expectedSignature.equalsIgnoreCase(computedSignature);

        if (!isValid) {
            log.warn("Signature verification failed. Expected: {}, Computed: {}",
                    expectedSignature, computedSignature);
        } else {
            log.debug("Signature verification successful");
        }

        return isValid;
    }

    /**
     * Convert settlement transaction details to string format for HMAC
     * @param details List of transaction details
     * @return String representation
     */
    private String convertSettlementTxnDetailsToString(List<SettlementTransactionDetail> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }

        return details.stream()
                .map(detail -> {
                    Map<String, Object> detailFields = new TreeMap<>();
                    detailFields.put("txnDate", detail.getTxnDate());
                    detailFields.put("transactionType", detail.getTransactionType());
                    detailFields.put("transactionId", detail.getTransactionId());
                    detailFields.put("txnAmount", detail.getTxnAmount());
                    detailFields.put("baseAmount", detail.getBaseAmount());
                    return detailFields.values().stream()
                            .map(this::getOrDefault)
                            .collect(Collectors.joining("|"));
                })
                .collect(Collectors.joining("|"));
    }

    /**
     * Get string value or empty string if null
     * @param value Object value
     * @return String representation or empty string
     */
    private String getOrDefault(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
}
