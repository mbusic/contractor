package hr.qnr.contractor.dto;

public record UserCreateRequest(
        String username,
        String password,
        String role,
        String displayName,
        Long clientId,
        Long branchId
) {}
