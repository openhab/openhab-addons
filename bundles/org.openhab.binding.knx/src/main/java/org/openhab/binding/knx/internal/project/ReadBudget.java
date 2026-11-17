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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Counts what one read decompresses and stops it at the {@link ReadLimits}. One instance per read; not
 * thread-safe.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ReadBudget {

    private final ReadLimits limits;
    private long totalBytes;
    private long records;
    private @Nullable String exceeded;

    ReadBudget(ReadLimits limits) {
        this.limits = limits;
    }

    ReadLimits limits() {
        return limits;
    }

    void checkEntryCount(int entries) throws KnxProjectException {
        if (entries > limits.maxEntries()) {
            throw new KnxProjectException(Reason.LIMIT_EXCEEDED,
                    "the archive has more than " + limits.maxEntries() + " entries");
        }
    }

    /** Counts one record the read creates or keeps; over the limit the read stops. */
    void countRecord() throws KnxProjectException {
        countRecords(1);
    }

    void countRecords(int count) throws KnxProjectException {
        records += count;
        if (records > limits.maxRecords()) {
            throw new KnxProjectException(Reason.LIMIT_EXCEEDED,
                    "the project needs more than " + limits.maxRecords() + " records");
        }
    }

    /** Notes the limit a parser is stopped for, to be reported by {@link #throwIfExceeded()}. */
    void exceed(String message) {
        exceeded = message;
    }

    /**
     * Wraps the decompressed stream of one entry.
     *
     * @param compressedSize the bytes the entry takes in the archive; for the ratio check
     */
    InputStream limit(InputStream in, String entryName, long compressedSize) {
        return new LimitedInputStream(in, entryName, Math.max(compressedSize, 1));
    }

    /**
     * A parser wraps the I/O errors of its input, so after any failure the caller asks here whether a limit
     * was the cause.
     */
    void throwIfExceeded() throws KnxProjectException {
        String message = exceeded;
        if (message != null) {
            throw new KnxProjectException(Reason.LIMIT_EXCEEDED, message);
        }
    }

    private final class LimitedInputStream extends InputStream {

        private final InputStream in;
        private final String entryName;
        private final long compressedSize;
        private long entryBytes;

        LimitedInputStream(InputStream in, String entryName, long compressedSize) {
            this.in = in;
            this.entryName = entryName;
            this.compressedSize = compressedSize;
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b >= 0) {
                count(1);
            }
            return b;
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) throws IOException {
            int n = in.read(b, off, len);
            if (n > 0) {
                count(n);
            }
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = in.skip(n);
            if (skipped > 0) {
                count(skipped);
            }
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        private void count(long n) throws IOException {
            entryBytes += n;
            totalBytes += n;
            String message = null;
            if (entryBytes > limits.maxEntryBytes()) {
                message = "entry " + entryName + " is larger than " + limits.maxEntryBytes() + " bytes";
            } else if (totalBytes > limits.maxTotalBytes()) {
                message = "the project needs more than " + limits.maxTotalBytes() + " bytes";
            } else if (entryBytes > limits.ratioGraceBytes() && entryBytes / compressedSize > limits.maxRatio()) {
                message = "entry " + entryName + " is compressed more than " + limits.maxRatio() + " to 1";
            }
            if (message != null) {
                exceeded = message;
                throw new IOException(message);
            }
        }
    }
}
