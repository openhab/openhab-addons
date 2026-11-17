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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The limits a read of a project file is held to. They are counted on the bytes actually decompressed, never
 * taken from the sizes the zip archive states.
 *
 * @param maxEntries entries in one archive
 * @param maxEntryBytes decompressed bytes of one entry
 * @param maxTotalBytes decompressed bytes of all entries read during one read
 * @param maxRatio decompressed bytes per compressed byte of one entry
 * @param ratioGraceBytes decompressed bytes of an entry before the ratio is checked, since small entries
 *            compress far better than large ones
 * @param maxProtectedPartBytes bytes of a protected project part, which is held in memory while it is read
 * @param maxProgramEntries entries kept from one application program file while it is read
 * @param maxRecords records one read creates: every record of the model and every entry kept from a
 *            manufacturer file, all passes together
 * @param maxDepth nesting depth of the elements of every XML file read, also inside parts that are skipped
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
record ReadLimits(int maxEntries, long maxEntryBytes, long maxTotalBytes, int maxRatio, long ratioGraceBytes,
        int maxProtectedPartBytes, int maxProgramEntries, int maxRecords, int maxDepth) {

    // Real projects: 141 entries, the largest entry 43 MB, 178 MB in total, about 18:1, a protected part of
    // 189 KB (format note §1, §3). One program file holds at most 54,374 ParameterRef and 32,001 ComObjectRef
    // (§3); an import keeps only the few hundred it needs. A real project's model is about 5,000 records (§5.1),
    // so the record limit is about 100 times that; the real nesting of a project is a few dozen levels.
    static final int MAX_ENTRIES = 5_000;
    static final long MAX_ENTRY_BYTES = 256L * 1024 * 1024;
    static final long MAX_TOTAL_BYTES = 1024L * 1024 * 1024;
    static final int MAX_RATIO = 100;
    static final long RATIO_GRACE_BYTES = 1024L * 1024;
    static final int MAX_PROTECTED_PART_BYTES = 64 * 1024 * 1024;
    static final int MAX_PROGRAM_ENTRIES = 500_000;
    static final int MAX_RECORDS = 500_000;
    static final int MAX_DEPTH = 512;

    static final ReadLimits DEFAULT = new ReadLimits(MAX_ENTRIES, MAX_ENTRY_BYTES, MAX_TOTAL_BYTES, MAX_RATIO,
            RATIO_GRACE_BYTES, MAX_PROTECTED_PART_BYTES, MAX_PROGRAM_ENTRIES, MAX_RECORDS, MAX_DEPTH);

    /** The limits of the archive and of the program files; the others keep their defaults. */
    ReadLimits(int maxEntries, long maxEntryBytes, long maxTotalBytes, int maxRatio, long ratioGraceBytes,
            int maxProtectedPartBytes, int maxProgramEntries) {
        this(maxEntries, maxEntryBytes, maxTotalBytes, maxRatio, ratioGraceBytes, maxProtectedPartBytes,
                maxProgramEntries, MAX_RECORDS, MAX_DEPTH);
    }

    /** The limits of the archive; the other limits keep their defaults. */
    ReadLimits(int maxEntries, long maxEntryBytes, long maxTotalBytes, int maxRatio, long ratioGraceBytes,
            int maxProtectedPartBytes) {
        this(maxEntries, maxEntryBytes, maxTotalBytes, maxRatio, ratioGraceBytes, maxProtectedPartBytes,
                MAX_PROGRAM_ENTRIES);
    }
}
