package com.talentfarm.sicms.storage.service;

import com.talentfarm.sicms.storage.model.StoredFile;
import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface StorageService {
    StoredFile store(String claimId, String originalFileName, String contentType, long fileSize, InputStream content);
    Resource load(String storageKey);
    void delete(String storageKey);
}