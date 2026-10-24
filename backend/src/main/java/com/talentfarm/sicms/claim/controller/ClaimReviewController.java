package com.talentfarm.sicms.claim.controller;

import com.talentfarm.sicms.claim.dto.request.ReviewDecisionRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimDocumentResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimReviewDetailResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimReviewPageResponse;
import com.talentfarm.sicms.claim.service.ClaimReviewService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/review")
@PreAuthorize("hasRole('CLAIMS_OFFICER')")
public class ClaimReviewController {
    private final ClaimReviewService reviewService;

    public ClaimReviewController(ClaimReviewService reviewService) { this.reviewService = reviewService; }

    @GetMapping
    public ClaimReviewPageResponse queue(@RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return reviewService.queue(status, page, size);
    }

    @GetMapping("/{claimId}")
    public ClaimReviewDetailResponse detail(@PathVariable UUID claimId) { return reviewService.detail(claimId); }

    @GetMapping("/{claimId}/documents")
    public List<ClaimDocumentResponse> documents(@PathVariable UUID claimId) { return reviewService.documents(claimId); }

    @GetMapping("/{claimId}/documents/{documentId}")
    public ResponseEntity<Resource> download(@PathVariable UUID claimId, @PathVariable UUID documentId) {
        var downloaded = reviewService.downloadDocument(claimId, documentId);
        String filename = downloaded.document().getOriginalFileName().replaceAll("[\\r\\n\\\"\\\\]", "_");
        ContentDisposition disposition = ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(downloaded.document().getContentType()))
                .contentLength(downloaded.document().getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(downloaded.resource());
    }

    @PostMapping("/{claimId}/start")
    public ClaimReviewDetailResponse start(Authentication authentication, @PathVariable UUID claimId,
                                           @Valid @RequestBody ReviewDecisionRequest request) {
        return reviewService.start(authentication, claimId, request);
    }

    @PostMapping("/{claimId}/request-documents")
    public ClaimReviewDetailResponse requestDocuments(Authentication authentication, @PathVariable UUID claimId,
                                                      @Valid @RequestBody ReviewDecisionRequest request) {
        return reviewService.requestDocuments(authentication, claimId, request);
    }

    @PostMapping("/{claimId}/resume")
    public ClaimReviewDetailResponse resume(Authentication authentication, @PathVariable UUID claimId,
                                            @Valid @RequestBody ReviewDecisionRequest request) {
        return reviewService.resume(authentication, claimId, request);
    }

    @PostMapping("/{claimId}/approve")
    public ClaimReviewDetailResponse approve(Authentication authentication, @PathVariable UUID claimId,
                                             @Valid @RequestBody ReviewDecisionRequest request) {
        return reviewService.approve(authentication, claimId, request);
    }

    @PostMapping("/{claimId}/reject")
    public ClaimReviewDetailResponse reject(Authentication authentication, @PathVariable UUID claimId,
                                            @Valid @RequestBody ReviewDecisionRequest request) {
        return reviewService.reject(authentication, claimId, request);
    }
}