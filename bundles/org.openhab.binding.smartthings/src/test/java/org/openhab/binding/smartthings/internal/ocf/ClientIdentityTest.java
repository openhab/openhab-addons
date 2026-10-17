/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Persistent generated client profile, renewal and protected-storage tests.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class ClientIdentityTest {
    @TempDir
    Path temporary = Path.of("target");

    @Test
    void generatesTheSamsungProfileAndReusesExactlyTheSamePersistedIdentity() throws Exception {
        Path directory = temporary.resolve("identity");
        Instant now = Instant.now();
        var identity = ClientIdentity.load(directory, now);
        var certificate = (X509Certificate) identity.chain()[0];
        certificate.checkValidity();
        certificate.verify(certificate.getPublicKey());
        assertEquals("SHA256withRSA", certificate.getSigAlgName());
        assertEquals(2048, ((RSAPublicKey) certificate.getPublicKey()).getModulus().bitLength());
        assertEquals(-1, certificate.getBasicConstraints());
        assertTrue(certificate.getKeyUsage()[0]);
        assertTrue(certificate.getKeyUsage()[2]);
        assertEquals(List.of("1.3.6.1.5.5.7.3.2"), certificate.getExtendedKeyUsage());
        assertTrue(certificate.getSubjectX500Principal().getName().contains("OU=uuid:" + ClientIdentity.SERVICE_UUID));
        assertEquals(List.of(List.of(6, "urn:uuid:" + ClientIdentity.SERVICE_UUID)),
                List.copyOf(certificate.getSubjectAlternativeNames()));
        byte[] persisted = Files.readAllBytes(directory.resolve("client.p12"));
        var reused = ClientIdentity.load(directory, now.plusSeconds(60));
        assertArrayEquals(identity.privateKey().getEncoded(), reused.privateKey().getEncoded());
        assertArrayEquals(certificate.getEncoded(), reused.chain()[0].getEncoded());
        assertArrayEquals(persisted, Files.readAllBytes(directory.resolve("client.p12")));
        if (Files.getFileStore(directory).supportsFileAttributeView("posix")) {
            assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(directory));
            assertEquals(PosixFilePermissions.fromString("rw-------"),
                    Files.getPosixFilePermissions(directory.resolve("client.p12")));
        }
        try (var files = Files.list(directory)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void renewsNearExpiryAndAfterExpiryWithoutReplacingTheKey() throws Exception {
        Path directory = temporary.resolve("identity");
        Instant now = Instant.now();
        var original = ClientIdentity.load(directory, now);
        var renewed = ClientIdentity.load(directory, now.plus(Duration.ofDays(3640)));
        assertArrayEquals(original.privateKey().getEncoded(), renewed.privateKey().getEncoded());
        assertFalse(java.util.Arrays.equals(original.chain()[0].getEncoded(), renewed.chain()[0].getEncoded()));
        var afterExpiry = ClientIdentity.load(directory, now.plus(Duration.ofDays(7300)));
        assertArrayEquals(original.privateKey().getEncoded(), afterExpiry.privateKey().getEncoded());
        ((X509Certificate) afterExpiry.chain()[0]).checkValidity(java.util.Date.from(now.plus(Duration.ofDays(7300))));
    }

    @Test
    void doesNotOverwriteACorruptedStoreOrRegenerateAfterAReadFailure() throws Exception {
        Path directory = temporary.resolve("identity");
        ClientIdentity.load(directory, Instant.now());
        Path file = directory.resolve("client.p12");
        byte[] damaged = new byte[] { 1, 2, 3 };
        Files.write(file, damaged);
        assertThrows(IOException.class, () -> ClientIdentity.load(directory, Instant.now()));
        assertArrayEquals(damaged, Files.readAllBytes(file));
    }

    @Test
    void lockContentionFailsWithoutReplacingTheIdentityAndRecoversAfterRelease() throws Exception {
        Path directory = temporary.resolve("identity");
        var identity = ClientIdentity.load(directory, Instant.now());
        try (var channel = java.nio.channels.FileChannel.open(directory.resolve("client.lock"),
                java.nio.file.StandardOpenOption.WRITE); var lock = channel.lock()) {
            assertTrue(lock.isValid());
            assertThrows(IOException.class, () -> ClientIdentity.load(directory, Instant.now()));
        }
        assertArrayEquals(identity.privateKey().getEncoded(),
                ClientIdentity.load(directory, Instant.now()).privateKey().getEncoded());
    }

    @Test
    void concurrentInitializationCreatesOneIdentity() throws Exception {
        Path directory = temporary.resolve("identity");
        var first = CompletableFuture.supplyAsync(() -> load(directory));
        var second = CompletableFuture.supplyAsync(() -> load(directory));
        try {
            assertArrayEquals(first.get(30, TimeUnit.SECONDS).privateKey().getEncoded(),
                    second.get(30, TimeUnit.SECONDS).privateKey().getEncoded());
        } finally {
            first.cancel(true);
            second.cancel(true);
        }
    }

    @Test
    void rejectsSymbolicLinksWithoutChangingTheTarget() throws Exception {
        assumeTrue(Files.getFileStore(temporary).supportsFileAttributeView("posix"));
        Path directory = temporary.resolve("identity");
        ClientIdentity.load(directory, Instant.now());
        Path file = directory.resolve("client.p12");
        Files.delete(file);
        Path unrelated = temporary.resolve("unrelated");
        Files.writeString(unrelated, "untouched");
        Files.createSymbolicLink(file, unrelated);
        assertThrows(IOException.class, () -> ClientIdentity.load(directory, Instant.now()));
        assertEquals("untouched", Files.readString(unrelated));
        Path linkedDirectory = temporary.resolve("linked-identity");
        Files.createSymbolicLink(linkedDirectory, directory);
        assertThrows(IOException.class, () -> ClientIdentity.load(linkedDirectory, Instant.now()));
    }

    private static ClientIdentity load(Path directory) {
        try {
            return ClientIdentity.load(directory, Instant.now());
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
