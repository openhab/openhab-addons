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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;

/**
 * Extracts media metadata from WAV files.
 *
 * <p>
 * Implements the subset needed for RAOP streaming: RIFF parsing for the duration (from
 * the {@code fmt } and {@code data} chunks) and ID3v2 tags carried in an {@code id3 } RIFF
 * chunk (frames {@code TIT2} = title, {@code TPE1} = artist, {@code TALB} = album), which
 * is how common taggers store metadata in WAV files.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class WavMetadata {

    /** Do not buffer more than this when scanning past audio data of a stream. */
    private static final long MAX_DATA_SKIP = 8 * 1024 * 1024;

    private WavMetadata() {
    }

    /**
     * Parses metadata from a WAV file.
     *
     * @param file the file to parse
     * @return extracted metadata; empty metadata when the file is not a WAV file
     */
    public static MediaMetadata parse(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return parse(in, Long.MAX_VALUE);
        } catch (IOException e) {
            return RaopStreamClient.EMPTY_METADATA;
        }
    }

    /**
     * Parses metadata from a WAV stream. The stream is consumed; callers wanting to also
     * play the stream must buffer it (e.g. {@code mark}/{@code reset}).
     *
     * @param in the stream to parse
     * @param maxDataSkip maximum number of audio payload bytes to scan past when looking
     *            for trailing tag chunks
     * @return extracted metadata; empty metadata when the stream is not a WAV stream
     */
    public static MediaMetadata parse(InputStream in, long maxDataSkip) {
        try {
            return parseRiff(in, maxDataSkip);
        } catch (IOException | RuntimeException e) {
            return RaopStreamClient.EMPTY_METADATA;
        }
    }

    /** Convenience overload with the default data-skip limit. */
    public static MediaMetadata parse(InputStream in) {
        return parse(in, MAX_DATA_SKIP);
    }

    private static MediaMetadata parseRiff(InputStream in, long maxDataSkip) throws IOException {
        byte[] riff = readExactly(in, 12);
        if (!"RIFF".equals(new String(riff, 0, 4, StandardCharsets.US_ASCII))
                || !"WAVE".equals(new String(riff, 8, 4, StandardCharsets.US_ASCII))) {
            return RaopStreamClient.EMPTY_METADATA;
        }

        int sampleRate = 0;
        int blockAlign = 0;
        long dataSize = -1;
        String title = null;
        String artist = null;
        String album = null;

        while (true) {
            byte[] chunkHeader;
            try {
                chunkHeader = readExactly(in, 8);
            } catch (EOFException e) {
                break;
            }
            String chunkId = new String(chunkHeader, 0, 4, StandardCharsets.US_ASCII);
            long chunkSize = leUint32(chunkHeader, 4);

            if ("fmt ".equals(chunkId) && chunkSize >= 16) {
                byte[] fmt = readExactly(in, (int) chunkSize);
                sampleRate = (int) leUint32(fmt, 4);
                blockAlign = (fmt[12] & 0xFF) | ((fmt[13] & 0xFF) << 8);
            } else if ("data".equals(chunkId)) {
                dataSize = chunkSize;
                if (chunkSize > maxDataSkip) {
                    break; // Too much to scan through; give up on trailing tags
                }
                in.skipNBytes(chunkSize);
            } else if ("id3".equalsIgnoreCase(chunkId.trim()) && chunkSize <= 1024 * 1024) {
                byte[] id3 = readExactly(in, (int) chunkSize);
                @Nullable
                String[] tags = parseId3(id3);
                title = tags[0] != null ? tags[0] : title;
                artist = tags[1] != null ? tags[1] : artist;
                album = tags[2] != null ? tags[2] : album;
            } else {
                if (chunkSize > maxDataSkip) {
                    break;
                }
                in.skipNBytes(chunkSize);
            }
            if ((chunkSize & 1) == 1) {
                in.skipNBytes(1); // Chunks are word aligned
            }
        }

        Duration duration = null;
        if (dataSize > 0 && sampleRate > 0 && blockAlign > 0) {
            duration = Duration.ofMillis(dataSize * 1000L / ((long) sampleRate * blockAlign));
        }
        return new MediaMetadata(title, artist, album, null, duration);
    }

    /** Parses an ID3v2 blob, returning {@code [title, artist, album]}. */
    private static @Nullable String[] parseId3(byte[] data) {
        @Nullable
        String[] result = new String[3];
        if (data.length < 10 || data[0] != 'I' || data[1] != 'D' || data[2] != '3') {
            return result;
        }
        int version = data[3] & 0xFF;
        int size = syncSafe(data, 6);
        int pos = 10;
        int end = Math.min(data.length, 10 + size);
        while (pos + 10 <= end) {
            String frameId = new String(data, pos, 4, StandardCharsets.US_ASCII);
            int frameSize = version >= 4 ? syncSafe(data, pos + 4) : (int) beUint32(data, pos + 4);
            pos += 10;
            if (frameSize <= 0 || pos + frameSize > end) {
                break;
            }
            @Nullable
            String value = decodeTextFrame(data, pos, frameSize);
            switch (frameId) {
                case "TIT2" -> result[0] = value;
                case "TPE1" -> result[1] = value;
                case "TALB" -> result[2] = value;
                default -> {
                    // Not interested in other frames
                }
            }
            pos += frameSize;
        }
        return result;
    }

    private static @Nullable String decodeTextFrame(byte[] data, int offset, int length) {
        if (length < 1) {
            return null;
        }
        int encoding = data[offset] & 0xFF;
        int start = offset + 1;
        int len = length - 1;
        String value = switch (encoding) {
            case 0 -> new String(data, start, len, StandardCharsets.ISO_8859_1);
            case 1 -> new String(data, start, len, StandardCharsets.UTF_16);
            case 2 -> new String(data, start, len, StandardCharsets.UTF_16BE);
            default -> new String(data, start, len, StandardCharsets.UTF_8);
        };
        int nul = value.indexOf('\0');
        if (nul >= 0) {
            value = value.substring(0, nul);
        }
        return value.isEmpty() ? null : value;
    }

    private static int syncSafe(byte[] data, int offset) {
        return ((data[offset] & 0x7F) << 21) | ((data[offset + 1] & 0x7F) << 14) | ((data[offset + 2] & 0x7F) << 7)
                | (data[offset + 3] & 0x7F);
    }

    private static long beUint32(byte[] data, int offset) {
        return ((data[offset] & 0xFFL) << 24) | ((data[offset + 1] & 0xFFL) << 16) | ((data[offset + 2] & 0xFFL) << 8)
                | (data[offset + 3] & 0xFFL);
    }

    private static long leUint32(byte[] data, int offset) {
        return (data[offset] & 0xFFL) | ((data[offset + 1] & 0xFFL) << 8) | ((data[offset + 2] & 0xFFL) << 16)
                | ((data[offset + 3] & 0xFFL) << 24);
    }

    private static byte[] readExactly(InputStream in, int count) throws IOException {
        byte[] data = in.readNBytes(count);
        if (data.length < count) {
            throw new EOFException("unexpected end of stream");
        }
        return data;
    }
}
