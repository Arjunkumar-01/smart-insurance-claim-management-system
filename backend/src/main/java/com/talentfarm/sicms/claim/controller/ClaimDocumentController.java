package com.talentfarm.sicms.claim.controller;

import com.talentfarm.sicms.claim.dto.response.ClaimDocumentResponse;
import com.talentfarm.sicms.claim.service.ClaimDocumentService;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/me/{claimId}/documents")
@PreAuthorize("hasRole('CUSTOMER')")
public class ClaimDocumentController {
    private final ClaimDocumentService documentService;

    public ClaimDocumentController(ClaimDocumentService documentService) { this.documentService = documentService; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimDocumentResponse> upload(Authentication authentication, @PathVariable UUID claimId,
                                                         @RequestPart("file") @NotNull MultipartFile file) {
        return ResponseEntity.status(201).body(documentService.upload(authentication, claimId, file));
    }

    @GetMapping
    public List<ClaimDocumentResponse> list(Authentication authentication, @PathVariable UUID claimId) {
        return documentService.list(authentication, claimId);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Resource> download(Authentication authentication, @PathVariable UUID claimId,
                                             @PathVariable UUID documentId) {
        var downloaded = documentService.download(authentication, claimId, documentId);
        String filename = downloaded.document().getOriginalFileName().replaceAll("[\\r\\n\\\"\\\\]", "_");
        MediaType mediaType = MediaType.parseMediaType(downloaded.document().getContentType());
        ContentDisposition disposition = ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(mediaType).contentLength(downloaded.document().getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(downloaded.resource());
    }
}