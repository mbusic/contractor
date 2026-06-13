package hr.qnr.contractor.dto;

public record UserDto(
        Long id,
        String username,
        String role,
        String displayName,
        Long clientId,
        String clientName,
        Long branchId,
        String branchName
) {}
