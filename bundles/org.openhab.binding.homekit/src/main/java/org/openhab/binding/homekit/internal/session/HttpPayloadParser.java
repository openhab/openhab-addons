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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class HttpPayloadParser implements AutoCloseable {

    private static final String NEWLINE_REGEX = "\\r?\\n";
    private static final int MAX_CONTENT_LENGTH = 65536;
    private static final int MAX_HEADER_BLOCK_SIZE = 2048;
    private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("(?i)^content-length:\\s*(\\d+)$");
    private static final Pattern CHUNKED_ENCODING_PATTERN = Pattern.compile("(?i)^transfer-encoding:\\s*chunked$");
    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("^(?:HTTP|EVENT)/\\d+\\.\\d+\\s+(\\d{3})");

    private final ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream chunkDataBuffer = new ByteArrayOutputStream();

    private boolean headersDone = false;
    private int contentLength = -1;
    private int headersLength = -1;
    private boolean isChunked = false;
    private boolean finalChunkSeen = false;

    /**
     * Accepts a byte array representing a fragment of the HTTP message (either headers or content).
     * It accumulates header data until the end of headers is detected, then processes content data
     * according to the Content-Length or chunked transfer encoding. If the content exceeds the maximum
     * allowed length, a SecurityException is thrown.
     *
     * @param frame the byte array containing a fragment of the HTTP message.
     * @throws IllegalStateException
     */
    public void accept(byte[] frame) throws IllegalStateException {
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
        // no content-length and not chunked: check status code
        try {
            int statusCode = getHttpStatusCode(headerBuffer.toByteArray());
            if (statusCode == 204 || (statusCode >= 100 && statusCode < 200)) {
                return true; // no-body responses
            }
            if (statusCode >= 400 && statusCode < 600) {
                return true; // treat error responses as complete even without body
            }
        } catch (IllegalStateException e) {
            // malformed status line - treat as complete
            return true;
        }
        return false;
    }

    /**
     * Extracts the HTTP status code from the given header byte array.
     * It looks for the status line in the format "HTTP/x.x xxx" and parses the three-digit status code.
     * If no valid status line is found or if the status code is malformed, a SecurityException is thrown.
     *
     * @param headerBytes the byte array containing HTTP headers.
     * @return the extracted HTTP status code as an integer.
     * @throws IllegalStateException if the status line is missing or malformed.
     */
    public static int getHttpStatusCode(byte[] headerBytes) throws IllegalStateException {
        String headers = new String(headerBytes, StandardCharsets.ISO_8859_1);
        Matcher matcher = STATUS_LINE_PATTERN.matcher(headers);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Malformed HTTP status code: " + matcher.group(1));
            }
        }
        throw new IllegalStateException("Missing HTTP status line");
    }

    /**
     * Parses chunked transfer encoding from the given byte array and appends the decoded content data
     * to the contentBuffer. It handles chunk size lines, chunk data, and the final zero-length chunk.
     * If the chunked data is malformed or exceeds maximum allowed length, a SecurityException is thrown.
     *
     * @param block the byte array containing chunked data to be parsed.
     * @throws IllegalStateException if the chunked data is malformed.
     */
    private void parseChunkedBytes(byte[] block) throws IllegalStateException {
        chunkDataBuffer.write(block, 0, block.length); // copy all incoming data into the buffer
        byte[] chunkBuffer = chunkDataBuffer.toByteArray();
        if (indexOfFinalChunkMarker(chunkBuffer) >= 0) {
            finalChunkSeen = true;
            int pos = 0;
            int max = chunkBuffer.length;
            while (pos < max) {
                byte[] sizeBuffer = readln(chunkBuffer, pos);
                if ((pos += sizeBuffer.length + 2) >= max) { // move past size and CRLF; exit on overrun
                    break;
                }
                if (sizeBuffer.length == 0) {
                    continue; // some implementations insert empty lines, so skip them
                }
                int size;
                try {
                    size = Integer.parseInt(new String(sizeBuffer, StandardCharsets.ISO_8859_1).trim(), 16);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid chunk size: " + sizeBuffer);
                }
                contentBuffer.write(chunkBuffer, pos, size);
                if ((pos += size) >= max) { // move past data; exit on overrun
                    break;
                }
                byte[] leftover = readln(chunkBuffer, pos); // read to the next CRLF after the chunk data
                if ((pos += leftover.length + 2) >= max) { // skip leftover data and CRLF; exit on overrun
                    break;
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
     * @throws IllegalStateException if the content exceeds maximum allowed length.
     */
    private void processContentBytes(byte[] data) throws IllegalStateException {
        if (isChunked && !finalChunkSeen) {
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
            throw new IllegalStateException("Content exceeds maximum allowed length");
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

    /**
     * Finds the index of the final chunk marker ("0\r\n\r\n") in the given byte array.
     *
     * @param data the byte array to search
     * @return the index of the final chunk marker, or -1 if not found
     */
    public static int indexOfFinalChunkMarker(byte[] data) {
        byte[] marker = new byte[] { '0', '\r', '\n', '\r', '\n' };
        int len = data.length;
        // start from the last possible position where the marker could begin
        for (int i = len - marker.length; i >= 0; i--) {
            boolean match = true;
            for (int j = 0; j < marker.length; j++) {
                if (data[i + j] != marker[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i; // found the final chunk marker
            }
        }
        return -1; // not found
    }

    /**
     * Reads a line from the given byte array starting at the specified index until a CRLF sequence is found.
     *
     * @param data the byte array to read from
     * @param start the starting index for reading
     * @return a byte array containing the line read (excluding CRLF)
     * @throws IllegalStateException if no CRLF is found
     */
    public static byte[] readln(byte[] data, int start) throws IllegalStateException {
        int end = indexOfCRLF(data, start);
        if (end < 0) {
            throw new IllegalStateException("No CRLF found in chunked data");
        }
        byte[] line = new byte[end - start];
        System.arraycopy(data, start, line, 0, line.length);
        return line;
    }

    @Override
    public void close() throws IOException {
        headerBuffer.close();
        contentBuffer.close();
        chunkDataBuffer.close();
    }
}
