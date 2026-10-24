package com.talentfarm.sicms.storage.service;

import com.talentfarm.sicms.storage.exception.StorageException;
import com.talentfarm.sicms.storage.exception.StoredFileNotFoundException;
import com.talentfarm.sicms.storage.model.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService implements StorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private final Path root;
    private final long maxFileSize;

    public LocalFileStorageService(String configuredRoot) {
        this(configuredRoot, DataSize.ofMegabytes(10));
    }

    @Autowired
    public LocalFileStorageService(@Value("${application.document-storage.path:./var/documents}") String configuredRoot,
                                   @Value("${application.document-storage.max-file-size:10MB}") DataSize configuredMaxFileSize) {
        try {
            root = Paths.get(configuredRoot).toAbsolutePath().normalize().resolve("claims").normalize();
            Files.createDirectories(root);
            maxFileSize = configuredMaxFileSize.toBytes();
        } catch (IOException | RuntimeException exception) {
            throw new StorageException("Claim document storage is unavailable", exception);
        }
    }

    @Override
    public StoredFile store(String claimId, String originalFileName, String contentType, long fileSize, InputStream content) {
        validate(contentType, fileSize);
        String safeOriginalName = sanitizeOriginalName(originalFileName);
        String storedFileName = UUID.randomUUID().toString();
        Path claimDirectory = resolveInsideRoot(claimId);
        Path target = resolveInsideRoot(claimId + "/" + storedFileName);
        try {
            Files.createDirectories(claimDirectory);
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
                return new StoredFile(safeOriginalName, storedFileName, claimId + "/" + storedFileName,
                    fileSize, contentType.toLowerCase(Locale.ROOT));
        } catch (IOException exception) {
            throw new StorageException("Claim document could not be stored", exception);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path path = resolveInsideRoot(storageKey);
        if (!Files.isRegularFile(path)) throw new StoredFileNotFoundException("Claim document file was not found");
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String storageKey) {
        Path path = resolveInsideRoot(storageKey);
        try { Files.deleteIfExists(path); }
        catch (IOException exception) { throw new StorageException("Claim document cleanup failed", exception); }
    }

    private void validate(String contentType, long fileSize) {
        if (fileSize <= 0) throw new StorageException("Claim document must not be empty");
        if (fileSize > maxFileSize) throw new StorageException("Claim document exceeds the configured size limit");
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new StorageException("Claim document type is not supported");
        }
    }

    private String sanitizeOriginalName(String value) {
        if (value == null || value.isBlank()) throw new StorageException("Claim document filename is required");
        String normalized = value.replace('\\', '/');
        String basename = Paths.get(normalized).getFileName().toString().replaceAll("[\\x00-\\x1F\\x7F]", "").trim();
        if (basename.isBlank() || basename.equals(".") || basename.equals("..") || basename.length() > 255) {
            throw new StorageException("Claim document filename is invalid");
        }
        return basename;
    }

    private Path resolveInsideRoot(String key) {
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) throw new StorageException("Claim document path is invalid");
        return resolved;
    }
}