package hr.qnr.contractor.dto;

import java.math.BigDecimal;

public record OrderUpdateRequest(
        Long branchId,
        Long clientId,
        String location,
        String contactPerson,
        String phone,
        String email,
        String description,
        String urgency,
        Long assignedServicerId,

        Integer estimatedKm,
        Double estimatedWorkHours,
        Integer estimatedNumberOfWorkers,
        Double estimatedTotalHours,
        BigDecimal estimatedMaterialCost,

        Integer actualKm,
        Double actualWorkHours,
        Integer actualNumberOfWorkers,
        Double actualTotalHours,
        BigDecimal actualMaterialCost
) {}
