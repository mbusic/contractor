package hr.qnr.contractor.dto;

public record LoginResponse(
        String token,
        String username,
        String role,
        String displayName,
        Long userId,
        Long clientId,
        Long branchId
) {}
