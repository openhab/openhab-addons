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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public record HttpPayload(byte[] headers, byte[] content) {
        public HttpPayload() {
            this(new byte[0], new byte[0]);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(HttpPayloadParser.class);

    private final InputStream inputStream;
    private final Thread inputThread;
    private final ExecutorService outputThreadExecutor;

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

    private final SynchronousQueue<HttpPayload> httpPayloadQueue = new SynchronousQueue<>();
    private final Semaphore futureHttpPayloadLock = new Semaphore(1);

    private static final HttpPayload CLOSE_SENTINEL = new HttpPayload();
    private static final HttpPayload ERROR_SENTINEL = new HttpPayload();

    private volatile boolean closed = false;
    private volatile @Nullable Throwable parserError = null;

    public HttpPayloadParser(InputStream stream) {
        inputStream = stream;

        inputThread = new Thread(this::inputTask, "http-parser-input");
        inputThread.setDaemon(true);
        inputThread.start();

        outputThreadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "http-parser-output");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Returns a CompletableFuture that asynchronously waits for the next complete HTTP pay-load to be
     * available. Exactly one CompletableFuture can be supplied at any time; subsequent calls will block
     * until the first one completes.
     *
     * @return a CompletableFuture that will be completed with the next HttpPayload
     */
    public CompletableFuture<HttpPayload> awaitHttpPayload() {
        try {
            // first caller acquires lock; subsequent calls block until first caller future completes
            futureHttpPayloadLock.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }

        CompletableFuture<HttpPayload> futureHttpPayload = new CompletableFuture<>();
        outputThreadExecutor.submit(() -> {
            try {
                // take() will block until HTTP parser hands off a complete pay-load
                HttpPayload httpPayload = Objects.requireNonNull(httpPayloadQueue.take());
                if (httpPayload == ERROR_SENTINEL) {
                    futureHttpPayload.completeExceptionally(
                            parserError instanceof Exception e ? e : new IOException("Parser error"));
                    return;
                }
                if (httpPayload == CLOSE_SENTINEL) {
                    futureHttpPayload.completeExceptionally(new IOException("Parser closed"));
                    return;
                }
                futureHttpPayload.complete(httpPayload);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                futureHttpPayload.completeExceptionally(e);
            } catch (RuntimeException r) {
                futureHttpPayload.completeExceptionally(r);
            } finally {
                // release lock so subsequent calls can proceed
                futureHttpPayloadLock.release();
            }
        });

        return futureHttpPayload;
    }

    /**
     * Continuously reads from the input stream, appending data to the input buffer, and parsing
     * available data to extract complete HTTP messages.
     */
    private void inputTask() {
        try {
            byte[] buffer = new byte[4096];
            while (!closed) {
                int byteCount = inputStream.read(buffer);
                if (byteCount == -1) {
                    // normal EOF
                    break;
                }
                if (byteCount > 0) {
                    appendToBuffer(buffer, 0, byteCount);
                    parseAvailable();
                }
            }
        } catch (IOException e) {
            logger.debug("Input stream closed or error occurred: {}", e.getMessage());
            parserError = e;
        } finally {
            closed = true;
            try {
                // wake consumer with the correct sentinel
                if (parserError != null) {
                    httpPayloadQueue.put(ERROR_SENTINEL);
                } else {
                    httpPayloadQueue.put(CLOSE_SENTINEL);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

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
        int needed = inputEndIndex + byteCount;
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
        while (true) {
            if (!headersDone) {
                int headerEndIndex = indexOfDoubleCRLF(inputBuffer, inputStartIndex, inputEndIndex);
                if (headerEndIndex < 0) {
                    // need more data
                    return;
                }
                int headersLength = (headerEndIndex + 4) - inputStartIndex;
                if (headersLength > MAX_HEADER_BLOCK_SIZE) {
                    throw new SecurityException("Header buffer overload");
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
                    enqueuePayload(new HttpPayload(headerBuffer.toByteArray(), contentBuffer.toByteArray()));
                    resetParserState();
                    continue; // parse next message if present
                }
                int remainingByteCount = inputEndIndex - inputStartIndex;
                if (remainingByteCount <= 0) {
                    return;
                }

                if (isChunked && !finalChunkSeen) {
                    chunkDataBuffer.write(inputBuffer, inputStartIndex, remainingByteCount);
                    inputStartIndex += remainingByteCount;
                    parseChunkedBytesFromStagingBuffer();
                } else if (contentLength >= 0) {
                    int toCopy = Math.min(remainingByteCount, contentLength - contentBuffer.size());
                    if (toCopy > 0) {
                        contentBuffer.write(inputBuffer, inputStartIndex, toCopy);
                        inputStartIndex += toCopy;
                    } else {
                        // nothing more needed for this message
                        inputStartIndex += remainingByteCount;
                    }
                } else {
                    // no content-length and not chunked; body presence decided by status code
                    // we don't consume anything here; message is complete by headers alone
                }

                if (contentBuffer.size() > MAX_CONTENT_LENGTH) {
                    throw new IOException("Content exceeds maximum allowed length");
                }

                if (isComplete()) {
                    enqueuePayload(new HttpPayload(headerBuffer.toByteArray(), contentBuffer.toByteArray()));
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
        if (remainingByteCount > 0) {
            byte[] restBytes = new byte[remainingByteCount];
            System.arraycopy(chunkBuffer, max, restBytes, 0, remainingByteCount);
            chunkDataBuffer.reset();
            chunkDataBuffer.write(restBytes, 0, remainingByteCount);
        } else {
            chunkDataBuffer.reset();
        }
    }

    /**
     * Determines if the current HTTP message is complete.
     *
     * @return true if the message is complete, false otherwise
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
            // 200 OK with no content-length => treat as zero-length body
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
     * Enqueues the given HTTP pay-load.
     *
     * @param httpPayload the HTTP pay-load to enqueue
     */
    private void enqueuePayload(HttpPayload httpPayload) {
        try {
            httpPayloadQueue.put(httpPayload); // blocks until a consumer takes it
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            inputStream.close();
        } catch (Exception e) {
            // fall through
        }
        try {
            inputThread.interrupt();
            inputThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        outputThreadExecutor.shutdownNow();
        try {
            if (!outputThreadExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                logger.debug("Executor did not terminate promptly");
            }
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
