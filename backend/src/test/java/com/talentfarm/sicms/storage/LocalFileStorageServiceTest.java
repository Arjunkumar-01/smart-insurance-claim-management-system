package com.talentfarm.sicms.storage;

import com.talentfarm.sicms.storage.exception.StorageException;
import com.talentfarm.sicms.storage.service.LocalFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {
    @TempDir
    Path temporaryRoot;

    @Test
    void storesAndLoadsAllowedFileWithGeneratedNameAndSanitizedOriginalName() throws Exception {
        LocalFileStorageService storage = new LocalFileStorageService(temporaryRoot.toString());
        byte[] bytes = "synthetic pdf bytes".getBytes(StandardCharsets.UTF_8);
        var stored = storage.store("claim-1", "..\\..\\synthetic.pdf", "application/pdf", bytes.length, new java.io.ByteArrayInputStream(bytes));

        assertEquals("synthetic.pdf", stored.originalFileName());
        assertNotEquals("synthetic.pdf", stored.storedFileName());
        assertTrue(storage.load(stored.storageKey()).exists());
        assertEquals("synthetic pdf bytes", new String(storage.load(stored.storageKey()).getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void rejectsEmptyOversizedUnsupportedAndTraversalStorageKeys() {
        LocalFileStorageService storage = new LocalFileStorageService(temporaryRoot.toString());
        assertThrows(StorageException.class, () -> storage.store("claim", "empty.pdf", "application/pdf", 0, InputStreamStub.EMPTY));
        assertThrows(StorageException.class, () -> storage.store("claim", "large.pdf", "application/pdf", 10L * 1024 * 1024 + 1, InputStreamStub.EMPTY));
        assertThrows(StorageException.class, () -> storage.store("claim", "script.html", "text/html", 1, InputStreamStub.EMPTY));
        assertThrows(StorageException.class, () -> storage.load("../outside"));
    }

    private static final class InputStreamStub {
        private static final java.io.InputStream EMPTY = new java.io.ByteArrayInputStream(new byte[]{1});
    }
}