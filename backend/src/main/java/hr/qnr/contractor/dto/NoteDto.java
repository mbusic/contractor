package hr.qnr.contractor.dto;

import java.time.Instant;

public record NoteDto(Long id, String text, String authorName, Instant createdAt) {}
