package com.talentfarm.sicms.storage.model;

public record StoredFile(String originalFileName, String storedFileName, String storageKey, long fileSize, String contentType) { }