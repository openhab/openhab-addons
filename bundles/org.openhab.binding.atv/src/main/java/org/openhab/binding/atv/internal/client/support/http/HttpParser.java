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
package org.openhab.binding.atv.internal.client.support.http;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;

/**
 * Parser and formatter for raw HTTP/RTSP messages.
 *
 * <p>
 * Byte-exactness notes:
 *
 * <ul>
 * <li>Messages are split on the first CRLFCRLF; only Content-Length delimited bodies are
 * supported (no chunked transfer encoding), and incomplete messages leave the input untouched so
 * the caller can retry once more data arrives.</li>
 * <li>A body is kept as {@code byte[]} when the Content-Type header starts with
 * {@code application} (e.g. {@code application/octet-stream},
 * {@code application/x-apple-binary-plist}); otherwise a UTF-8 decode is attempted and the raw
 * bytes are kept if the body is not valid UTF-8.</li>
 * <li>When formatting, {@code User-Agent} (requests), {@code Server} (responses),
 * {@code Content-Type} and {@code Content-Length} headers are inserted, and no Content-Length is
 * emitted for an empty body.</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HttpParser {

    /** User agent inserted into requests when none is given. */
    public static final String USER_AGENT = "openHAB/0.18.0";

    /** Server name inserted into responses when none is given. */
    public static final String SERVER_NAME = "openHAB-www/0.18.0";

    private static final byte[] HEADER_BODY_SEPARATOR = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

    // <protocol>/<version> <code> <message>, e.g. "HTTP/1.1 200 OK"
    private static final Pattern RESPONSE_LINE = Pattern.compile("([^/]+)/([0-9.]+) ([0-9]+) (.*)");

    // <method> <path> <protocol>/<version>, e.g. "GET / HTTP/1.1"
    private static final Pattern REQUEST_LINE = Pattern.compile("([A-Z_]+) ([^ ]+) ([^/]+)/([0-9.]+)");

    private HttpParser() {
    }

    /**
     * Result of a parse round: the parsed message (or {@code null} if the input does not yet
     * contain a complete message) and the leftover bytes to feed into the next round.
     *
     * @param <T> message type ({@link HttpRequest} or {@link HttpResponse})
     * @param message the parsed message, or {@code null} if more data is needed
     * @param remainder unconsumed bytes (the full input when {@code message} is {@code null})
     */
    public record ParseResult<T> (@Nullable T message, byte[] remainder) {
    }

    private record RawMessage(@Nullable String firstLine, CaseInsensitiveMap<String> headers, Object body,
            byte[] remainder) {
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        outer: for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * Splits headers from body on CRLFCRLF, parses the headers into a case-insensitive map and
     * extracts a Content-Length delimited body.
     */
    private static RawMessage parseHttpMessage(byte[] message) {
        int separator = indexOf(message, HEADER_BODY_SEPARATOR);
        if (separator < 0) {
            return new RawMessage(null, new CaseInsensitiveMap<>(), new byte[0], message);
        }

        String headerStr = new String(message, 0, separator, StandardCharsets.UTF_8);
        byte[] body = Arrays.copyOfRange(message, separator + HEADER_BODY_SEPARATOR.length, message.length);
        String[] headerLines = headerStr.split("\r\n", -1);

        CaseInsensitiveMap<String> msgHeaders = new CaseInsensitiveMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            String line = headerLines[i];
            if (line.isEmpty()) {
                continue;
            }
            int split = line.indexOf(": ");
            if (split < 0) {
                throw new IllegalArgumentException("bad header line: " + line);
            }
            msgHeaders.put(line.substring(0, split), line.substring(split + 2));
        }

        int contentLength = Integer.parseInt(msgHeaders.getOrDefault("Content-Length", "0").trim());
        if (body.length < contentLength) {
            return new RawMessage(null, new CaseInsensitiveMap<>(), new byte[0], message);
        }

        byte[] bodyBytes = Arrays.copyOfRange(body, 0, contentLength);
        Object msgBody = bodyBytes;

        // Assume body is text unless content type is application/*
        if (!msgHeaders.getOrDefault("Content-Type", "").startsWith("application")) {
            try {
                msgBody = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bodyBytes)).toString();
            } catch (CharacterCodingException e) {
                // Not valid UTF-8: keep body as bytes
            }
        }

        return new RawMessage(headerLines[0], msgHeaders, msgBody,
                Arrays.copyOfRange(body, contentLength, body.length));
    }

    /**
     * Parses an HTTP/RTSP response from a raw byte buffer.
     *
     * @param response raw bytes as received from the wire
     * @return the parsed response (or {@code null} when incomplete) plus leftover bytes
     * @throws IllegalArgumentException if the status line is malformed
     */
    public static ParseResult<HttpResponse> parseResponse(byte[] response) {
        RawMessage raw = parseHttpMessage(response);
        String firstLine = raw.firstLine();
        if (firstLine == null) {
            return new ParseResult<>(null, raw.remainder());
        }

        Matcher match = RESPONSE_LINE.matcher(firstLine);
        if (!match.lookingAt()) {
            throw new IllegalArgumentException("bad first line: " + firstLine);
        }

        return new ParseResult<>(new HttpResponse(match.group(1), match.group(2), Integer.parseInt(match.group(3)),
                match.group(4), raw.headers(), raw.body()), raw.remainder());
    }

    /**
     * Parses an HTTP/RTSP request from a raw byte buffer. Used for "reverse HTTP" where the remote
     * device sends us requests (AirPlay event channel).
     *
     * @param request raw bytes as received from the wire
     * @return the parsed request (or {@code null} when incomplete) plus leftover bytes
     * @throws IllegalArgumentException if the request line is malformed
     */
    public static ParseResult<HttpRequest> parseRequest(byte[] request) {
        RawMessage raw = parseHttpMessage(request);
        String firstLine = raw.firstLine();
        if (firstLine == null || firstLine.isEmpty()) {
            return new ParseResult<>(null, raw.remainder());
        }

        Matcher match = REQUEST_LINE.matcher(firstLine);
        if (!match.lookingAt()) {
            throw new IllegalArgumentException("bad first line: " + firstLine);
        }

        return new ParseResult<>(new HttpRequest(match.group(1), match.group(2), match.group(3), match.group(4),
                raw.headers(), raw.body()), raw.remainder());
    }

    /**
     * Returns true when {@code data} starts with a complete first line that looks like a response
     * status line (as opposed to a request line). Used to decide whether incoming data is a
     * response to one of our requests or an unsolicited reverse request.
     *
     * @param data raw buffered bytes (must contain at least one CRLF)
     * @return true if the first line matches {@code <proto>/<version> <code> <message>}
     */
    public static boolean startsWithResponseLine(byte[] data) {
        int lineEnd = indexOf(data, "\r\n".getBytes(StandardCharsets.US_ASCII));
        if (lineEnd < 0) {
            lineEnd = data.length;
        }
        String firstLine = new String(data, 0, lineEnd, StandardCharsets.UTF_8);
        return RESPONSE_LINE.matcher(firstLine).lookingAt();
    }

    /**
     * Formats a request/response first line plus headers and body into raw bytes. Header
     * insertion order is: User-Agent (unless already present in
     * {@code headers}, checked case-insensitively), Content-Type (when given), Content-Length
     * (when the body is non-empty), then all entries of {@code headers} in iteration order.
     *
     * @param method request method
     * @param uri request URI
     * @param protocol full protocol string, e.g. {@code "HTTP/1.1"} or {@code "RTSP/1.0"}
     * @param userAgent user agent inserted when {@code headers} has no User-Agent
     * @param contentType optional Content-Type header value ({@code null} to omit)
     * @param headers extra headers (values are stringified like Python's f-string); may be null
     * @param body message body ({@link String}, {@code byte[]} or {@code null})
     * @return the encoded message
     */
    public static byte[] formatMessage(String method, String uri, String protocol, String userAgent,
            @Nullable String contentType, @Nullable Map<String, ?> headers, @Nullable Object body) {
        byte[] bodyBytes = body == null ? new byte[0]
                : body instanceof byte[] bytes ? bytes : body.toString().getBytes(StandardCharsets.UTF_8);
        Map<String, ?> hdrs = headers == null ? Map.of() : headers;

        StringBuilder msg = new StringBuilder();
        msg.append(method).append(' ').append(uri).append(' ').append(protocol);
        if (!containsKeyIgnoreCase(hdrs, "User-Agent")) {
            msg.append("\r\nUser-Agent: ").append(userAgent);
        }
        if (contentType != null && !contentType.isEmpty()) {
            msg.append("\r\nContent-Type: ").append(contentType);
        }
        if (bodyBytes.length > 0) {
            msg.append("\r\nContent-Length: ").append(bodyBytes.length);
        }
        for (Map.Entry<String, ?> entry : hdrs.entrySet()) {
            msg.append("\r\n").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        msg.append("\r\n\r\n");

        byte[] header = msg.toString().getBytes(StandardCharsets.UTF_8);
        if (bodyBytes.length == 0) {
            return header;
        }
        byte[] output = Arrays.copyOf(header, header.length + bodyBytes.length);
        System.arraycopy(bodyBytes, 0, output, header.length, bodyBytes.length);
        return output;
    }

    /**
     * Encodes a request into raw bytes.
     *
     * @param request the request to encode
     * @return the encoded message
     */
    public static byte[] formatRequest(HttpRequest request) {
        return formatMessage(request.method(), request.path(), request.protocol() + "/" + request.version(), USER_AGENT,
                null, request.headers(), request.body());
    }

    /**
     * Encodes a response into raw bytes. A {@code Server} header
     * is inserted unless already present; a {@link Map} body is encoded as a binary property
     * list; Content-Length is only emitted for non-empty bodies.
     *
     * @param response the response to encode
     * @return the encoded message
     */
    public static byte[] formatResponse(HttpResponse response) {
        Map<String, ?> headers = response.headers() == null ? Map.of() : response.headers();

        StringBuilder output = new StringBuilder();
        output.append(response.protocol()).append('/').append(response.version()).append(' ').append(response.code())
                .append(' ').append(response.message()).append("\r\n");
        if (!containsKeyIgnoreCase(headers, "Server")) {
            output.append("Server: ").append(SERVER_NAME).append("\r\n");
        }
        for (Map.Entry<String, ?> entry : headers.entrySet()) {
            output.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        byte[] body = response.bodyBytes();
        if (body.length > 0) {
            output.append("Content-Length: ").append(body.length).append("\r\n");
        }
        output.append("\r\n");

        byte[] header = output.toString().getBytes(StandardCharsets.UTF_8);
        if (body.length == 0) {
            return header;
        }
        byte[] result = Arrays.copyOf(header, header.length + body.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        return result;
    }

    /**
     * Decodes a binary property list from a response body.
     *
     * @param response the response whose body holds a binary plist
     * @return the decoded root dictionary
     * @throws ProtocolError if the body is not bytes or text
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeBplistFromBody(HttpResponse response) {
        Object body = response.body();
        if (!(body instanceof byte[]) && !(body instanceof String)) {
            throw new ProtocolError(
                    "expected bytes or str but got " + (body == null ? "null" : body.getClass().getSimpleName()));
        }
        byte[] bytes = body instanceof byte[] raw ? raw : ((String) body).getBytes(StandardCharsets.UTF_8);
        return (Map<String, Object>) BinaryPlist.parse(bytes);
    }

    private static boolean containsKeyIgnoreCase(Map<String, ?> headers, String key) {
        if (headers instanceof CaseInsensitiveMap<?> cim) {
            return cim.containsKey(key);
        }
        for (String existing : headers.keySet()) {
            if (existing.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }
}
