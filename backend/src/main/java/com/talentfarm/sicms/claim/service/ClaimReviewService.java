package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.claim.domain.ClaimDocument;
import com.talentfarm.sicms.claim.domain.ClaimReview;
import com.talentfarm.sicms.claim.domain.ClaimReviewAction;
import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.claim.domain.ClaimStatusHistory;
import com.talentfarm.sicms.claim.dto.request.ReviewDecisionRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimDocumentResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimReviewDetailResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimReviewPageResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimReviewResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimStatusHistoryResponse;
import com.talentfarm.sicms.claim.repository.ClaimDocumentRepository;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.repository.ClaimReviewRepository;
import com.talentfarm.sicms.claim.repository.ClaimStatusHistoryRepository;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.storage.service.StorageService;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClaimReviewService {
    private static final int MAX_PAGE_SIZE = 50;
    private static final List<ClaimStatus> DEFAULT_QUEUE = List.of(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW, ClaimStatus.DOCUMENTS_REQUIRED);
    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;
    private final ClaimReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ClaimWorkflowService workflow;
    private final ClaimStatusTransitionService transitionService;
    private final ClaimStatusHistoryRepository historyRepository;
    private final AuditService auditService;

    public ClaimReviewService(ClaimRepository claimRepository, ClaimDocumentRepository documentRepository,
                              ClaimReviewRepository reviewRepository, UserRepository userRepository,
                              StorageService storageService, ClaimWorkflowService workflow,
                              ClaimStatusTransitionService transitionService, ClaimStatusHistoryRepository historyRepository, AuditService auditService) {
        this.claimRepository = claimRepository;
        this.documentRepository = documentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.workflow = workflow;
        this.transitionService = transitionService;
        this.historyRepository = historyRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ClaimReviewPageResponse queue(String status, int page, int size) {
        validatePage(page, size);
        List<ClaimStatus> statuses = status == null || status.isBlank() ? DEFAULT_QUEUE : List.of(parseStatus(status));
        Page<Claim> claims = claimRepository.findByStatusIn(statuses, PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "submittedAt")));
        return new ClaimReviewPageResponse(claims.getContent().stream().map(this::detail).toList(), claims.getNumber(),
                claims.getSize(), claims.getTotalElements(), claims.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ClaimReviewDetailResponse detail(UUID claimId) { return detail(findClaim(claimId)); }

    @Transactional
    public ClaimReviewDetailResponse start(Authentication authentication, UUID claimId, ReviewDecisionRequest request) {
        return transition(authentication, claimId, ClaimStatus.UNDER_REVIEW, ClaimReviewAction.START_REVIEW, request);
    }

    @Transactional
    public ClaimReviewDetailResponse requestDocuments(Authentication authentication, UUID claimId, ReviewDecisionRequest request) {
        return transition(authentication, claimId, ClaimStatus.DOCUMENTS_REQUIRED, ClaimReviewAction.REQUEST_DOCUMENTS, request);
    }

    @Transactional
    public ClaimReviewDetailResponse resume(Authentication authentication, UUID claimId, ReviewDecisionRequest request) {
        return transition(authentication, claimId, ClaimStatus.UNDER_REVIEW, ClaimReviewAction.RESUME_REVIEW, request);
    }

    @Transactional
    public ClaimReviewDetailResponse approve(Authentication authentication, UUID claimId, ReviewDecisionRequest request) {
        return transition(authentication, claimId, ClaimStatus.APPROVED, ClaimReviewAction.APPROVE, request);
    }

    @Transactional
    public ClaimReviewDetailResponse reject(Authentication authentication, UUID claimId, ReviewDecisionRequest request) {
        return transition(authentication, claimId, ClaimStatus.REJECTED, ClaimReviewAction.REJECT, request);
    }

    @Transactional(readOnly = true)
    public List<ClaimDocumentResponse> documents(UUID claimId) {
        findClaim(claimId);
        return documentRepository.findByClaimIdOrderByUploadedAtAsc(claimId).stream().map(this::documentResponse).toList();
    }

    @Transactional(readOnly = true)
    public DownloadedReviewDocument downloadDocument(UUID claimId, UUID documentId) {
        findClaim(claimId);
        ClaimDocument document = documentRepository.findByIdAndClaimId(documentId, claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim document was not found"));
        return new DownloadedReviewDocument(document, storageService.load(document.getStorageKey()));
    }

    private ClaimReviewDetailResponse transition(Authentication authentication, UUID claimId, ClaimStatus next,
                                                 ClaimReviewAction action, ReviewDecisionRequest request) {
        Claim claim = findClaim(claimId);
        User reviewer = userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer account was not found"));
        transitionService.transition(claim, next, reviewer, reasonFor(action));
        reviewRepository.save(new ClaimReview(claim, reviewer, action, clean(request == null ? null : request.notes())));
        auditService.record(auditAction(action), reviewer, "CLAIM", claimId, "Claim review action: " + action);
        return detail(claim);
    }

    private Claim findClaim(UUID claimId) {
        return claimRepository.findById(claimId).orElseThrow(() -> new ResourceNotFoundException("Claim was not found"));
    }

    private ClaimReviewDetailResponse detail(Claim claim) {
        return new ClaimReviewDetailResponse(claim.getId(), claim.getClaimNumber(), claim.getClaimAmount(),
            claim.getPolicy().getCoverageAmount(), claim.getClaimType(), claim.getIncidentDate(), claim.getDescription(), claim.getStatus(),
                claim.getPolicy().getPolicyNumber(), claim.getSubmittedAt(),
                documentRepository.findByClaimIdOrderByUploadedAtAsc(claim.getId()).stream().map(this::documentResponse).toList(),
                reviewRepository.findByClaimIdOrderByCreatedAtAsc(claim.getId()).stream().map(this::reviewResponse).toList(),
                historyRepository.findByClaimIdOrderByChangedAtAscIdAsc(claim.getId()).stream().map(this::historyResponse).toList());
    }

    private ClaimReviewResponse reviewResponse(ClaimReview review) {
        return new ClaimReviewResponse(review.getId(), review.getAction(), review.getNotes(),
                review.getReviewedBy().getId(), review.getCreatedAt());
    }

    private ClaimDocumentResponse documentResponse(ClaimDocument document) {
        return new ClaimDocumentResponse(document.getId(), document.getOriginalFileName(), document.getContentType(),
                document.getFileSize(), document.getUploadedAt());
    }

    private ClaimStatusHistoryResponse historyResponse(ClaimStatusHistory history) {
        String actor = history.getChangedBy() == null ? "SYSTEM" : history.getChangedBy().getRoles().stream()
                .map(role -> role.getName().name()).findFirst().orElse("USER");
        return new ClaimStatusHistoryResponse(history.getId(), history.getFromStatus(), history.getToStatus(), actor,
                history.getChangeReason(), history.getChangedAt());
    }

    private String reasonFor(ClaimReviewAction action) {
        return switch (action) {
            case START_REVIEW -> "review started";
            case REQUEST_DOCUMENTS -> "additional documents requested";
            case RESUME_REVIEW -> "review resumed";
            case APPROVE -> "claim approved";
            case REJECT -> "claim rejected";
        };
    }

    private AuditAction auditAction(ClaimReviewAction action) {
        return switch (action) {
            case START_REVIEW -> AuditAction.CLAIM_REVIEW_STARTED;
            case REQUEST_DOCUMENTS -> AuditAction.CLAIM_DOCUMENTS_REQUESTED;
            case RESUME_REVIEW -> AuditAction.CLAIM_REVIEW_RESUMED;
            case APPROVE -> AuditAction.CLAIM_APPROVED;
            case REJECT -> AuditAction.CLAIM_REJECTED;
        };
    }

    private ClaimStatus parseStatus(String value) {
        try { return ClaimStatus.valueOf(value.trim().toUpperCase()); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("Claim status is invalid"); }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50");
    }

    private String clean(String value) { return value == null ? null : value.trim(); }

    public record DownloadedReviewDocument(ClaimDocument document, Resource resource) { }
}