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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the RTSP protocol used by Apple (with its quirks and all), generalized to
 * support both AirPlay 1 and 2.
 *
 * <p>
 * Wraps an {@link HttpConnection}, sending messages with protocol {@code RTSP/1.0} and
 * correlating responses by their {@code CSeq} header: the connection level FIFO may hand a
 * response for a different CSeq to us (RTSP responses can arrive out of order), in which case the
 * response completes the future registered for that CSeq while we keep waiting for our own.
 *
 * <p>
 * Every request carries the standard headers CSeq, DACP-ID (random 64-bit as uppercase hex,
 * no zero padding), Active-Remote (random 32-bit decimal) and Client-Instance (same value as
 * DACP-ID). The session URI is {@code rtsp://<local ip>/<session id>} with a random 32-bit
 * session id. Password protected devices are supported via MD5 digest authentication
 * (RFC 2069 style, no qop): after {@link #announce} receives a 401 with a WWW-Authenticate
 * challenge, an {@code Authorization} header is added to every subsequent request.
 *
 * <p>
 * DMAP-tagged now-playing updates (metadata/artwork) are not handled here; they belong with
 * the RAOP protocol implementation.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RtspSession {

    /** Frames per audio packet. */
    public static final int FRAMES_PER_PACKET = 352;

    /** User agent used for all RTSP requests. */
    public static final String USER_AGENT = "AirPlay/550.10";

    /** Content type for binary property list payloads. */
    public static final String BPLIST_CONTENT_TYPE = "application/x-apple-binary-plist";

    /** Payload byte signaling that traffic is to be unencrypted (auth-setup). */
    public static final byte[] AUTH_SETUP_UNENCRYPTED = new byte[] { 0x01 };

    /**
     * A static Curve25519 public key used to satisfy the auth-setup step for devices requiring
     * it (e.g. AirPort Express). Nothing is ever verified. Source: owntone-server.
     */
    public static final byte[] CURVE25519_PUB_KEY = { 0x59, 0x02, (byte) 0xed, (byte) 0xe9, 0x0d, 0x4e, (byte) 0xf2,
            (byte) 0xbd, 0x4c, (byte) 0xb6, (byte) 0x8a, 0x63, 0x30, 0x03, (byte) 0x82, 0x07, (byte) 0xa9, 0x4d,
            (byte) 0xbd, 0x50, (byte) 0xd8, (byte) 0xaa, 0x46, 0x5b, 0x5d, (byte) 0x8c, 0x01, 0x2a, 0x0c, 0x7e, 0x1d,
            0x4e };

    private static final String HTTP_PROTOCOL = "HTTP/1.1";
    private static final String RTSP_PROTOCOL = "RTSP/1.0";

    /** SDP payload template for ANNOUNCE. */
    private static final String ANNOUNCE_PAYLOAD = "v=0\r\n" + "o=iTunes %1$s 0 IN IP4 %2$s\r\n" + "s=iTunes\r\n"
            + "c=IN IP4 %3$s\r\n" + "t=0 0\r\n" + "m=audio 0 RTP/AVP 96\r\n" + "a=rtpmap:96 L16/44100/2\r\n"
            + "a=fmtp:96 " + FRAMES_PER_PACKET + " 0 %4$d 40 10 14 %5$d 255 0 0 %6$d\r\n";

    /** Timeout waiting for the response with our CSeq. */
    private static final Duration CSEQ_TIMEOUT = Duration.ofSeconds(4);

    private static final Logger LOGGER = LoggerFactory.getLogger(RtspSession.class);

    /**
     * Digest authentication information for password protected devices.
     *
     * @param username user name (the default is {@code "openHAB"})
     * @param realm realm from the WWW-Authenticate challenge
     * @param password device password
     * @param nonce nonce from the WWW-Authenticate challenge
     */
    public record DigestInfo(String username, String realm, String password, String nonce) {
    }

    private final HttpConnection connection;
    private final Map<Integer, CompletableFuture<HttpResponse>> requests = new ConcurrentHashMap<>();
    private final Object sendLock = new Object();

    private final long sessionId;
    private final String dacpId;
    private final long activeRemote;

    private volatile @Nullable DigestInfo digestInfo; // Password authentication
    private int cseq;

    /**
     * Creates a new session with random session id, DACP-ID and Active-Remote.
     *
     * @param connection the underlying connection
     */
    public RtspSession(HttpConnection connection) {
        this(connection, randomUint32(), randomUint64(), randomUint32());
    }

    /**
     * Creates a new session with explicit identifiers (for tests).
     *
     * @param connection the underlying connection
     * @param sessionId session id (unsigned 32-bit)
     * @param dacpId DACP-ID source value (unsigned 64-bit, rendered as uppercase hex)
     * @param activeRemote Active-Remote value (unsigned 32-bit)
     */
    public RtspSession(HttpConnection connection, long sessionId, long dacpId, long activeRemote) {
        this.connection = connection;
        this.sessionId = sessionId;
        // uppercase hex, no zero padding
        this.dacpId = HexFormat.of().withUpperCase().toHexDigits(dacpId).replaceFirst("^0+(?=.)", "");
        this.activeRemote = activeRemote;
    }

    private static long randomUint32() {
        return new SecureRandom().nextLong(1L << 32);
    }

    private static long randomUint64() {
        return new SecureRandom().nextLong();
    }

    /** Returns the underlying connection. */
    public HttpConnection connection() {
        return connection;
    }

    /** Returns the session id (random unsigned 32-bit). */
    public long sessionId() {
        return sessionId;
    }

    /** Returns the DACP-ID header value (uppercase hex). */
    public String dacpId() {
        return dacpId;
    }

    /** Returns the Active-Remote header value (unsigned 32-bit). */
    public long activeRemote() {
        return activeRemote;
    }

    /** Returns the current digest authentication info, or {@code null} when not authenticating. */
    public @Nullable DigestInfo digestInfo() {
        return digestInfo;
    }

    /**
     * Sets digest authentication info explicitly (normally populated by {@link #announce}).
     *
     * @param digestInfo the info, or {@code null} to disable authentication
     */
    public void setDigestInfo(@Nullable DigestInfo digestInfo) {
        this.digestInfo = digestInfo;
    }

    /**
     * Returns the URI used for session requests: {@code rtsp://<local ip>/<session id>}.
     */
    public String uri() {
        return "rtsp://" + connection.localIp() + "/" + Long.toUnsignedString(sessionId);
    }

    /**
     * Returns the Authorization payload for Apple's digest authentication (RFC 2069 style MD5
     * digest without qop).
     *
     * @param method request method
     * @param uri request URI
     * @param user user name
     * @param realm authentication realm
     * @param pwd password
     * @param nonce server nonce
     * @return the Authorization header value
     */
    public static String getDigestPayload(String method, String uri, String user, String realm, String pwd,
            String nonce) {
        String ha1 = md5Hex(user + ":" + realm + ":" + pwd);
        String ha2 = md5Hex(method + ":" + uri);
        String diResponse = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        return "Digest username=\"" + user + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + uri
                + "\", response=\"" + diResponse + "\"";
    }

    private static String md5Hex(String input) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    /**
     * Returns device information from {@code GET /info}, or an empty map if the device does not
     * support it.
     *
     * @return future device information dictionary
     */
    public CompletableFuture<Map<String, Object>> info() {
        return exchange("GET", "/info", null, null, null, true, RTSP_PROTOCOL).thenApply(deviceInfo -> {
            // If not supported, just return an empty dict
            if (deviceInfo.code() != 200) {
                LOGGER.debug("Device does not support /info");
                return Map.of();
            }
            return HttpParser.decodeBplistFromBody(deviceInfo);
        });
    }

    /**
     * Sends the auth-setup message with the default "unencrypted" payload.
     *
     * @return future response
     */
    public CompletableFuture<HttpResponse> authSetup() {
        // Payload to say that we want to proceed unencrypted
        byte[] body = Arrays.copyOf(AUTH_SETUP_UNENCRYPTED, AUTH_SETUP_UNENCRYPTED.length + CURVE25519_PUB_KEY.length);
        System.arraycopy(CURVE25519_PUB_KEY, 0, body, AUTH_SETUP_UNENCRYPTED.length, CURVE25519_PUB_KEY.length);
        return authSetup(body);
    }

    /**
     * Sends the auth-setup message with an explicit payload.
     *
     * @param body the auth-setup payload
     * @return future response
     */
    public CompletableFuture<HttpResponse> authSetup(byte[] body) {
        return exchange("POST", "/auth-setup", "application/octet-stream", null, body, false, HTTP_PROTOCOL);
    }

    /**
     * Sends an ANNOUNCE message (AirPlay 1 only). When
     * {@code password} is given and the device responds 401 with a WWW-Authenticate challenge,
     * digest authentication is set up and the ANNOUNCE retried.
     *
     * @param bytesPerChannel bytes per audio channel (bits = 8 * this)
     * @param channels number of audio channels
     * @param sampleRate sample rate in Hz
     * @param password optional device password ({@code null} when not password protected)
     * @return future response
     */
    public CompletableFuture<HttpResponse> announce(int bytesPerChannel, int channels, int sampleRate,
            @Nullable String password) {
        String body = String.format(ANNOUNCE_PAYLOAD, Long.toUnsignedString(sessionId), connection.localIp(),
                connection.remoteIp(), 8 * bytesPerChannel, channels, sampleRate);

        boolean requiresPassword = password != null;

        return exchange("ANNOUNCE", null, "application/sdp", null, body, requiresPassword, RTSP_PROTOCOL)
                .thenCompose(response -> {
                    // Save the necessary data for password authentication
                    Object wwwAuthenticate = response.headers().get("www-authenticate");
                    if (response.code() == 401 && wwwAuthenticate != null && password != null) {
                        // WWW-Authenticate is split on double-quotes: _, realm, _, nonce, _
                        String[] parts = wwwAuthenticate.toString().split("\"", -1);
                        digestInfo = new DigestInfo("openHAB", parts[1], password, parts[3]);

                        return exchange("ANNOUNCE", null, "application/sdp", null, body, false, RTSP_PROTOCOL);
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    /**
     * Sends a SETUP message.
     *
     * @param headers extra headers (may be null)
     * @param body request body ({@link String}, {@code byte[]} or {@link Map} which is encoded
     *            as a binary plist; may be null)
     * @return future response
     */
    public CompletableFuture<HttpResponse> setup(@Nullable Map<String, ?> headers, @Nullable Object body) {
        return exchange("SETUP", null, null, headers, body, false, RTSP_PROTOCOL);
    }

    /**
     * Sends a RECORD message.
     *
     * @param headers extra headers (may be null)
     * @param body request body (may be null)
     * @return future response
     */
    public CompletableFuture<HttpResponse> record(@Nullable Map<String, ?> headers, @Nullable Object body) {
        return exchange("RECORD", null, null, headers, body, false, RTSP_PROTOCOL);
    }

    /**
     * Sends a FLUSH message.
     *
     * @param headers extra headers (may be null)
     * @param body request body (may be null)
     * @return future response
     */
    public CompletableFuture<HttpResponse> flush(@Nullable Map<String, ?> headers, @Nullable Object body) {
        return exchange("FLUSH", null, null, headers, body, false, RTSP_PROTOCOL);
    }

    /**
     * Sends a SET_PARAMETER message.
     *
     * @param parameter parameter name
     * @param value parameter value
     * @return future response
     */
    public CompletableFuture<HttpResponse> setParameter(String parameter, String value) {
        return exchange("SET_PARAMETER", null, "text/parameters", null, parameter + ": " + value, false, RTSP_PROTOCOL);
    }

    /**
     * Sends a feedback message.
     *
     * @param allowError if true, error responses complete normally (except 403)
     * @return future response
     */
    public CompletableFuture<HttpResponse> feedback(boolean allowError) {
        return exchange("POST", "/feedback", null, null, null, allowError, RTSP_PROTOCOL);
    }

    /**
     * Sends a TEARDOWN message.
     *
     * @param rtspSession the RTSP session id to tear down (value of the Session header)
     * @return future response
     */
    public CompletableFuture<HttpResponse> teardown(Object rtspSession) {
        return exchange("TEARDOWN", null, null, Map.of("Session", rtspSession), null, false, RTSP_PROTOCOL);
    }

    /**
     * Sends an RTSP message and returns the (future) response.
     *
     * <p>
     * Standard headers (CSeq, DACP-ID, Active-Remote, Client-Instance and, when digest
     * authentication is active, Authorization) are added before {@code headers}. A {@link Map}
     * body is encoded as a binary property list with Content-Type
     * {@value #BPLIST_CONTENT_TYPE}. The response completing the returned future is the one
     * whose CSeq header matches this request, even if the connection delivers responses out of
     * order.
     *
     * @param method request method
     * @param uri request URI ({@code null} uses the session URI {@link #uri()})
     * @param contentType optional Content-Type value
     * @param headers extra headers (may be null)
     * @param body request body ({@link String}, {@code byte[]}, {@link Map} or null)
     * @param allowError if true, error codes other than 403 complete normally
     * @param protocol protocol string (normally {@code "RTSP/1.0"})
     * @return future response, failing with {@link OperationTimeoutError} when no response with
     *         our CSeq arrives in time
     */
    public CompletableFuture<HttpResponse> exchange(String method, @Nullable String uri, @Nullable String contentType,
            @Nullable Map<String, ?> headers, @Nullable Object body, boolean allowError, String protocol) {
        int requestCseq;
        String effectiveUri = uri != null ? uri : uri();
        CompletableFuture<HttpResponse> cseqFuture = new CompletableFuture<>();
        CompletableFuture<HttpResponse> sendFuture;

        // Allocate the CSeq and send under a lock so CSeq order matches wire order
        synchronized (sendLock) {
            requestCseq = cseq;
            cseq += 1;

            Map<String, Object> hdrs = new LinkedHashMap<>();
            hdrs.put("CSeq", requestCseq);
            hdrs.put("DACP-ID", dacpId);
            hdrs.put("Active-Remote", activeRemote);
            hdrs.put("Client-Instance", dacpId);

            // Add the password authentication if required
            DigestInfo info = digestInfo;
            if (info != null) {
                hdrs.put("Authorization", getDigestPayload(method, effectiveUri, info.username(), info.realm(),
                        info.password(), info.nonce()));
            }

            if (headers != null) {
                hdrs.putAll(headers);
            }

            // If body is a map, assume that payload should be sent as a binary plist
            Object requestBody = body;
            if (body instanceof Map) {
                hdrs.put("Content-Type", BPLIST_CONTENT_TYPE);
                requestBody = BinaryPlist.dump(body);
            }

            // Map a future to the current CSeq and make the request
            requests.put(requestCseq, cseqFuture);
            sendFuture = connection.sendAndReceive(method, effectiveUri, protocol, USER_AGENT, contentType, hdrs,
                    requestBody, allowError, HttpConnection.DEFAULT_TIMEOUT);
        }

        sendFuture.whenComplete((response, error) -> {
            if (error != null) {
                Throwable directCause = error.getCause();
                Throwable cause = error instanceof CompletionException && directCause != null ? directCause : error;
                cseqFuture.completeExceptionally(cause);
                return;
            }
            // The response most likely contains a CSeq and it is also very likely to be the one
            // we expect, but it could be for someone else. So complete the matching future.
            int responseCseq = parseCseq(response);
            CompletableFuture<HttpResponse> waiting = requests.get(responseCseq);
            if (waiting != null) {
                waiting.complete(response);
            }
        });

        // Wait for the response to the CSeq we expect
        return cseqFuture.orTimeout(CSEQ_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS).handle((response, error) -> {
            requests.remove(requestCseq);
            if (error != null) {
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause()
                        : error;
                if (cause instanceof TimeoutException) {
                    throw new CompletionException(new OperationTimeoutError(
                            "no response to CSeq " + requestCseq + " (" + effectiveUri + ")", cause));
                }
                throw new CompletionException(cause);
            }
            return response;
        });
    }

    private static int parseCseq(HttpResponse response) {
        Object value = response.headers().get("CSeq");
        try {
            return value == null ? -1 : Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
