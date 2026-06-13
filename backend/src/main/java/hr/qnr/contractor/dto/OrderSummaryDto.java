package hr.qnr.contractor.dto;

public record OrderSummaryDto(
        Long id,
        String orderNumber,
        BranchDto branch,
        String urgency,
        String status,
        String clientName,
        String location
) {}
