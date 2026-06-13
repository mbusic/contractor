package hr.qnr.contractor.dto;

import java.util.List;

public record ClientDto(
        Long id,
        String type,
        String name,
        String contactPerson,
        String phone,
        String email,
        String address,
        List<LocationDto> locations
) {}
