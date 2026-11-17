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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * The central directory of the nested, protected archive, parsed by hand from memory because the JDK's zip
 * classes do not read WinZip AES entries (format note §2.3).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class InnerArchive {

    static final int STORED = 0;
    static final int DEFLATED = 8;

    private static final int END_SIGNATURE = 0x06054b50;
    private static final int CENTRAL_SIGNATURE = 0x02014b50;
    private static final int LOCAL_SIGNATURE = 0x04034b50;
    private static final int END_LENGTH = 22;
    private static final int CENTRAL_LENGTH = 46;
    private static final int LOCAL_LENGTH = 30;
    private static final int AES_METHOD = 99;
    private static final int AES_EXTRA = 0x9901;
    private static final int FLAG_ENCRYPTED = 1;
    private static final int FLAG_STRONG_ENCRYPTION = 1 << 6;
    private static final int FLAG_UTF8 = 1 << 11;

    /**
     * The WinZip AES fields of an entry.
     *
     * @param version 1 = AE-1 (CRC kept), 2 = AE-2 (CRC not used)
     * @param strength 1, 2, 3 = AES-128, 192, 256
     * @param method the compression method under the encryption
     */
    record Aes(int version, int strength, int method) {

        int keyLength() {
            return 8 + 8 * strength;
        }

        int saltLength() {
            return keyLength() / 2;
        }
    }

    /**
     * One entry; {@code dataOffset} and {@code compressedSize} give the entry's data in the archive.
     */
    record Entry(String name, int method, long crc, int compressedSize, long size, int dataOffset, @Nullable Aes aes) {

        boolean isDirectory() {
            return name.endsWith("/");
        }
    }

    private InnerArchive() {
    }

    /**
     * @throws KnxProjectException MALFORMED for a damaged archive, UNSUPPORTED_PROTECTION for an entry with the
     *             ETS 4/5 protection or an unknown kind of encryption, LIMIT_EXCEEDED for too many entries
     */
    static List<Entry> entries(byte[] zip, String archive, ReadBudget budget) throws KnxProjectException {
        int end = findEnd(zip);
        if (end < 0) {
            throw malformed(archive, "is not a zip archive or is cut short");
        }
        int count = u16(zip, end + 10);
        long directorySize = u32(zip, end + 12);
        long directoryOffset = u32(zip, end + 16);
        if (u16(zip, end + 4) != 0 || u16(zip, end + 6) != 0 || u16(zip, end + 8) != count) {
            throw malformed(archive, "spans several disks");
        }
        if (directoryOffset + directorySize > end) {
            throw malformed(archive, "has its central directory outside the archive");
        }
        budget.checkEntryCount(count);
        List<Entry> entries = new ArrayList<>(count);
        long[] localOffsets = new long[count];
        int position = (int) directoryOffset;
        int directoryEnd = (int) (directoryOffset + directorySize);
        for (int i = 0; i < count; i++) {
            if (position + CENTRAL_LENGTH > directoryEnd || u32(zip, position) != CENTRAL_SIGNATURE) {
                throw malformed(archive, "has a damaged central directory");
            }
            int flags = u16(zip, position + 8);
            int method = u16(zip, position + 10);
            long crc = u32(zip, position + 16);
            long compressedSize = u32(zip, position + 20);
            long size = u32(zip, position + 24);
            int nameLength = u16(zip, position + 28);
            int extraLength = u16(zip, position + 30);
            int commentLength = u16(zip, position + 32);
            long localOffset = u32(zip, position + 42);
            int nameStart = position + CENTRAL_LENGTH;
            int next = nameStart + nameLength + extraLength + commentLength;
            if (next > directoryEnd) {
                throw malformed(archive, "has a damaged central directory");
            }
            String name = new String(zip, nameStart, nameLength,
                    (flags & FLAG_UTF8) != 0 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1);
            Aes aes = kind(zip, flags, method, nameStart + nameLength, extraLength, name, archive);
            int dataOffset = dataOffset(zip, localOffset, compressedSize, directoryOffset, name, archive);
            if (aes != null && compressedSize < aes.saltLength() + WinZipAes.VERIFIER_LENGTH
                    + WinZipAes.AUTHENTICATION_CODE_LENGTH) {
                throw malformed(archive, "entry " + name + " is too short for its encryption");
            }
            entries.add(new Entry(name, method, crc, (int) compressedSize, size, dataOffset, aes));
            localOffsets[i] = localOffset;
            position = next;
        }
        checkRanges(entries, localOffsets, directoryOffset, archive);
        return entries;
    }

    /**
     * The ratio limit divides by the compressed size an entry states, so a stated range that runs on into the
     * next entry would hide a high ratio. No honest archive has overlapping entries.
     */
    private static void checkRanges(List<Entry> entries, long[] localOffsets, long directoryOffset, String archive)
            throws KnxProjectException {
        long[] sorted = localOffsets.clone();
        Arrays.sort(sorted);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int following = Arrays.binarySearch(sorted, localOffsets[i]);
            while (following < sorted.length && sorted[following] <= localOffsets[i]) {
                following++;
            }
            long limit = following < sorted.length ? sorted[following] : directoryOffset;
            if ((long) entry.dataOffset() + entry.compressedSize() > limit) {
                throw malformed(archive, "entry " + entry.name() + " overlaps the entry after it");
            }
        }
    }

    /** The AES fields, or null for an entry that is not encrypted. */
    private static @Nullable Aes kind(byte[] zip, int flags, int method, int extraStart, int extraLength, String name,
            String archive) throws KnxProjectException {
        boolean encrypted = (flags & FLAG_ENCRYPTED) != 0;
        if (encrypted && method != AES_METHOD) {
            throw new KnxProjectException(Reason.UNSUPPORTED_PROTECTION,
                    "the protection of ETS 4 and ETS 5 projects is not supported yet");
        }
        if ((flags & FLAG_STRONG_ENCRYPTION) != 0) {
            throw new KnxProjectException(Reason.UNSUPPORTED_PROTECTION,
                    archive + " uses a kind of encryption that is not supported");
        }
        if (!encrypted) {
            if (method == AES_METHOD) {
                throw malformed(archive, "entry " + name + " is marked AES but not encrypted");
            }
            checkMethod(method, name, archive);
            return null;
        }
        int position = extraStart;
        int extraEnd = extraStart + extraLength;
        while (position + 4 <= extraEnd) {
            int id = u16(zip, position);
            int length = u16(zip, position + 2);
            if (position + 4 + length > extraEnd) {
                break;
            }
            if (id == AES_EXTRA) {
                if (length != 7) {
                    throw malformed(archive, "entry " + name + " has a damaged AES field");
                }
                int version = u16(zip, position + 4);
                int strength = zip[position + 8] & 0xFF;
                if ((version != 1 && version != 2) || zip[position + 6] != 'A' || zip[position + 7] != 'E'
                        || strength < 1 || strength > 3) {
                    throw new KnxProjectException(Reason.UNSUPPORTED_PROTECTION,
                            archive + " uses a kind of AES encryption that is not supported");
                }
                int realMethod = u16(zip, position + 9);
                checkMethod(realMethod, name, archive);
                return new Aes(version, strength, realMethod);
            }
            position += 4 + length;
        }
        throw malformed(archive, "entry " + name + " is encrypted without an AES field");
    }

    private static void checkMethod(int method, String name, String archive) throws KnxProjectException {
        if (method != STORED && method != DEFLATED) {
            throw malformed(archive, "entry " + name + " uses a compression method other than stored or deflated");
        }
    }

    private static int dataOffset(byte[] zip, long localOffset, long compressedSize, long directoryOffset, String name,
            String archive) throws KnxProjectException {
        if (localOffset + LOCAL_LENGTH > directoryOffset || u32(zip, (int) localOffset) != LOCAL_SIGNATURE) {
            throw malformed(archive, "entry " + name + " has a damaged local header");
        }
        long dataOffset = localOffset + LOCAL_LENGTH + u16(zip, (int) localOffset + 26)
                + u16(zip, (int) localOffset + 28);
        if (dataOffset + compressedSize > directoryOffset) {
            throw malformed(archive, "entry " + name + " reaches past its data");
        }
        return (int) dataOffset;
    }

    private static int findEnd(byte[] zip) {
        for (int i = zip.length - END_LENGTH; i >= 0 && i >= zip.length - END_LENGTH - 0xFFFF; i--) {
            if (u32(zip, i) == END_SIGNATURE && i + END_LENGTH + u16(zip, i + 20) == zip.length) {
                return i;
            }
        }
        return -1;
    }

    private static KnxProjectException malformed(String archive, String what) {
        return new KnxProjectException(Reason.MALFORMED, archive + " " + what);
    }

    private static int u16(byte[] b, int offset) {
        return (b[offset] & 0xFF) | (b[offset + 1] & 0xFF) << 8;
    }

    private static long u32(byte[] b, int offset) {
        return u16(b, offset) | (long) u16(b, offset + 2) << 16;
    }
}
