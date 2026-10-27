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
package org.openhab.binding.icalendar.internal.handler;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper for HTTP digest authentication as described in RFC 7616 (and RFC 2617).
 * It evaluates the challenge of a <code>WWW-Authenticate</code> header and creates the matching
 * <code>Authorization</code> header for a single request.
 * Supported are the algorithms <code>MD5</code>, <code>MD5-sess</code>, <code>SHA-256</code> and
 * <code>SHA-256-sess</code> with <code>qop=auth</code> or without qop.
 *
 * @author Contributors - Initial contribution
 */
@NonNullByDefault
final class DigestAuthentication {
    private static final Pattern DIGEST_CHALLENGE = Pattern.compile("(?:^|,)\\s*Digest\\s+(.*)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PARAMETER = Pattern
            .compile("(\\w+)\\s*=\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^,\\s]*))");
    private static final String NONCE_COUNT = "00000001";
    private static final SecureRandom RANDOM = new SecureRandom();

    private DigestAuthentication() {
        // utility class
    }

    /**
     * Searches the values of all <code>WWW-Authenticate</code> headers for a digest challenge.
     *
     * @param headerValues the values of the <code>WWW-Authenticate</code> headers of a response
     * @return the challenge parameters (without the scheme) or <code>null</code> if there is no digest challenge
     */
    static @Nullable String findChallenge(List<String> headerValues) {
        for (String value : headerValues) {
            Matcher matcher = DIGEST_CHALLENGE.matcher(value.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Creates the <code>Authorization</code> header value for a request.
     *
     * @param challenge the challenge parameters as returned by {@link #findChallenge(List)}
     * @param method the HTTP method of the request
     * @param uri the URI of the request
     * @param username the username
     * @param password the password
     * @return the header value or <code>null</code> if the challenge is not supported
     */
    static @Nullable String createAuthorization(String challenge, String method, URI uri, String username,
            String password) {
        byte[] random = new byte[8];
        RANDOM.nextBytes(random);
        return createAuthorization(challenge, method, requestTarget(uri), username, password,
                HexFormat.of().formatHex(random));
    }

    /**
     * Creates the <code>Authorization</code> header value for a request.
     *
     * @param challenge the challenge parameters as returned by {@link #findChallenge(List)}
     * @param method the HTTP method of the request
     * @param requestTarget path and query of the request, as sent in the request line
     * @param username the username
     * @param password the password
     * @param cnonce the client nonce
     * @return the header value or <code>null</code> if the challenge is not supported
     */
    static @Nullable String createAuthorization(String challenge, String method, String requestTarget, String username,
            String password, String cnonce) {
        Map<String, String> parameters = parseParameters(challenge);
        String realm = parameters.get("realm");
        String nonce = parameters.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }

        String algorithm = parameters.getOrDefault("algorithm", "MD5");
        String digestName;
        boolean session;
        switch (algorithm.toUpperCase(Locale.ROOT)) {
            case "MD5":
                digestName = "MD5";
                session = false;
                break;
            case "MD5-SESS":
                digestName = "MD5";
                session = true;
                break;
            case "SHA-256":
                digestName = "SHA-256";
                session = false;
                break;
            case "SHA-256-SESS":
                digestName = "SHA-256";
                session = true;
                break;
            default:
                return null;
        }

        boolean useQop = false;
        String offeredQop = parameters.get("qop");
        if (offeredQop != null) {
            for (String option : offeredQop.split(",")) {
                if ("auth".equalsIgnoreCase(option.trim())) {
                    useQop = true;
                }
            }
            if (!useQop) {
                // e.g. only auth-int, which is not supported
                return null;
            }
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String ha1 = hash(digest, username + ":" + realm + ":" + password);
        if (session) {
            ha1 = hash(digest, ha1 + ":" + nonce + ":" + cnonce);
        }
        String ha2 = hash(digest, method + ":" + requestTarget);
        String response;
        if (useQop) {
            response = hash(digest, ha1 + ":" + nonce + ":" + NONCE_COUNT + ":" + cnonce + ":auth:" + ha2);
        } else {
            response = hash(digest, ha1 + ":" + nonce + ":" + ha2);
        }

        StringBuilder header = new StringBuilder("Digest ");
        header.append("username=\"").append(escape(username)).append("\"");
        header.append(", realm=\"").append(escape(realm)).append("\"");
        header.append(", nonce=\"").append(escape(nonce)).append("\"");
        header.append(", uri=\"").append(escape(requestTarget)).append("\"");
        if (parameters.containsKey("algorithm")) {
            header.append(", algorithm=").append(algorithm);
        }
        header.append(", response=\"").append(response).append("\"");
        if (useQop) {
            header.append(", qop=auth, nc=").append(NONCE_COUNT).append(", cnonce=\"").append(cnonce).append("\"");
        }
        String opaque = parameters.get("opaque");
        if (opaque != null) {
            header.append(", opaque=\"").append(escape(opaque)).append("\"");
        }
        return header.toString();
    }

    private static String requestTarget(URI uri) {
        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        String query = uri.getRawQuery();
        return query == null ? path : path + "?" + query;
    }

    private static String hash(MessageDigest digest, String input) {
        return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> parseParameters(String challenge) {
        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = PARAMETER.matcher(challenge);
        while (matcher.find()) {
            String quoted = matcher.group(2);
            String value = quoted != null ? quoted.replaceAll("\\\\(.)", "$1") : matcher.group(3);
            parameters.putIfAbsent(matcher.group(1).toLowerCase(Locale.ROOT), value == null ? "" : value);
        }
        return parameters;
    }
}
