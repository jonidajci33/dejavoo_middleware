package com.dejavoo.middleware.service;

import com.dejavoo.middleware.dto.SettlementWebhookRequest;
import com.dejavoo.middleware.dto.SettlementWebhookResponse;
import com.dejavoo.middleware.entity.Settlement;
import com.dejavoo.middleware.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;

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
}
