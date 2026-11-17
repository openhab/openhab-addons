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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Where the entries of the outer archive begin, read from the central directory a second time because the JDK
 * does not tell. The ratio limit divides by the compressed size an entry states; the JDK accepts a stated size
 * that runs on into the entries after it, and then the inflater stops at the real end, so the stated size says
 * nothing about the bytes really consumed. An honest archive has no overlapping entries, so one that has is
 * refused. An entry placed beyond 4 GiB, which the size limits rule out anyway, is refused too.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class EntryRanges {

    private static final int END_SIGNATURE = 0x06054b50;
    private static final int ZIP64_END_SIGNATURE = 0x06064b50;
    private static final int ZIP64_LOCATOR_SIGNATURE = 0x07064b50;
    private static final int CENTRAL_SIGNATURE = 0x02014b50;
    private static final int LOCAL_SIGNATURE = 0x04034b50;
    private static final int END_LENGTH = 22;
    private static final int ZIP64_LOCATOR_LENGTH = 20;
    private static final int ZIP64_END_LENGTH = 56;
    private static final int CENTRAL_LENGTH = 46;
    private static final int LOCAL_LENGTH = 30;
    private static final long MAX_32 = 0xFFFFFFFFL;
    private static final int FLAG_UTF8 = 1 << 11;

    /** Local header offset and the offset where the next entry (or the central directory) begins. */
    private record Span(long localOffset, long limit) {
    }

    private final Path file;
    private final Map<String, Span> spans;
    private final Set<String> duplicates;

    private EntryRanges(Path file, Map<String, Span> spans, Set<String> duplicates) {
        this.file = file;
        this.spans = spans;
        this.duplicates = duplicates;
    }

    static EntryRanges read(Path file, int entries) throws IOException, KnxProjectException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            long directoryOffset = directoryOffset(channel);
            Map<String, Long> offsets = new HashMap<>();
            Set<String> duplicates = new HashSet<>();
            channel.position(directoryOffset);
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(Channels.newInputStream(channel)))) {
                for (int i = 0; i < entries; i++) {
                    readRecord(in, directoryOffset, offsets, duplicates);
                }
            }
            TreeSet<Long> sorted = new TreeSet<>(offsets.values());
            Map<String, Span> spans = new HashMap<>();
            offsets.forEach((name, offset) -> {
                Iterator<Long> following = sorted.tailSet(offset, false).iterator();
                spans.put(name, new Span(offset, following.hasNext() ? following.next() : directoryOffset));
            });
            return new EntryRanges(file, spans, duplicates);
        } catch (EOFException e) {
            throw damaged("is cut short");
        }
    }

    private static long directoryOffset(FileChannel channel) throws IOException, KnxProjectException {
        long length = channel.size();
        int tail = (int) Math.min(length, END_LENGTH + 0xFFFFL);
        ByteBuffer buffer = fill(channel, length - tail, tail);
        for (int i = tail - END_LENGTH; i >= 0; i--) {
            if (buffer.getInt(i) != END_SIGNATURE || i + END_LENGTH + u16(buffer, i + 20) != tail) {
                continue;
            }
            long offset = buffer.getInt(i + 16) & MAX_32;
            if (offset == MAX_32) {
                long locator = length - tail + i - ZIP64_LOCATOR_LENGTH;
                ByteBuffer pointer = fill(channel, locator, ZIP64_LOCATOR_LENGTH);
                if (pointer.getInt(0) != ZIP64_LOCATOR_SIGNATURE) {
                    throw damaged("has no zip64 end record");
                }
                ByteBuffer end = fill(channel, pointer.getLong(8), ZIP64_END_LENGTH);
                if (end.getInt(0) != ZIP64_END_SIGNATURE) {
                    throw damaged("has no zip64 end record");
                }
                offset = end.getLong(48);
            }
            if (offset < 0 || offset > length) {
                throw damaged("has its central directory outside the file");
            }
            return offset;
        }
        throw damaged("has no end record");
    }

    private static void readRecord(DataInputStream in, long directoryOffset, Map<String, Long> offsets,
            Set<String> duplicates) throws IOException, KnxProjectException {
        ByteBuffer header = ByteBuffer.wrap(in.readNBytes(CENTRAL_LENGTH)).order(ByteOrder.LITTLE_ENDIAN);
        if (header.remaining() < CENTRAL_LENGTH || header.getInt(0) != CENTRAL_SIGNATURE) {
            throw damaged("has a damaged central directory");
        }
        int flags = u16(header, 8);
        int nameLength = u16(header, 28);
        int extraLength = u16(header, 30);
        int commentLength = u16(header, 32);
        long offset = header.getInt(42) & MAX_32;
        byte[] nameBytes = in.readNBytes(nameLength);
        in.skipNBytes(extraLength + (long) commentLength);
        if (nameBytes.length < nameLength) {
            throw new EOFException();
        }
        if (offset >= directoryOffset) {
            throw damaged("has an entry outside the file");
        }
        String name = new String(nameBytes,
                (flags & FLAG_UTF8) != 0 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1);
        if (offsets.put(name, offset) != null) {
            duplicates.add(name);
        }
    }

    /**
     * @throws KnxProjectException MALFORMED when the entry's stated compressed size reaches past where the next
     *             entry begins
     */
    void check(ZipEntry entry) throws IOException, KnxProjectException {
        String name = entry.getName();
        Span span = spans.get(name);
        if (span == null || duplicates.contains(name)) {
            throw new KnxProjectException(Reason.MALFORMED, "entry " + name + " cannot be placed in the archive");
        }
        long compressed = entry.getCompressedSize();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer local = fill(channel, span.localOffset(), LOCAL_LENGTH);
            if (local.getInt(0) != LOCAL_SIGNATURE) {
                throw new KnxProjectException(Reason.MALFORMED, "entry " + name + " has a damaged local header");
            }
            long dataOffset = span.localOffset() + LOCAL_LENGTH + u16(local, 26) + u16(local, 28);
            if (compressed < 0 || dataOffset + compressed > span.limit()) {
                throw new KnxProjectException(Reason.MALFORMED, "entry " + name + " overlaps the entry after it");
            }
        } catch (EOFException e) {
            throw new KnxProjectException(Reason.MALFORMED, "entry " + name + " has a damaged local header");
        }
    }

    private static ByteBuffer fill(FileChannel channel, long position, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        long at = position;
        while (buffer.hasRemaining()) {
            int n = channel.read(buffer, at);
            if (n < 0) {
                throw new EOFException();
            }
            at += n;
        }
        return buffer;
    }

    private static int u16(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset) & 0xFFFF;
    }

    private static KnxProjectException damaged(String what) {
        return new KnxProjectException(Reason.MALFORMED, "the archive " + what);
    }
}
