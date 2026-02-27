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
package org.openhab.binding.dahuadoor.internal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class DahuaDoorHttpQueries {

    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    private static final String SNAPSHOT_PATH = "/cgi-bin/snapshot.cgi";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(DahuaDoorHttpQueries.class);

    private @Nullable DahuaDoorConfiguration config;
    private @Nullable HttpClient httpClient;
    private @Nullable DigestAuthentication digestAuth;
    private @Nullable URI authUri;

    public DahuaDoorHttpQueries(@Nullable HttpClient httpClient, @Nullable DahuaDoorConfiguration config) {
        this.config = config;
        this.httpClient = httpClient;

        if (httpClient != null && config != null) {
            try {
                authUri = new URI("http://" + config.hostname);
                AuthenticationStore auth = httpClient.getAuthenticationStore();
                digestAuth = new DigestAuthentication(authUri, Authentication.ANY_REALM, config.username,
                        config.password);
                auth.addAuthentication(digestAuth);
            } catch (Exception e) {
                logger.warn("Failed to configure digest authentication for {}", config.hostname, e);
            }
        }
    }

    public byte @Nullable [] requestImage() {
        final @Nullable DahuaDoorConfiguration localConfig = config;
        if (localConfig == null) {
            logger.warn("Configuration not initialized");
            return null;
        }

        try {
            SnapshotHttpResponse response = sendSnapshotRequest(localConfig, null);
            if (response.statusCode == 200) {
                return response.body;
            }

            if (response.statusCode == 401) {
                String challenge = response.headers.get("www-authenticate");
                if (challenge != null) {
                    String digestHeader = createDigestAuthorizationHeader(challenge, localConfig);
                    if (digestHeader != null) {
                        SnapshotHttpResponse authResponse = sendSnapshotRequest(localConfig, digestHeader);
                        if (authResponse.statusCode == 200) {
                            return authResponse.body;
                        }
                        logger.debug("Authenticated snapshot request failed with HTTP status {} from {}",
                                authResponse.statusCode, localConfig.hostname);
                    }
                }
            }

            logger.debug("Snapshot request failed with HTTP status {} from {}", response.statusCode,
                    localConfig.hostname);
        } catch (Exception e) {
            logger.warn("Could not retrieve snapshot from {}", localConfig.hostname, e);
        }

        return null;
    }

    public void openDoor(int doorNo) {
        final @Nullable DahuaDoorConfiguration localConfig = config;

        if (localConfig == null) {
            logger.warn("Configuration not initialized");
            return;
        }

        try {
            String path = "/cgi-bin/accessControl.cgi?action=openDoor&UserID=101&Type=Remote&channel=" + doorNo;
            OpenDoorHttpResponse response = sendOpenDoorRequest(localConfig, path, null);

            if (response.statusCode == 200) {
                logger.debug("Open Door Success");
            } else if (response.statusCode == 401) {
                String challenge = response.wwwAuthenticate;
                if (challenge != null) {
                    String digestHeader = createDigestAuthorizationHeaderForOpenDoor(challenge, localConfig, path);
                    if (digestHeader != null) {
                        OpenDoorHttpResponse authResponse = sendOpenDoorRequest(localConfig, path, digestHeader);
                        if (authResponse.statusCode == 200) {
                            logger.debug("Open Door Success (with authentication)");
                        } else {
                            logger.debug("Open door request failed with HTTP status {} for door {} on {}",
                                    authResponse.statusCode, doorNo, localConfig.hostname);
                        }
                    }
                } else {
                    logger.debug("Open door request failed with HTTP status {} for door {} on {}", response.statusCode,
                            doorNo, localConfig.hostname);
                }
            } else {
                logger.debug("Open door request failed with HTTP status {} for door {} on {}", response.statusCode,
                        doorNo, localConfig.hostname);
            }
        } catch (Exception e) {
            logger.warn("Could not open door {} on {}", doorNo, localConfig.hostname, e);
        }
    }

    private OpenDoorHttpResponse sendOpenDoorRequest(DahuaDoorConfiguration localConfig, String path,
            @Nullable String authorizationHeader) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(localConfig.hostname, 80), REQUEST_TIMEOUT_SECONDS * 1000);
            socket.setSoTimeout(REQUEST_TIMEOUT_SECONDS * 1000);

            StringBuilder request = new StringBuilder();
            request.append("GET ").append(path).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(localConfig.hostname).append("\r\n");
            request.append("Connection: close\r\n");
            if (authorizationHeader != null) {
                request.append("Authorization: ").append(authorizationHeader).append("\r\n");
            }
            request.append("\r\n");

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();

            byte[] rawResponse = readHttpResponse(socket.getInputStream());
            return parseOpenDoorResponse(rawResponse);
        }
    }

    private OpenDoorHttpResponse parseOpenDoorResponse(byte[] rawResponse) {
        String headerText = new String(rawResponse, StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\\r\\n");

        int statusCode = 0;
        if (lines.length > 0) {
            String[] statusParts = lines[0].split(" ");
            if (statusParts.length > 1) {
                try {
                    statusCode = Integer.parseInt(statusParts[1]);
                } catch (NumberFormatException e) {
                    statusCode = 0;
                }
            }
        }

        String wwwAuthenticate = null;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.regionMatches(true, 0, "WWW-Authenticate:", 0, "WWW-Authenticate:".length())) {
                wwwAuthenticate = line.substring("WWW-Authenticate:".length()).trim();
                break;
            }
        }

        return new OpenDoorHttpResponse(statusCode, wwwAuthenticate);
    }

    private @Nullable String createDigestAuthorizationHeaderForOpenDoor(String challenge,
            DahuaDoorConfiguration localConfig, String path) throws Exception {
        if (!challenge.toLowerCase().startsWith("digest")) {
            return null;
        }

        Map<String, String> digestParams = parseDigestChallenge(challenge.substring(6).trim());
        String realm = digestParams.get("realm");
        String nonce = digestParams.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }

        String qop = digestParams.get("qop");
        if (qop != null && qop.contains(",")) {
            qop = qop.split(",")[0].trim();
        }

        String ha1 = md5Hex(localConfig.username + ":" + realm + ":" + localConfig.password);
        String ha2 = md5Hex("GET:" + path);

        StringBuilder auth = new StringBuilder("Digest ");
        auth.append("username=\"").append(escapeDigestValue(localConfig.username)).append("\"");
        auth.append(", realm=\"").append(escapeDigestValue(realm)).append("\"");
        auth.append(", nonce=\"").append(escapeDigestValue(nonce)).append("\"");
        auth.append(", uri=\"").append(path).append("\"");

        String response;
        if (qop != null && !qop.isBlank()) {
            String nonceCount = "00000001";
            String cnonce = randomHex(16);
            response = md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);
            auth.append(", qop=").append(qop);
            auth.append(", nc=").append(nonceCount);
            auth.append(", cnonce=\"").append(cnonce).append("\"");
        } else {
            response = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        }

        String opaque = digestParams.get("opaque");
        if (opaque != null) {
            auth.append(", opaque=\"").append(escapeDigestValue(opaque)).append("\"");
        }

        String algorithm = digestParams.get("algorithm");
        if (algorithm != null) {
            auth.append(", algorithm=").append(algorithm);
        }

        auth.append(", response=\"").append(response).append("\"");
        return auth.toString();
    }

    private SnapshotHttpResponse sendSnapshotRequest(DahuaDoorConfiguration localConfig,
            @Nullable String authorizationHeader) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(localConfig.hostname, 80), REQUEST_TIMEOUT_SECONDS * 1000);
            socket.setSoTimeout(REQUEST_TIMEOUT_SECONDS * 1000);

            StringBuilder request = new StringBuilder();
            request.append("GET ").append(SNAPSHOT_PATH).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(localConfig.hostname).append("\r\n");
            request.append("Connection: close\r\n");
            request.append("Accept: */*\r\n");
            if (authorizationHeader != null) {
                request.append("Authorization: ").append(authorizationHeader).append("\r\n");
            }
            request.append("\r\n");

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();

            byte[] rawResponse = readHttpResponse(socket.getInputStream());
            return parseHttpResponse(rawResponse);
        }
    }

    private byte[] readHttpResponse(InputStream inputStream) throws Exception {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();

        int state = 0;
        while (true) {
            int next = inputStream.read();
            if (next == -1) {
                break;
            }

            headerStream.write(next);

            if (state == 0 && next == '\r') {
                state = 1;
            } else if (state == 1 && next == '\n') {
                state = 2;
            } else if (state == 2 && next == '\r') {
                state = 3;
            } else if (state == 3 && next == '\n') {
                break;
            } else {
                state = 0;
            }
        }

        byte[] headerBytes = headerStream.toByteArray();
        int contentLength = extractContentLength(headerBytes);

        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        responseStream.write(headerBytes);

        byte[] buffer = new byte[4096];
        if (contentLength >= 0) {
            int remaining = contentLength;
            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                responseStream.write(buffer, 0, read);
                remaining -= read;
            }
        } else {
            while (true) {
                try {
                    int read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    responseStream.write(buffer, 0, read);
                } catch (SocketTimeoutException e) {
                    logger.debug("Snapshot response read timed out without Content-Length; using partial body");
                    break;
                }
            }
        }

        return responseStream.toByteArray();
    }

    private int extractContentLength(byte[] headerBytes) {
        String headerText = new String(headerBytes, StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\\r\\n");
        for (String line : lines) {
            if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
                String value = line.substring("Content-Length:".length()).trim();
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private SnapshotHttpResponse parseHttpResponse(byte[] rawResponse) {
        int headerEnd = -1;
        for (int i = 0; i < rawResponse.length - 3; i++) {
            if (rawResponse[i] == '\r' && rawResponse[i + 1] == '\n' && rawResponse[i + 2] == '\r'
                    && rawResponse[i + 3] == '\n') {
                headerEnd = i + 4;
                break;
            }
        }

        if (headerEnd < 0) {
            return new SnapshotHttpResponse(0, Map.of(), new byte[0]);
        }

        String headerText = new String(rawResponse, 0, headerEnd, StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\\r\\n");

        int statusCode = 0;
        if (lines.length > 0) {
            String[] statusParts = lines[0].split(" ");
            if (statusParts.length > 1) {
                try {
                    statusCode = Integer.parseInt(statusParts[1]);
                } catch (NumberFormatException e) {
                    statusCode = 0;
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                headers.put(name, value);
            }
        }

        byte[] body = java.util.Arrays.copyOfRange(rawResponse, headerEnd, rawResponse.length);
        return new SnapshotHttpResponse(statusCode, headers, body);
    }

    private @Nullable String createDigestAuthorizationHeader(String challenge, DahuaDoorConfiguration localConfig)
            throws Exception {
        if (!challenge.toLowerCase().startsWith("digest")) {
            return null;
        }

        Map<String, String> digestParams = parseDigestChallenge(challenge.substring(6).trim());
        String realm = digestParams.get("realm");
        String nonce = digestParams.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }

        String qop = digestParams.get("qop");
        if (qop != null && qop.contains(",")) {
            qop = qop.split(",")[0].trim();
        }

        String ha1 = md5Hex(localConfig.username + ":" + realm + ":" + localConfig.password);
        String ha2 = md5Hex("GET:" + SNAPSHOT_PATH);

        StringBuilder auth = new StringBuilder("Digest ");
        auth.append("username=\"").append(escapeDigestValue(localConfig.username)).append("\"");
        auth.append(", realm=\"").append(escapeDigestValue(realm)).append("\"");
        auth.append(", nonce=\"").append(escapeDigestValue(nonce)).append("\"");
        auth.append(", uri=\"").append(SNAPSHOT_PATH).append("\"");

        String response;
        if (qop != null && !qop.isBlank()) {
            String nonceCount = "00000001";
            String cnonce = randomHex(16);
            response = md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);
            auth.append(", qop=").append(qop);
            auth.append(", nc=").append(nonceCount);
            auth.append(", cnonce=\"").append(cnonce).append("\"");
        } else {
            response = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        }

        String opaque = digestParams.get("opaque");
        if (opaque != null) {
            auth.append(", opaque=\"").append(escapeDigestValue(opaque)).append("\"");
        }

        String algorithm = digestParams.get("algorithm");
        if (algorithm != null) {
            auth.append(", algorithm=").append(algorithm);
        }

        auth.append(", response=\"").append(response).append("\"");
        return auth.toString();
    }

    private Map<String, String> parseDigestChallenge(String challenge) {
        Map<String, String> result = new HashMap<>();
        int index = 0;

        while (index < challenge.length()) {
            while (index < challenge.length()
                    && (challenge.charAt(index) == ',' || Character.isWhitespace(challenge.charAt(index)))) {
                index++;
            }

            int equals = challenge.indexOf('=', index);
            if (equals < 0) {
                break;
            }

            String key = challenge.substring(index, equals).trim().toLowerCase();
            index = equals + 1;

            String value;
            if (index < challenge.length() && challenge.charAt(index) == '"') {
                index++;
                int end = challenge.indexOf('"', index);
                if (end < 0) {
                    break;
                }
                value = challenge.substring(index, end);
                index = end + 1;
            } else {
                int comma = challenge.indexOf(',', index);
                if (comma < 0) {
                    value = challenge.substring(index).trim();
                    index = challenge.length();
                } else {
                    value = challenge.substring(index, comma).trim();
                    index = comma + 1;
                }
            }

            result.put(key, value);
        }

        return result;
    }

    private String md5Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.ISO_8859_1));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String randomHex(int length) {
        byte[] bytes = new byte[length / 2];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(length);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String escapeDigestValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public void dispose() {
        final HttpClient localHttpClient = httpClient;
        final DigestAuthentication localDigestAuth = digestAuth;
        final URI localAuthUri = authUri;

        if (localHttpClient != null && localDigestAuth != null && localAuthUri != null) {
            AuthenticationStore auth = localHttpClient.getAuthenticationStore();
            auth.removeAuthentication(localDigestAuth);
            logger.debug("Removed digest authentication from shared HttpClient for URI {}", localAuthUri);
        }

        digestAuth = null;
        authUri = null;
        httpClient = null;
        config = null;
    }

    private record SnapshotHttpResponse(int statusCode, Map<String, String> headers, byte[] body) {
    }

    private record OpenDoorHttpResponse(int statusCode, @Nullable String wwwAuthenticate) {
    }
}
