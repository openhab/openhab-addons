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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests that every limit of {@link ReadLimits} is enforced while reading.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ReadLimitsTest {

    private static final long LARGE = 1L << 30;

    private static KnxProjectException fails(ReadLimits limits, Path file) {
        return assertThrows(KnxProjectException.class, () -> new KnxProjectReader(limits).read(file, null));
    }

    /** The ETS 5 fixture with the installation file padded by a comment of the given number of bytes. */
    private static TestContainer padded(int padding, char filler) {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        StringBuilder pad = new StringBuilder("<!--");
        for (int i = 0; i < padding; i++) {
            pad.append(i % 64 == 63 ? '\n' : filler);
        }
        pad.append("-->");
        container.put("P-1C2D/0.xml",
                container.text("P-1C2D/0.xml").replace("<Installations>", pad + "<Installations>"));
        return container;
    }

    private static int size(TestContainer container, String entry) {
        return container.text(entry).getBytes(StandardCharsets.UTF_8).length;
    }

    @Test
    void defaultsLeaveRoomForTheSizesSeenInRealProjects() {
        ReadLimits limits = ReadLimits.DEFAULT;
        assertTrue(limits.maxEntries() >= 1_000);
        assertTrue(limits.maxEntryBytes() >= 43_000_000L);
        assertTrue(limits.maxTotalBytes() >= 180_000_000L);
        assertTrue(limits.maxRatio() >= 18);
        assertTrue(limits.maxProgramEntries() >= 100_000);
    }

    @Test
    void theSixLimitsOfTheArchiveKeepTheDefaultsOfTheOthers() {
        ReadLimits limits = new ReadLimits(1, 2, 3, 4, 5, 6);

        assertEquals(ReadLimits.MAX_PROGRAM_ENTRIES, limits.maxProgramEntries());
        assertEquals(new ReadLimits(1, 2, 3, 4, 5, 6, ReadLimits.MAX_PROGRAM_ENTRIES), limits);
    }

    @Test
    void tooManyEntries() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        container.put("M-0083/a.xml", "a").put("M-0083/b.xml", "b");
        Path file = container.write();
        ReadLimits limits = new ReadLimits(6, LARGE, LARGE, 100, 1 << 20, 1 << 20);

        assertEquals(Reason.LIMIT_EXCEEDED, fails(limits, file).getReason());
        assertEquals(4, new KnxProjectReader(new ReadLimits(7, LARGE, LARGE, 100, 1 << 20, 1 << 20)).read(file, null)
                .groupAddresses().size());
    }

    @Test
    void entryTooLarge() {
        Path file = padded(20_000, 'x').write();
        ReadLimits limits = new ReadLimits(100, 8_000, LARGE, 100, 1 << 20, 1 << 20);

        assertEquals(Reason.LIMIT_EXCEEDED, fails(limits, file).getReason());
    }

    @Test
    void entryLargerThanTheZipClaims() {
        TestContainer container = padded(20_000, 'x');
        Path file = TestContainer.write(TestContainer.claimUncompressedSize(container.zip(), "P-1C2D/0.xml", 100));
        ReadLimits limits = new ReadLimits(100, 8_000, LARGE, 100, 1 << 20, 1 << 20);

        assertEquals(Reason.LIMIT_EXCEEDED, fails(limits, file).getReason());
    }

    @Test
    void tooManyBytesInTotal() throws Exception {
        TestContainer container = TestContainer.of("ets5", "P-1C2D");
        Path file = container.write();
        long needed = size(container, "P-1C2D/project.xml") + size(container, "P-1C2D/0.xml");

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new ReadLimits(100, LARGE, needed - 1, 100, 1 << 20, 1 << 20), file).getReason());
        assertEquals(4, new KnxProjectReader(new ReadLimits(100, LARGE, needed, 100, 1 << 20, 1 << 20)).read(file, null)
                .groupAddresses().size());
    }

    @Test
    void compressionRatioTooHigh() throws Exception {
        Path file = padded(200_000, ' ').write();

        assertEquals(Reason.LIMIT_EXCEEDED,
                fails(new ReadLimits(100, LARGE, LARGE, 20, 4_096, 1 << 20), file).getReason());
        assertEquals(4, new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 20, 1 << 20, 1 << 20)).read(file, null)
                .groupAddresses().size());
    }

    @Test
    void ratioIsNotTakenFromTheCompressedSizeTheZipClaims() {
        TestContainer container = padded(200_000, ' ');
        container.put("M-00FA/filler.bin", TestContainer.filler(100_000));
        byte[] zip = TestContainer.inflateClaimedCompressedSize(container.zip(), "P-1C2D/0.xml", 100_000);
        ReadLimits limits = new ReadLimits(100, LARGE, LARGE, 20, 4_096, 1 << 20);

        assertEquals(Reason.MALFORMED, fails(limits, TestContainer.write(zip)).getReason());
    }

    @Test
    void overlappingEntriesAreRefusedWhereTheCentralDirectoryHasAZip64EndRecord() throws Exception {
        TestContainer container = padded(200_000, ' ');
        container.put("M-00FA/filler.bin", TestContainer.filler(100_000));
        ReadLimits limits = new ReadLimits(100, LARGE, LARGE, 20, 1 << 20, 1 << 20);

        assertEquals(4,
                new KnxProjectReader(limits)
                        .read(TestContainer.write(TestContainer.withZip64EndRecord(container.zip())), null)
                        .groupAddresses().size());
        byte[] claimed = TestContainer.inflateClaimedCompressedSize(container.zip(), "P-1C2D/0.xml", 100_000);
        assertEquals(Reason.MALFORMED,
                fails(limits, TestContainer.write(TestContainer.withZip64EndRecord(claimed))).getReason());
    }
}
