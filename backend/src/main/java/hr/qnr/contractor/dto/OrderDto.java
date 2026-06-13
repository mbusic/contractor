package hr.qnr.contractor.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        String orderNumber,
        BranchDto branch,
        ClientDto client,
        String location,
        String contactPerson,
        String phone,
        String email,
        String description,
        String urgency,
        String status,
        UserDto assignedServicer,

        Integer estimatedKm,
        Double estimatedWorkHours,
        Integer estimatedNumberOfWorkers,
        Double estimatedTotalHours,
        BigDecimal estimatedMaterialCost,

        Integer actualKm,
        Double actualWorkHours,
        Integer actualNumberOfWorkers,
        Double actualTotalHours,
        BigDecimal actualMaterialCost,

        Instant createdAt,
        Instant updatedAt,
        List<NoteDto> notes,
        List<PhotoDto> photos
) {}
