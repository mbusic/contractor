package hr.qnr.contractor.dto;

public record ClientCreateRequest(
        String type,
        String name,
        String contactPerson,
        String phone,
        String email,
        String address
) {}
