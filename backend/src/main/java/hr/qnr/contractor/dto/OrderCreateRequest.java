package hr.qnr.contractor.dto;

public record OrderCreateRequest(
        Long branchId,
        Long clientId,
        String location,
        String contactPerson,
        String phone,
        String email,
        String description,
        String urgency
) {}
