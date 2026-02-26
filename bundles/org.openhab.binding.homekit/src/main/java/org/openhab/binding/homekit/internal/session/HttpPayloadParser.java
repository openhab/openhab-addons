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
package org.openhab.binding.homekit.internal.session;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.HomekitBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to parse incoming HTTP messages and determine when a complete message has been received.
 * It accumulates header data until the end of headers is detected, then reads the Content-Length header to
 * determine how many bytes of content to expect. It tracks the number of content bytes read to know when the
 * full message has been received. It also supports chunked transfer encoding. If the content exceeds a maximum
 * allowed length, a SecurityException is thrown. It uses a single reader thread for polling the socket input
 * stream either directly in plain text or via a {@link DecryptingInputStream} wrapper. And it uses call-backs
 * to deliver the results.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HttpPayloadParser implements AutoCloseable {

    /**
     * Represents a complete HTTP payload with headers and content.
     *
     * @param headers the HTTP headers as a byte array
     * @param content the HTTP content/body as a byte array
     */
    public record HttpPayload(byte[] headers, byte[] content) {
        public HttpPayload() {
            this(new byte[0], new byte[0]);
        }
    }

    private static final String NEWLINE_REGEX = "\\r?\\n";
    private static final int MAX_CONTENT_LENGTH = 262144; // 256 KB
    private static final int MAX_HEADER_BLOCK_SIZE = 2048;
    private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("(?i)^content-length:\\s*(\\d+)$");
    private static final Pattern CHUNKED_ENCODING_PATTERN = Pattern.compile("(?i)^transfer-encoding:\\s*chunked$");
    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("^(?:HTTP|EVENT)/\\d+\\.\\d+\\s+(\\d{3})");
    private static final int READER_START_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(HttpPayloadParser.class);

    private final InputStream inputStream;
    private final Thread inputThread;
    private final CountDownLatch inputThreadRunning = new CountDownLatch(1);
    private final HttpReaderListener listener;

    // grow-able buffer for incoming bytes, and indexes to valid data
    private byte[] inputBuffer = new byte[8192];
    private int inputStartIndex = 0; // first valid byte
    private int inputEndIndex = 0; // one past last valid byte

    // current message state
    private final ByteArrayOutput headerBuffer = new ByteArrayOutput();
    private final ByteArrayOutput contentBuffer = new ByteArrayOutput();
    private final ByteArrayOutput chunkDataBuffer = new ByteArrayOutput();

    private int contentLength = -1;
    private boolean headersDone = false;
    private boolean isChunked = false;
    private boolean finalChunkSeen = false;

    private volatile boolean closed = false;

    public HttpPayloadParser(InputStream stream, HttpReaderListener eventListener) {
        inputStream = stream;
        listener = eventListener;
        inputThread = new Thread(this::inputTask,
                "OH-binding-" + HomekitBindingConstants.BINDING_ID + "-http-parser-input");
        inputThread.setDaemon(true);
    }

    /**
     * Starts the input thread and blocks until the signal that it is actually running.
     */
    public void start() {
        inputThread.start();
        try {
            if (!inputThreadRunning.await(READER_START_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Input thread failed to start within {} seconds", READER_START_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted before input thread could start");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Continuously reads from the input stream, appending data to the input buffer, and parsing
     * available data to extract complete HTTP messages.
     */
    private void inputTask() {
        try {
            byte[] buffer = new byte[4096];
            inputThreadRunning.countDown();
            while (!closed) {
                try {
                    int byteCount = inputStream.read(buffer);
                    if (byteCount == -1) {
                        // normal EOF
                        break;
                    }
                    if (byteCount > 0) {
                        appendToBuffer(buffer, 0, byteCount);
                        parseAvailable();
                    }
                } catch (SocketTimeoutException e) {
                    // allows thread to poll closed flag or be interrupted
                    continue;
                } catch (IOException e) {
                    listener.onHttpReaderError(e);
                    break;
                }
            }
        } finally {
            closed = true;
            listener.onHttpReaderClose(
                    Arrays.copyOfRange(inputBuffer, inputStartIndex, Math.max(inputStartIndex, inputEndIndex - 1)));
        }
    }

    /**
     * Appends the given source bytes to the input buffer, growing or compacting it as needed.
     *
     * @param source the source byte array
     * @param offset the offset in the source array to start from
     * @param length the number of bytes to append
     */
    private void appendToBuffer(byte[] source, int offset, int length) {
        ensureCapacity(length);
        System.arraycopy(source, offset, inputBuffer, inputEndIndex, length);
        inputEndIndex += length;
    }

    /**
     * Ensures that the input buffer has enough capacity to accommodate the incoming byte count.
     * If not enough space is available, it will compact the buffer or grow it as needed.
     *
     * @param byteCount the number of incoming bytes to accommodate
     */
    private void ensureCapacity(int byteCount) {
        int free = inputBuffer.length - inputEndIndex;
        if (free >= byteCount) {
            return;
        }
        // compact if possible
        if (inputStartIndex > 0) {
            int remainingByteCount = inputEndIndex - inputStartIndex;
            System.arraycopy(inputBuffer, inputStartIndex, inputBuffer, 0, remainingByteCount);
            inputStartIndex = 0;
            inputEndIndex = remainingByteCount;
            free = inputBuffer.length - inputEndIndex;
            if (free >= byteCount) {
                return;
            }
        }
        // grow if needed
        int needed = inputEndIndex - inputStartIndex + byteCount;
        int newCapacity = Math.max(inputBuffer.length * 2, needed);
        byte[] newBuffer = new byte[newCapacity];
        System.arraycopy(inputBuffer, inputStartIndex, newBuffer, 0, inputEndIndex - inputStartIndex);
        inputEndIndex = inputEndIndex - inputStartIndex;
        inputStartIndex = 0;
        inputBuffer = newBuffer;
    }

    /**
     * Parses available bytes in the input buffer to extract complete HTTP messages.
     *
     * @throws IOException if an I/O error occurs or if the message is malformed
     */
    private void parseAvailable() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            if (!headersDone) {
                int headerEndIndex = indexOfDoubleCRLF(inputBuffer, inputStartIndex, inputEndIndex);
                if (headerEndIndex < 0) {
                    // need more data
                    if ((inputEndIndex - inputStartIndex) > MAX_HEADER_BLOCK_SIZE) {
                        throw new IOException("Header buffer overload");
                    }
                    return;
                }
                int headersLength = (headerEndIndex + 4) - inputStartIndex;
                if (headersLength > MAX_HEADER_BLOCK_SIZE) {
                    throw new IOException("Header buffer overload");
                }

                headerBuffer.reset();
                headerBuffer.write(inputBuffer, inputStartIndex, headersLength);
                parseHeaders(headerBuffer.toByteArray());

                headersDone = true;
                inputStartIndex += headersLength;
            }

            if (headersDone) {
                // if the message is complete AND has no body, emit immediately.
                // this handles: 200 without Content-Length, 204, 1xx, 4xx, 5xx.
                if (isComplete() && contentLength <= 0 && !isChunked) {
                    listener.onHttpPayload(new HttpPayload(headerBuffer.toByteArray(), contentBuffer.toByteArray()));
                    resetParserState();
                    continue; // parse next message if present
                }
                int remainingByteCount = inputEndIndex - inputStartIndex;
                if (remainingByteCount <= 0) {
                    return;
                }

                if (isChunked && !finalChunkSeen) {
                    chunkDataBuffer.write(inputBuffer, inputStartIndex, remainingByteCount);
                    if (chunkDataBuffer.size() > MAX_CONTENT_LENGTH) {
                        throw new IOException("Chunked data size %d exceeds maximum allowed %d"
                                .formatted(chunkDataBuffer.size(), MAX_CONTENT_LENGTH));
                    }
                    inputStartIndex += remainingByteCount;
                    parseChunkedBytesFromStagingBuffer();
                } else if (contentLength >= 0) {
                    int toCopy = Math.min(remainingByteCount, contentLength - contentBuffer.size());
                    if (toCopy > 0) {
                        contentBuffer.write(inputBuffer, inputStartIndex, toCopy);
                        inputStartIndex += toCopy;
                    } else {
                        // body is complete; do NOT consume extra bytes
                        // let the main loop emit the message and continue parsing
                    }
                } else {
                    // no content-length and not chunked; treat this message as header-only and emit it now
                    listener.onHttpPayload(new HttpPayload(headerBuffer.toByteArray(), contentBuffer.toByteArray()));
                    resetParserState();
                    continue; // parse next message if present
                }

                if (contentBuffer.size() > MAX_CONTENT_LENGTH) {
                    throw new IOException("Content size %d exceeds maximum allowed %d".formatted(contentBuffer.size(),
                            MAX_CONTENT_LENGTH));
                }

                if (isComplete()) {
                    listener.onHttpPayload(new HttpPayload(headerBuffer.toByteArray(), contentBuffer.toByteArray()));
                    resetParserState();
                    // loop to see if another message is already buffered
                } else {
                    // need more data
                    return;
                }
            }
        }
    }

    /**
     * Parses HTTP headers to extract Content-Length and Transfer-Encoding.
     *
     * @param headerBytes the byte array containing the HTTP headers
     * @throws IOException if an I/O error occurs or if headers are malformed
     */
    private void parseHeaders(byte[] headerBytes) throws IOException {
        contentLength = -1;
        isChunked = false;
        for (String httpHeader : new String(headerBytes, StandardCharsets.ISO_8859_1).split(NEWLINE_REGEX)) {
            Matcher matcher = CONTENT_LENGTH_PATTERN.matcher(httpHeader);
            if (matcher.find()) {
                try {
                    contentLength = Integer.parseInt(matcher.group(1));
                    if (contentLength < 0 || contentLength > MAX_CONTENT_LENGTH) {
                        throw new IOException("Invalid Content-Length");
                    }
                } catch (NumberFormatException e) {
                    throw new IOException("Malformed Content-Length header: " + matcher.group(1));
                }
            } else {
                matcher = CHUNKED_ENCODING_PATTERN.matcher(httpHeader);
                if (matcher.find()) {
                    isChunked = true;
                }
            }
        }
    }

    /**
     * Parses chunked bytes from the staging buffer and appends complete chunks to the content buffer.
     *
     * @throws IOException if an I/O error occurs or if chunk sizes are invalid
     */
    private void parseChunkedBytesFromStagingBuffer() throws IOException {
        byte[] chunkBuffer = chunkDataBuffer.toByteArray();
        int finalIndex = indexOfFinalChunkMarker(chunkBuffer);
        if (finalIndex < 0) { // not enough yet
            return;
        }

        finalChunkSeen = true;
        int pos = 0;
        int max = finalIndex + 5; // include "0\r\n\r\n"

        while (pos < max) {
            byte[] sizeBuffer = readln(chunkBuffer, pos);
            pos += sizeBuffer.length + 2;
            if (pos > max) {
                break;
            }
            if (sizeBuffer.length == 0) {
                continue;
            }

            int size;
            try {
                size = Integer.parseInt(new String(sizeBuffer, StandardCharsets.ISO_8859_1).trim(), 16);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid chunk size: " + new String(sizeBuffer, StandardCharsets.ISO_8859_1));
            }
            if (size == 0) {
                break;
            }

            if (pos + size > max) {
                throw new IOException("Chunk size exceeds available data");
            }

            contentBuffer.write(chunkBuffer, pos, size);
            pos += size;

            // skip CRLF after chunk
            byte[] leftoverBytes = readln(chunkBuffer, pos);
            pos += leftoverBytes.length + 2;
        }

        // drop everything we consumed from staging buffer
        int remainingByteCount = chunkDataBuffer.size() - max;
        chunkDataBuffer.reset();
        if (remainingByteCount > 0) {
            // push leftover bytes back into the main input buffer
            ensureCapacity(remainingByteCount);
            System.arraycopy(chunkBuffer, max, inputBuffer, inputEndIndex, remainingByteCount);
            inputEndIndex += remainingByteCount;
        }
    }

    /**
     * Determines if the current HTTP message is complete.
     *
     * @return true if the message is complete, or has an invalid status code, false otherwise
     */
    private boolean isComplete() {
        if (!headersDone) {
            return false;
        }
        if (isChunked) {
            return finalChunkSeen;
        }
        if (contentLength == 0) {
            return true; // explicit zero-length body
        }
        if (contentLength > 0) {
            return contentBuffer.size() >= contentLength;
        }
        try {
            int statusCode = getHttpStatusCode(headerBuffer.toByteArray());
            // no-body responses
            if (statusCode == 204 || (statusCode >= 100 && statusCode < 200)
                    || (statusCode >= 400 && statusCode < 600)) {
                return true;
            }
            // received 200 OK when 1) headers headers are completely received, 2) content-length is
            // missing and 3) encoding is not chunked => treat edge case as zero-length body
            if (statusCode == 200) {
                return true;
            }
        } catch (IllegalStateException e) {
            return true; // malformed status line - treat as complete
        }
        return false;
    }

    /*
     * Resets the parser state for the next HTTP message.
     */
    private void resetParserState() {
        headersDone = false;
        contentLength = -1;
        isChunked = false;
        finalChunkSeen = false;
        headerBuffer.reset();
        contentBuffer.reset();
        chunkDataBuffer.reset();
    }

    /**
     * Extracts the HTTP status code from the given header bytes.
     *
     * @param headerBytes the byte array containing the HTTP headers
     * @return the HTTP status code
     * @throws IllegalStateException if the status line is missing or malformed
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
     * Finds the index of the first occurrence of double CRLF ("\r\n\r\n") in the given data
     * between the specified from (inclusive) and to (exclusive) indices.
     *
     * @param data the byte array to search
     * @param from the starting index to search from (inclusive)
     * @param to the ending index to search to (exclusive)
     * @return the index of the first occurrence of double CRLF, or -1 if not found
     */
    private static int indexOfDoubleCRLF(byte[] data, int from, int to) {
        for (int i = from; i + 3 < to; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the first CRLF ("\r\n") in the given data starting from the specified index.
     *
     * @param data the byte array to search
     * @param from the starting index to search from
     * @return the index of the first CRLF, or -1 if not found
     */
    private static int indexOfCRLF(byte[] data, int from) {
        for (int i = from; i + 1 < data.length; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the final chunk marker ("0\r\n\r\n") in the given data.
     *
     * @param data the byte array containing the data
     * @return the index of the final chunk marker, or -1 if not found
     */
    private static int indexOfFinalChunkMarker(byte[] data) {
        byte[] marker = new byte[] { '0', '\r', '\n', '\r', '\n' };
        int len = data.length;
        for (int i = len - marker.length; i >= 0; i--) {
            boolean match = true;
            for (int j = 0; j < marker.length; j++) {
                if (data[i + j] != marker[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Reads a line ending with CRLF from the given data starting at the specified start index.
     *
     * @param data the byte array containing the data
     * @param start the starting index to read from
     * @return the line as a byte array (excluding CRLF)
     * @throws IllegalStateException if no CRLF is found
     */
    private static byte[] readln(byte[] data, int start) throws IllegalStateException {
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
        closed = true;
        try {
            inputThread.interrupt(); // interrupt is faster than closed flag alone
            inputThread.join(); // blocks until thread is really finished
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Simple grow-able byte array wrapper class.
     */
    protected static final class ByteArrayOutput {
        private byte[] buffer = new byte[256];
        private int size = 0;

        protected void write(byte[] data, int offset, int length) {
            ensure(length);
            System.arraycopy(data, offset, buffer, size, length);
            size += length;
        }

        protected int size() {
            return size;
        }

        protected byte[] toByteArray() {
            byte[] outputBytes = new byte[size];
            System.arraycopy(buffer, 0, outputBytes, 0, size);
            return outputBytes;
        }

        protected void reset() {
            size = 0;
        }

        private void ensure(int additionalByteCount) {
            int neededByteCount = size + additionalByteCount;
            if (neededByteCount <= buffer.length) {
                return;
            }
            int newCapacity = Math.max(buffer.length * 2, neededByteCount);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, size);
            buffer = newBuffer;
        }
    }
}
