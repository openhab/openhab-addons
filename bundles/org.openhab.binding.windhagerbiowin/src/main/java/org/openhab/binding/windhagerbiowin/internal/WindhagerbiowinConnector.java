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
package org.openhab.binding.windhagerbiowin.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Connector for the BioWin webserver.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerbiowinConnector {

    private static final String API_DOCS_PATH = "api-docs/";
    private static final String DATAPOINT_API_PATH = "api/1.0/datapoint/";
    private static final Pattern DIGEST_PARAMETER_PATTERN = Pattern.compile("(\\w+)=((?:\"[^\"]*\")|(?:[^,\\s]+))");

    private final Logger logger = LoggerFactory.getLogger(WindhagerbiowinConnector.class);

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    public WindhagerbiowinConnector(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Establishes a connection to the BioWin webserver and validates that it answers successfully.
     *
     * @return true if the webserver responds successfully, false otherwise
     */
    public boolean connect() {
        HttpResponse<String> response = sendRequest(API_DOCS_PATH);
        return response != null && response.statusCode() >= 200 && response.statusCode() < 400;
    }

    /**
     * Reads a numeric value from a BioWin webserver path.
     *
     * @param path the BioWin path, for example {@code 0/802/435/heat}
     * @return the parsed value, or {@code null} if the request fails or the response is not numeric
     */
    public @Nullable BigDecimal readValue(String path) {
        if (path.isBlank()) {
            return null;
        }

        HttpResponse<String> response = sendRequest(DATAPOINT_API_PATH + normalizeOid(path));
        if (response == null || response.statusCode() < 200 || response.statusCode() >= 300) {
            return null;
        }

        try {
            return new BigDecimal(readValueElement(response.body()).getAsString());
        } catch (RuntimeException e) {
            logger.debug("BioWin returned a non-numeric value for path {}", path);
            return null;
        }
    }

    public @Nullable String readString(String path) {
        if (path.isBlank()) {
            return null;
        }

        HttpResponse<String> response = sendRequest(DATAPOINT_API_PATH + normalizeOid(path));
        if (response == null || response.statusCode() < 200 || response.statusCode() >= 300) {
            return null;
        }

        try {
            return readValueElement(response.body()).getAsString();
        } catch (RuntimeException e) {
            logger.debug("BioWin returned an invalid response for path {}", path);
            return null;
        }
    }

    private @Nullable HttpResponse<String> sendRequest(String path) {
        String normalizedPath = path.isBlank() ? "" : "/" + path.replaceFirst("^/+", "");
        URI uri = URI.create("http://" + hostname + ":" + port + normalizedPath);

        try {
            HttpResponse<String> response = sendRequest(uri, null);
            if (response != null && response.statusCode() == 401) {
                String challenge = response.headers().firstValue("WWW-Authenticate").orElse("");
                String authorization = createDigestAuthorization(challenge, uri, "GET");
                if (authorization != null) {
                    response = sendRequest(uri, authorization);
                }
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while requesting BioWin path {}", path);
        } catch (Exception e) {
            logger.debug("Unable to request BioWin path {}: {}", path, e.getMessage());
        }
        return null;
    }

    private static JsonElement readValueElement(String body) {
        JsonObject response = JsonParser.parseString(body).getAsJsonObject();
        JsonElement value = response.get("value");
        if (value == null || value.isJsonNull()) {
            throw new IllegalArgumentException("BioWin response does not contain a value");
        }
        return value;
    }

    private static String normalizeOid(String path) {
        return path.replaceFirst("^/+", "");
    }

    private @Nullable HttpResponse<String> sendRequest(URI uri, @Nullable String authorization)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).GET();
        if (authorization != null) {
            requestBuilder.header("Authorization", authorization);
        }
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private @Nullable String createDigestAuthorization(String challenge, URI uri, String method) {
        if (!challenge.regionMatches(true, 0, "Digest", 0, "Digest".length())) {
            return null;
        }

        Map<String, String> parameters = parseDigestParameters(challenge.substring("Digest".length()));
        String realm = parameters.get("realm");
        String nonce = parameters.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }

        String qop = selectQop(parameters.get("qop"));
        String cnonce = randomToken();
        String nonceCount = "00000001";
        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        String requestPath = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String ha2 = md5Hex(method + ":" + requestPath);
        String response = qop == null ? md5Hex(ha1 + ":" + nonce + ":" + ha2)
                : md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);

        StringBuilder authorization = new StringBuilder("Digest username=\"").append(escape(username))
                .append("\", realm=\"").append(escape(realm)).append("\", nonce=\"").append(escape(nonce))
                .append("\", uri=\"").append(escape(requestPath)).append("\", response=\"").append(response)
                .append("\"");
        if (qop != null) {
            authorization.append(", qop=").append(qop).append(", nc=").append(nonceCount).append(", cnonce=\"")
                    .append(cnonce).append("\"");
        }
        return authorization.toString();
    }

    private static Map<String, String> parseDigestParameters(String challenge) {
        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = DIGEST_PARAMETER_PATTERN.matcher(challenge);
        while (matcher.find()) {
            String value = matcher.group(2);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            parameters.put(matcher.group(1).toLowerCase(), value);
        }
        return parameters;
    }

    private static @Nullable String selectQop(@Nullable String qop) {
        if (qop == null) {
            return null;
        }
        for (String option : qop.split(",")) {
            if ("auth".equalsIgnoreCase(option.trim())) {
                return "auth";
            }
        }
        return null;
    }

    private static String randomToken() {
        return Long.toHexString(System.nanoTime());
    }

    private static String md5Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                result.append(String.format("%02x", item & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is required for HTTP Digest authentication", e);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
