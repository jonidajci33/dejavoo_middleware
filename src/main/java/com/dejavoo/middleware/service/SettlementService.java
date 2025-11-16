package com.dejavoo.middleware.service;

import com.dejavoo.middleware.dto.SettlementWebhookRequest;
import com.dejavoo.middleware.dto.SettlementWebhookResponse;
import com.dejavoo.middleware.entity.MerchantSecret;
import com.dejavoo.middleware.entity.Settlement;
import com.dejavoo.middleware.repository.MerchantSecretRepository;
import com.dejavoo.middleware.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MerchantSecretRepository merchantSecretRepository;
    private final HmacSignatureService hmacSignatureService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Process settlement webhook request
     * @param request the settlement webhook request
     * @param merchantId the merchant ID from the URL path
     * @return SettlementWebhookResponse indicating success or failure
     */
    @Transactional
    public SettlementWebhookResponse processSettlement(SettlementWebhookRequest request, String merchantId) {
        try {
            log.info("Processing settlement webhook for merchant: {}, settlement ID: {}",
                    merchantId, request.getId());

            // Verify HMAC signature
            if (!verifySignature(request, merchantId)) {
                log.error("HMAC signature verification failed for merchant: {}", merchantId);
                return SettlementWebhookResponse.error("Invalid signature");
            }

            // Check if settlement has already been processed
            if (settlementRepository.existsBySettlementId(request.getId())) {
                log.warn("Settlement with ID {} has already been processed", request.getId());
                return SettlementWebhookResponse.alreadyProcessed(request.getId());
            }

            // Parse settlement date
            LocalDate settlementDate = parseSettlementDate(request.getSettlementDate());

            // Create and save new settlement record
            Settlement settlement = Settlement.builder()
                    .settlementId(request.getId())
                    .merchantId(merchantId)
                    .settlementDate(settlementDate)
                    .settlementAmount(request.getSettlementAmount())
                    .batchNumber(request.getBatchNumber())
                    .settlementCount(request.getSettlementCount())
                    .tpn(request.getTpn())
                    .eventType(request.getEventType())
                    .subEventType(request.getSubEventType())
                    .build();

            Settlement savedSettlement = settlementRepository.save(settlement);

            log.info("Successfully processed settlement: {} for merchant: {}",
                    savedSettlement.getSettlementId(), merchantId);

            return SettlementWebhookResponse.success(
                    savedSettlement.getSettlementId(),
                    "Settlement processed successfully"
            );

        } catch (DateTimeParseException e) {
            log.error("Invalid settlement date format: {}", request.getSettlementDate(), e);
            return SettlementWebhookResponse.error("Invalid settlement date format");
        } catch (Exception e) {
            log.error("Error processing settlement for merchant: {}", merchantId, e);
            return SettlementWebhookResponse.error("Error processing settlement: " + e.getMessage());
        }
    }

    /**
     * Parse settlement date string to LocalDate
     * @param dateStr the date string in format yyyy-MM-dd
     * @return LocalDate object
     * @throws DateTimeParseException if date format is invalid
     */
    private LocalDate parseSettlementDate(String dateStr) throws DateTimeParseException {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * Check if settlement exists by ID
     * @param settlementId the settlement ID
     * @return true if exists, false otherwise
     */
    public boolean isSettlementProcessed(String settlementId) {
        return settlementRepository.existsBySettlementId(settlementId);
    }

    /**
     * Verify HMAC signature for the webhook request
     * @param request the settlement webhook request
     * @param merchantId the merchant ID
     * @return true if signature is valid, false otherwise
     */
    private boolean verifySignature(SettlementWebhookRequest request, String merchantId) {
        try {
            // Get merchant secret key from database
            Optional<MerchantSecret> merchantSecretOpt = merchantSecretRepository
                    .findByMerchantIdAndActive(merchantId, true);

            if (merchantSecretOpt.isEmpty()) {
                log.error("No active HMAC secret key found for merchant: {}", merchantId);
                return false;
            }

            String secretKey = merchantSecretOpt.get().getHmacSecretKey();

            // Verify signature using HMAC service
            return hmacSignatureService.verifySignature(
                    request.getSignature(),
                    request.getId(),
                    request.getTpn(),
                    request.getVersion(),
                    request.getVersionCreatedDt(),
                    request.getEventType(),
                    request.getSubEventType(),
                    request.getRequestType(),
                    request.getBatchNumber(),
                    request.getSettlementDate(),
                    request.getSettlementCount(),
                    request.getSettlementAmount(),
                    request.getSettlementTxnDetails(),
                    secretKey
            );

        } catch (Exception e) {
            log.error("Error verifying HMAC signature for merchant: {}", merchantId, e);
            return false;
        }
    }
}
