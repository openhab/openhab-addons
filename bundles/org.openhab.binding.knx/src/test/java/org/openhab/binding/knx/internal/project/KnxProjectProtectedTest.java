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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.project.ProtectedPartWriter.DEFLATED;
import static org.openhab.binding.knx.internal.project.ProtectedPartWriter.STORED;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests for {@link KnxProjectReader} on password-protected projects, built in the test by
 * {@link ProtectedPartWriter} from the made-up fixtures.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectProtectedTest {

    private static final String PASSWORD = "made-up Pass 42";
    private static final String PART = "P-3E4F";
    private static final long LARGE = 1L << 30;

    private final KnxProjectReader reader = new KnxProjectReader();

    private static Path file(byte[] inner) {
        return TestContainer.ofProtected(PART + ".zip", inner).write();
    }

    private static Path file(ProtectedPartWriter writer) {
        return file(writer.build());
    }

    private KnxProject read(Path file, @Nullable String password) throws Exception {
        return reader.read(file, password == null ? null : password.toCharArray());
    }

    private KnxProjectException fails(Path file, @Nullable String password) {
        return fails(reader, file, password);
    }

    private static KnxProjectException fails(KnxProjectReader reader, Path file, @Nullable String password) {
        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> reader.read(file, password == null ? null : password.toCharArray()));
        String message = String.valueOf(e.getMessage());
        assertFalse(message.contains(PASSWORD), message);
        return e;
    }

    private KnxProject unprotected(String fixture) throws Exception {
        return reader.read(TestContainer.of(fixture, PART).write(), null);
    }

    private static byte[] flipBit(byte[] data, int offset) {
        byte[] copy = data.clone();
        copy[offset] ^= 0x10;
        return copy;
    }

    @Test
    void readsAProtectedProjectAsEts6WritesIt() throws Exception {
        byte[] blob = new byte[3000];
        for (int i = 0; i < blob.length; i++) {
            blob[i] = (byte) (i * 7);
        }
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD).directory("BinaryData/")
                .aes("BinaryData/made-up.bin", blob, DEFLATED, 1, 3).aes("BinaryData/other.bin", blob, STORED, 1, 3);
        char[] password = PASSWORD.toCharArray();

        KnxProject project = reader.read(file(writer), password);

        assertEquals(unprotected("ets6"), project);
        assertEquals(3, project.groupAddresses().size());
        assertArrayEquals(PASSWORD.toCharArray(), password);
    }

    @Test
    void readsEveryGenerationsFixtureWhenProtected() throws Exception {
        for (String fixture : List.of("ets5", "ets6-free")) {
            assertEquals(unprotected(fixture), read(file(ProtectedPartWriter.ets6(fixture, PASSWORD)), PASSWORD),
                    fixture);
        }
    }

    @Test
    void readsStoredAndDeflatedEntriesOfEveryKeyStrength() throws Exception {
        KnxProject expected = unprotected("ets6");
        String installation = TestContainer.resource("ets6/0.xml");
        String projectXml = TestContainer.resource("ets6/project.xml");
        for (int strength = 1; strength <= 3; strength++) {
            for (int method : new int[] { STORED, DEFLATED }) {
                ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                        .aes("project.xml", projectXml, method, 1, strength)
                        .aes("0.xml", installation, method, 2, strength);
                assertEquals(expected, read(file(writer), PASSWORD), "strength " + strength + ", method " + method);
            }
        }
    }

    @Test
    void readsUnencryptedEntriesNextToEncryptedOnes() throws Exception {
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .plain("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED)
                .aes("0.xml", TestContainer.resource("ets6/0.xml"), STORED, 1, 1);
        assertEquals(unprotected("ets6"), read(file(writer), PASSWORD));
    }

    @Test
    void readsWithAPasswordOutsideAscii() throws Exception {
        String password = "Wächter-€-𝄞";
        Path file = file(ProtectedPartWriter.ets6("ets6", password));

        assertEquals(unprotected("ets6"), read(file, password));
        assertEquals(Reason.WRONG_PASSWORD, fails(file, "Wachter-E-x").getReason());
    }

    @Test
    void wrongPassword() {
        Path file = file(ProtectedPartWriter.ets6("ets6", PASSWORD));

        assertEquals(Reason.WRONG_PASSWORD, fails(file, "made-up Pass 43").getReason());
        assertEquals(Reason.WRONG_PASSWORD, fails(file, PASSWORD.toLowerCase(Locale.ROOT)).getReason());
    }

    @Test
    void noPassword() {
        Path file = file(ProtectedPartWriter.ets6("ets6", PASSWORD));

        assertEquals(Reason.PASSWORD_REQUIRED, fails(file, null).getReason());
        assertEquals(Reason.PASSWORD_REQUIRED, fails(file, "").getReason());
    }

    @Test
    void flippedBitInTheCipherText() {
        for (String entry : List.of("0.xml", "project.xml")) {
            ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
            byte[] inner = writer.build();
            ProtectedPartWriter.Layout layout = writer.layout(entry);
            for (int offset : new int[] { layout.cipherTextOffset(),
                    layout.cipherTextOffset() + layout.cipherTextLength() - 1 }) {
                assertEquals(Reason.MALFORMED, fails(file(flipBit(inner, offset)), PASSWORD).getReason(), entry);
            }
        }
    }

    @Test
    void flippedBitInTheAuthenticationCode() {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
        byte[] inner = writer.build();
        int code = writer.layout("0.xml").authenticationCodeOffset();

        assertEquals(Reason.MALFORMED, fails(file(flipBit(inner, code)), PASSWORD).getReason());
        assertEquals(Reason.MALFORMED, fails(file(flipBit(inner, code + 9)), PASSWORD).getReason());
    }

    @Test
    void flippedBitInTheSalt() {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
        byte[] inner = writer.build();

        assertEquals(Reason.MALFORMED,
                fails(file(flipBit(inner, writer.layout("project.xml").dataOffset())), PASSWORD).getReason());
    }

    @Test
    void entriesWithDifferentPasswordsAreMalformedNotAWrongPassword() {
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .aes("0.xml", TestContainer.resource("ets6/0.xml"), DEFLATED, 1, 3)
                .aesWithPassword("project.xml", TestContainer.resource("ets6/project.xml"), "another made-up one");
        Path file = file(writer);

        assertEquals(Reason.MALFORMED, fails(file, PASSWORD).getReason());
        assertEquals(Reason.MALFORMED, fails(file, "another made-up one").getReason());
        assertEquals(Reason.WRONG_PASSWORD, fails(file, "neither").getReason());
    }

    @Test
    void aWrongPasswordThatPassesAVerifierButFailsTheAuthenticationCodeIsAWrongPassword() {
        String wrong = "a wrong made-up one";
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .aesWithVerifierOf("0.xml", TestContainer.resource("ets6/0.xml"), wrong)
                .aes("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED, 1, 3);
        Path file = file(writer);

        // the verifier of 0.xml matches, its code does not, and project.xml fails its verifier
        assertEquals(Reason.WRONG_PASSWORD, fails(file, wrong).getReason());
        assertEquals(Reason.MALFORMED, fails(file, PASSWORD).getReason());
    }

    @Test
    void aPartWhoseOnlyEncryptedEntryFailsItsCodeWithTheRightPasswordIsAWrongPassword() {
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .plain("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED)
                .aes("0.xml", TestContainer.resource("ets6/0.xml"), STORED, 1, 1);
        byte[] inner = writer.build();
        byte[] damaged = flipBit(inner, writer.layout("0.xml").authenticationCodeOffset());

        // the price of the rule: no entry passes both checks, so nothing tells damage from a wrong password
        assertEquals(Reason.WRONG_PASSWORD, fails(file(damaged), PASSWORD).getReason());
    }

    @Test
    void damageInAnEntryThatIsNotReadIsIgnored() throws Exception {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD).aes("BinaryData/made-up.bin",
                new byte[100], STORED, 1, 3);
        byte[] inner = writer.build();
        byte[] damaged = flipBit(inner, writer.layout("BinaryData/made-up.bin").cipherTextOffset() + 3);

        assertEquals(unprotected("ets6"), read(file(damaged), PASSWORD));
    }

    @Test
    void truncatedArchive() {
        byte[] inner = ProtectedPartWriter.ets6("ets6", PASSWORD).build();
        for (int length : new int[] { 0, 3, 30, inner.length / 3, inner.length / 2, inner.length - 23,
                inner.length - 1 }) {
            assertEquals(Reason.MALFORMED, fails(file(Arrays.copyOf(inner, length)), PASSWORD).getReason(),
                    "cut at " + length);
            assertEquals(Reason.MALFORMED, fails(file(Arrays.copyOf(inner, length)), null).getReason(),
                    "cut at " + length);
        }
    }

    @Test
    void entryReachingPastItsArchive() {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
        byte[] inner = writer.build();
        int central = centralDirectoryEntry(inner, "0.xml");
        ByteBuffer buffer = ByteBuffer.wrap(inner).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(central + 20, buffer.getInt(central + 20) + 1000);

        assertEquals(Reason.MALFORMED, fails(file(inner), PASSWORD).getReason());
    }

    @Test
    void notAZipArchiveInside() {
        assertEquals(Reason.MALFORMED,
                fails(file("no archive here".getBytes(StandardCharsets.US_ASCII)), PASSWORD).getReason());
    }

    @Test
    void crcIsCheckedForAe1AndNotForAe2() throws Exception {
        String installation = TestContainer.resource("ets6/0.xml");
        String projectXml = TestContainer.resource("ets6/project.xml");

        ProtectedPartWriter wrongCrc = new ProtectedPartWriter(PASSWORD).aes("project.xml", projectXml, DEFLATED, 1, 3)
                .aesWithCrc("0.xml", installation, 1, 0x12345678L);
        assertEquals(Reason.MALFORMED, fails(file(wrongCrc), PASSWORD).getReason());

        ProtectedPartWriter ae2 = new ProtectedPartWriter(PASSWORD).aes("project.xml", projectXml, DEFLATED, 2, 3)
                .aes("0.xml", installation, DEFLATED, 2, 3);
        assertEquals(unprotected("ets6"), read(file(ae2), PASSWORD));
    }

    @Test
    void ets4And5ProtectionIsNotSupportedYet() {
        Path file = file(new ProtectedPartWriter(PASSWORD).traditional("0.xml").traditional("project.xml"));

        for (String password : new @Nullable String[] { null, PASSWORD }) {
            KnxProjectException e = fails(file, password);
            assertEquals(Reason.UNSUPPORTED_PROTECTION, e.getReason());
            assertTrue(String.valueOf(e.getMessage()).contains("not supported yet"), e.getMessage());
        }
    }

    @Test
    void unknownAesVersionIsNotSupported() {
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .aes("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED, 3, 3)
                .aes("0.xml", TestContainer.resource("ets6/0.xml"), DEFLATED, 1, 3);
        assertEquals(Reason.UNSUPPORTED_PROTECTION, fails(file(writer), PASSWORD).getReason());
    }

    @Test
    void missingFilesInTheProtectedPart() {
        ProtectedPartWriter noInstallation = new ProtectedPartWriter(PASSWORD).aes("project.xml",
                TestContainer.resource("ets6/project.xml"), DEFLATED, 1, 3);
        assertEquals(Reason.MALFORMED, fails(file(noInstallation), PASSWORD).getReason());

        ProtectedPartWriter noProject = new ProtectedPartWriter(PASSWORD).aes("0.xml",
                TestContainer.resource("ets6/0.xml"), DEFLATED, 1, 3);
        assertEquals(Reason.MALFORMED, fails(file(noProject), PASSWORD).getReason());
    }

    @Test
    void innerEntryOverTheLimit() throws Exception {
        Path file = file(ProtectedPartWriter.ets6("ets6", PASSWORD));
        int installationSize = TestContainer.resource("ets6/0.xml").getBytes(StandardCharsets.UTF_8).length;

        KnxProjectReader tight = new KnxProjectReader(
                new ReadLimits(100, installationSize - 1, LARGE, 100, 1 << 20, 1 << 20));
        assertEquals(Reason.LIMIT_EXCEEDED, fails(tight, file, PASSWORD).getReason());
        KnxProjectReader enough = new KnxProjectReader(
                new ReadLimits(100, installationSize, LARGE, 100, 1 << 20, 1 << 20));
        assertEquals(3, enough.read(file, PASSWORD.toCharArray()).groupAddresses().size());
    }

    @Test
    void tooManyInnerEntries() throws Exception {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
        for (int i = 0; i < 6; i++) {
            writer.plain("BinaryData/" + i + ".bin", "x", STORED);
        }
        Path file = file(writer);

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(7, LARGE, LARGE, 100, 1 << 20, 1 << 20)), file, PASSWORD)
                        .getReason());
        assertEquals(3, new KnxProjectReader(new ReadLimits(8, LARGE, LARGE, 100, 1 << 20, 1 << 20))
                .read(file, PASSWORD.toCharArray()).groupAddresses().size());
    }

    @Test
    void protectedPartOverTheLimit() throws Exception {
        byte[] inner = ProtectedPartWriter.ets6("ets6", PASSWORD).build();
        Path file = file(inner);

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, inner.length - 1)), file,
                        PASSWORD).getReason());
        assertEquals(3, new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, inner.length))
                .read(file, PASSWORD.toCharArray()).groupAddresses().size());
    }

    @Test
    void innerBytesCountTowardsTheTotal() throws Exception {
        byte[] inner = ProtectedPartWriter.ets6("ets6", PASSWORD).build();
        Path file = file(inner);
        long needed = inner.length + TestContainer.resource("ets6/0.xml").getBytes(StandardCharsets.UTF_8).length
                + TestContainer.resource("ets6/project.xml").getBytes(StandardCharsets.UTF_8).length;

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, needed - 1, 100, 1 << 20, 1 << 20)), file,
                        PASSWORD).getReason());
        assertEquals(3, new KnxProjectReader(new ReadLimits(100, LARGE, needed, 100, 1 << 20, 1 << 20))
                .read(file, PASSWORD.toCharArray()).groupAddresses().size());
    }

    @Test
    void innerCompressionRatioTooHigh() throws Exception {
        String installation = TestContainer.resource("ets6/0.xml").replace("<Installations>",
                "<!--" + " ".repeat(200_000) + "--><Installations>");
        ProtectedPartWriter writer = new ProtectedPartWriter(PASSWORD)
                .aes("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED, 1, 3)
                .aes("0.xml", installation, DEFLATED, 1, 3);
        Path file = file(writer);

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 20, 4_096, 1 << 20)), file, PASSWORD)
                        .getReason());
        assertEquals(3, new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 20, 1 << 20, 1 << 20))
                .read(file, PASSWORD.toCharArray()).groupAddresses().size());
    }

    @Test
    void innerRatioIsNotTakenFromTheCompressedSizeTheZipClaims() {
        String installation = TestContainer.resource("ets6/0.xml").replace("<Installations>",
                "<!--" + " ".repeat(200_000) + "--><Installations>");
        byte[] inner = new ProtectedPartWriter(PASSWORD)
                .plain("project.xml", TestContainer.resource("ets6/project.xml"), DEFLATED)
                .plain("0.xml", installation, DEFLATED).plain("filler.bin", TestContainer.filler(100_000), STORED)
                .build();
        byte[] claimed = TestContainer.inflateClaimedCompressedSize(inner, "0.xml", 100_000);

        assertEquals(Reason.MALFORMED,
                fails(new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 20, 4_096, 1 << 20)), file(claimed), null)
                        .getReason());
    }

    private void unlockAndCollect(byte[] inner, byte[] zipPassword, List<byte[]> secrets) throws Exception {
        List<InnerArchive.Entry> entries = InnerArchive.entries(inner, PART + ".zip",
                new ReadBudget(ReadLimits.DEFAULT));
        try {
            ProtectedProjectPart.unlock(inner, PART + ".zip", entries, zipPassword, new HashMap<>(), secrets::add);
        } finally {
            assertTrue(isZero(zipPassword), "zip password");
            assertFalse(secrets.isEmpty());
            for (byte[] secret : secrets) {
                assertTrue(isZero(secret), "derived key material");
            }
        }
    }

    private static boolean isZero(byte[] array) {
        for (byte b : array) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    @Test
    void passwordAndDerivedKeysAreOverwrittenAfterARightPassword() throws Exception {
        byte[] inner = ProtectedPartWriter.ets6("ets6", PASSWORD).build();
        List<byte[]> secrets = new ArrayList<>();

        unlockAndCollect(inner, WinZipAes.etsZipPassword(PASSWORD.toCharArray()), secrets);

        assertEquals(4, secrets.size());
    }

    @Test
    void passwordAndDerivedKeysAreOverwrittenAfterAWrongPassword() {
        byte[] inner = ProtectedPartWriter.ets6("ets6", PASSWORD).build();
        List<byte[]> secrets = new ArrayList<>();

        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> unlockAndCollect(inner, WinZipAes.etsZipPassword("neither".toCharArray()), secrets));

        assertEquals(Reason.WRONG_PASSWORD, e.getReason());
    }

    @Test
    void passwordAndDerivedKeysAreOverwrittenAfterDamage() {
        ProtectedPartWriter writer = ProtectedPartWriter.ets6("ets6", PASSWORD);
        byte[] inner = writer.build();
        byte[] damaged = flipBit(inner, writer.layout("0.xml").cipherTextOffset() + 3);
        List<byte[]> secrets = new ArrayList<>();

        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> unlockAndCollect(damaged, WinZipAes.etsZipPassword(PASSWORD.toCharArray()), secrets));

        assertEquals(Reason.MALFORMED, e.getReason());
    }

    @Test
    void protectedReadsInParallel() throws Exception {
        Path file = file(ProtectedPartWriter.ets6("ets6", PASSWORD));
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(executor.submit(() -> reader.read(file, PASSWORD.toCharArray()).groupAddresses().size()));
            }
            for (Future<Integer> result : results) {
                assertEquals(3, result.get(120, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static int centralDirectoryEntry(byte[] zip, String name) {
        ByteBuffer buffer = ByteBuffer.wrap(zip).order(ByteOrder.LITTLE_ENDIAN);
        byte[] wanted = name.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i + 46 <= zip.length; i++) {
            if (buffer.getInt(i) == 0x02014b50 && (buffer.getShort(i + 28) & 0xFFFF) == wanted.length
                    && Arrays.equals(zip, i + 46, i + 46 + wanted.length, wanted, 0, wanted.length)) {
                return i;
            }
        }
        throw new IllegalStateException("no central directory entry " + name);
    }
}
