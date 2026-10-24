package com.talentfarm.sicms.claim.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClaimDocumentResponse(UUID documentId, String originalFileName, String contentType,
                                    long fileSize, Instant uploadedAt) { }