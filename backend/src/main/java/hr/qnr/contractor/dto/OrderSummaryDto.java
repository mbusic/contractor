package hr.qnr.contractor.dto;

import java.time.Instant;

public record OrderSummaryDto(
        Long id,
        String orderNumber,
        BranchDto branch,
        String urgency,
        String status,
        String clientName,
        String location,
        Instant createdAt,
        Integer actualKm,
        Double actualTotalHours,
        Integer actualNumberOfWorkers
) {}
