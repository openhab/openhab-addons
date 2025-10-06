/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.session;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper class to parse incoming HTTP messages and determine when a complete message has been received.
 * It accumulates header data until the end of headers is detected, then reads the Content-Length header to
 * determine how many bytes of content to expect. It tracks the number of content bytes read to know when the full
 * message has been received. It also supports chunked transfer encoding. If the content exceeds a maximum
 * allowed length, a SecurityException is thrown.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HttpPayloadParser {

    private static final String NEWLINE_REGEX = "\\r?\\n";
    private static final int MAX_CONTENT_LENGTH = 65536;
    private static final int MAX_HEADER_BLOCK_SIZE = 2048;
    private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("(?i)^content-length:\\s*(\\d+)$");
    private static final Pattern CHUNKED_ENCODING_PATTERN = Pattern.compile("(?i)^transfer-encoding:\\s*chunked$");

    private final ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream chunkHeaderBuffer = new ByteArrayOutputStream();

    private boolean headersDone = false;
    private int contentLength = -1;
    private int headersLength = -1;
    private boolean isChunked = false;
    private int currentChunkRemaining = -1;
    private boolean finalChunkSeen = false;

    /**
     * Accepts a byte array representing a fragment of the HTTP message (either headers or content).
     * It accumulates header data until the end of headers is detected, then processes content data
     * according to the Content-Length or chunked transfer encoding. If the content exceeds the maximum
     * allowed length, a SecurityException is thrown.
     *
     * @param frame the byte array containing a fragment of the HTTP message.
     * @throws SecurityException if the content exceeds maximum allowed length or if headers are malformed.
     */
    public void accept(byte[] frame) throws SecurityException {
        if (frame.length == 0) {
            return;
        }

        if (!headersDone) {
            headerBuffer.write(frame, 0, frame.length);
            if (headerBuffer.size() > MAX_HEADER_BLOCK_SIZE) {
                throw new SecurityException("Header buffer overload");
            }
            byte[] headerBytes = headerBuffer.toByteArray();
            int index = indexOfDoubleCRLF(headerBytes, 0);
            if (index >= 0) {
                headersDone = true;
                headersLength = index + 4; // length of "\r\n\r\n"

                // parse headers for content-length and chunked encoding
                for (String httpHeader : new String(headerBytes, StandardCharsets.ISO_8859_1).split(NEWLINE_REGEX)) {
                    Matcher matcher = CONTENT_LENGTH_PATTERN.matcher(httpHeader);
                    if (matcher.find()) {
                        try {
                            contentLength = Integer.parseInt(matcher.group(1));
                            if (contentLength < 0 || contentLength > MAX_CONTENT_LENGTH) {
                                throw new SecurityException("Invalid Content-Length");
                            }
                        } catch (NumberFormatException e) {
                            throw new SecurityException("Malformed Content-Length header: " + matcher.group(1));
                        }
                    } else {
                        matcher = CHUNKED_ENCODING_PATTERN.matcher(httpHeader);
                        if (matcher.find()) {
                            isChunked = true;
                        }
                    }
                }

                // move any bytes after headers into content processing buffer
                byte[] leftover = new byte[headerBytes.length - headersLength];
                System.arraycopy(headerBuffer.toByteArray(), headersLength, leftover, 0, leftover.length);
                if (leftover.length > 0) {
                    // process leftover through the chunked/fixed-length logic below
                    processContentBytes(leftover);
                }
                headerBuffer.reset();
                headerBuffer.write(headerBytes, 0, headersLength);
            }
            return; // no content processing until headers are done
        }
        processContentBytes(frame);
    }

    public byte[] getContent() {
        return contentBuffer.toByteArray();
    }

    public byte[] getHeaders() {
        return headerBuffer.toByteArray();
    }

    /**
     * Determines if the complete HTTP message (headers and content) has been read.
     * For chunked encoding, checks if the final chunk has been seen.
     * For fixed-length bodies, checks if the expected content length has been reached.
     * If neither chunked nor content-length is specified, it returns false as the end of the message
     * cannot be determined.
     *
     * @return true if the complete HTTP message has been read, false otherwise.
     */
    public boolean isComplete() {
        if (!headersDone) {
            return false;
        }
        if (isChunked) {
            return finalChunkSeen;
        }
        if (contentLength >= 0) {
            return contentBuffer.size() >= contentLength;
        }
        return false;
    }

    /**
     * Parses chunked transfer encoding from the given byte array and appends the decoded content data
     * to the contentBuffer. It handles chunk size lines, chunk data, and the final zero-length chunk.
     * If the chunked data is malformed or exceeds maximum allowed length, a SecurityException is thrown.
     *
     * @param block the byte array containing chunked data to be parsed.
     * @throws SecurityException if the chunked data is malformed or exceeds maximum allowed length.
     */
    private void parseChunkedBytes(byte[] block) throws SecurityException {
        int pos = 0, blockLength = block.length;
        while (pos < blockLength && !finalChunkSeen) {
            // are we expecting a new chunk-size line?
            if (currentChunkRemaining == -1) {
                // look for CRLF wholly inside this data block
                int lfPos = indexOfCRLF(block, pos);
                // or CR at end of buffer + LF at start of data
                boolean boundaryLF = chunkHeaderBuffer.size() > 0
                        && chunkHeaderBuffer.toByteArray()[chunkHeaderBuffer.size() - 1] == '\r' && pos < blockLength
                        && block[pos] == '\n';
                if (lfPos < 0 && !boundaryLF) {
                    // no complete CRLF yet - buffer everything
                    chunkHeaderBuffer.write(block, pos, blockLength - pos);
                    return;
                }
                // we have a CRLF either wholly in data, or spanning buffer + data
                byte[] chunkHeaderBytes;
                if (boundaryLF) {
                    // CR was in buffer, LF is data[pos] - drop buffer trailing '\r' and data[pos]
                    byte[] chunkHeaderPrefix = chunkHeaderBuffer.toByteArray();
                    // copy prefix without the last byte
                    chunkHeaderBytes = Arrays.copyOf(chunkHeaderPrefix, chunkHeaderPrefix.length - 1);
                    pos += 1; // consume the '\n'
                } else {
                    // entire line is in data block
                    int chunkHeaderLen = lfPos - pos;
                    chunkHeaderBytes = new byte[chunkHeaderLen];
                    System.arraycopy(block, pos, chunkHeaderBytes, 0, chunkHeaderLen);
                    pos = lfPos + 2; // skip '\r\n'
                }

                String chunkHeader = new String(chunkHeaderBytes, StandardCharsets.ISO_8859_1).trim();
                int chunkSize;
                try {
                    chunkSize = Integer.parseInt(chunkHeader, 16);
                } catch (NumberFormatException e) {
                    throw new SecurityException("Invalid chunk size: " + chunkHeader);
                }
                chunkHeaderBuffer.reset();
                if (chunkSize == 0) {
                    finalChunkSeen = true;
                    return;
                }
                currentChunkRemaining = chunkSize;
            }

            // we are in the middle of a chunk
            int take = Math.min(currentChunkRemaining, blockLength - pos);
            if (take > 0) {
                contentBuffer.write(block, pos, take);
                pos += take;
                currentChunkRemaining -= take;
                if (contentBuffer.size() > MAX_CONTENT_LENGTH) {
                    throw new SecurityException("Content exceeds maximum allowed length");
                }
            }

            // once we finish this chunk, we must consume the trailing CRLF
            if (currentChunkRemaining == 0) {
                if (blockLength - pos >= 2) {
                    if (block[pos] == '\r' && block[pos + 1] == '\n') {
                        pos += 2;
                        currentChunkRemaining = -1;
                    } else {
                        throw new SecurityException("Missing CRLF after chunk data");
                    }
                } else {
                    // buffer partial CRLF after chunk content for next accept()
                    chunkHeaderBuffer.write(block, pos, blockLength - pos);
                    return;
                }
            }
        }
    }

    /**
     * Processes content bytes according to whether the transfer encoding is chunked or fixed-length.
     * For chunked encoding, it calls parseChunkedBytes() to handle chunk parsing.
     * For fixed-length bodies, it appends up to contentLength bytes to the content buffer.
     * If no content-length is specified and not chunked, it treats the content as a stream and appends all data.
     * If the content exceeds the maximum allowed length, a SecurityException is thrown.
     *
     * @param data the byte array containing content data to be processed.
     * @throws SecurityException if the content exceeds maximum allowed length.
     */
    private void processContentBytes(byte[] data) throws SecurityException {
        if (isChunked) {
            parseChunkedBytes(data);
        } else if (contentLength >= 0) {
            // fixed-length content: accept up to contentLength
            int toCopy = Math.min(data.length, contentLength - contentBuffer.size());
            if (toCopy > 0) {
                contentBuffer.write(data, 0, toCopy);
            }
        } else {
            // no content-length (and not chunked): treat as a stream
            contentBuffer.write(data, 0, data.length);
        }
        if (contentBuffer.size() > MAX_CONTENT_LENGTH) {
            throw new SecurityException("Content exceeds maximum allowed length");
        }
    }

    /**
     * Finds the index of the CRLF sequence in the given byte array starting from a specified index.
     *
     * @param buf the byte array to search
     * @param from the starting index for the search
     * @return the index of the CRLF sequence, or -1 if not found
     */
    public static int indexOfCRLF(byte[] buf, int from) {
        for (int i = from; i + 1 < buf.length; i++) {
            if (buf[i] == '\r' && buf[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the double CRLF sequence in the given byte array.
     *
     * @param data the byte array to search
     * @return the index of the double CRLF sequence, or -1 if not found
     */
    public static int indexOfDoubleCRLF(byte[] data, int start) {
        for (int i = start; i + 3 < data.length; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
}
