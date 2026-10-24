package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.claim.domain.ClaimDocument;
import com.talentfarm.sicms.claim.dto.response.ClaimDocumentResponse;
import com.talentfarm.sicms.claim.repository.ClaimDocumentRepository;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.storage.model.StoredFile;
import com.talentfarm.sicms.storage.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimDocumentService {
    private final CustomerRepository customerRepository;
    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;
    private final StorageService storageService;

    public ClaimDocumentService(CustomerRepository customerRepository, ClaimRepository claimRepository,
                                ClaimDocumentRepository documentRepository, StorageService storageService) {
        this.customerRepository = customerRepository;
        this.claimRepository = claimRepository;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ClaimDocumentResponse upload(Authentication authentication, UUID claimId, MultipartFile file) {
        Claim claim = ownedClaim(authentication, claimId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Claim document must not be empty");
        StoredFile stored;
        try {
            stored = storageService.store(claimId.toString(), file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getInputStream());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Claim document could not be read");
        }
        try {
            ClaimDocument document = documentRepository.saveAndFlush(new ClaimDocument(claim, stored.originalFileName(),
                    stored.storedFileName(), stored.contentType(), stored.fileSize(), stored.storageKey()));
            return toResponse(document);
        } catch (RuntimeException exception) {
            storageService.delete(stored.storageKey());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ClaimDocumentResponse> list(Authentication authentication, UUID claimId) {
        ownedClaim(authentication, claimId);
        return documentRepository.findByClaimIdOrderByUploadedAtAsc(claimId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DownloadedDocument download(Authentication authentication, UUID claimId, UUID documentId) {
        ownedClaim(authentication, claimId);
        ClaimDocument document = documentRepository.findByIdAndClaimId(documentId, claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim document was not found"));
        return new DownloadedDocument(document, storageService.load(document.getStorageKey()));
    }

    private Claim ownedClaim(Authentication authentication, UUID claimId) {
        Customer customer = currentCustomer(authentication);
        return claimRepository.findByIdAndPolicyCustomerId(claimId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim was not found"));
    }

    private Customer currentCustomer(Authentication authentication) {
        UUID userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found"));
    }

    private ClaimDocumentResponse toResponse(ClaimDocument document) {
        return new ClaimDocumentResponse(document.getId(), document.getOriginalFileName(), document.getContentType(),
                document.getFileSize(), document.getUploadedAt());
    }

    public record DownloadedDocument(ClaimDocument document, Resource resource) { }
}