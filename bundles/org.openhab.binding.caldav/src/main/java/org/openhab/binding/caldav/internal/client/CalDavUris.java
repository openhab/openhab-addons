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
package org.openhab.binding.caldav.internal.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates and resolves network URLs and provides canonical identities with lossless compact references.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - RFC 3986 URI resolution
 * @author Andreas Vilippus - Canonical URI identities and compact references
 */
@NonNullByDefault
public final class CalDavUris {
    private CalDavUris() {
    }

    public static URI validate(URI uri) {
        String host = uri.getHost();
        boolean loopback = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "[::1]".equals(host)
                || "::1".equals(host);
        if (host == null || uri.getUserInfo() != null || uri.getFragment() != null
                || !("https".equalsIgnoreCase(uri.getScheme())
                        || loopback && "http".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("A valid HTTPS URL without user information or fragment is required");
        }
        String path = removeDotSegments(uri.getRawPath());
        @Nullable
        String query = uri.getRawQuery();
        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority() + path + (query == null ? "" : "?" + query));
    }

    public static URI resolve(URI origin, String reference) {
        URI base = validate(origin);
        URI target = validate(resolveReference(base, reference));
        if (!sameOrigin(base, target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        return target;
    }

    private static URI resolveReference(URI base, String reference) {
        URI ref = URI.create(reference);
        if (ref.isAbsolute()) {
            return ref;
        }
        @Nullable
        String authority = ref.getRawAuthority();
        String path = ref.getRawPath();
        @Nullable
        String query = ref.getRawQuery();
        if (authority == null) {
            authority = base.getRawAuthority();
            if (path.isEmpty()) {
                path = base.getRawPath();
                query = query == null ? base.getRawQuery() : query;
            } else if (!path.startsWith("/")) {
                String basePath = base.getRawPath();
                path = (basePath.isEmpty() ? "/" : basePath.substring(0, basePath.lastIndexOf('/') + 1)) + path;
            }
        }
        @Nullable
        String fragment = ref.getRawFragment();
        return URI.create(base.getScheme() + "://" + authority + path + (query == null ? "" : "?" + query)
                + (fragment == null ? "" : "#" + fragment));
    }

    /**
     * Returns a validated absolute identity, preserving path case, empty segments, reserved escapes and query presence.
     */
    public static URI canonicalize(URI uri) {
        validate(uri);
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        int port = uri.getPort();
        String authority = uri.getHost().toLowerCase(Locale.ROOT)
                + (port < 0 || port == ("https".equals(scheme) ? 443 : 80) ? "" : ":" + port);
        String path = uri.getRawPath();
        @Nullable
        String query = uri.getRawQuery();
        // Decode unreserved escapes before removing dot segments so the resulting identity is idempotent.
        return validate(URI.create(scheme + "://" + authority + normalizeEscapes(path.isEmpty() ? "/" : path)
                + (query == null ? "" : "?" + normalizeEscapes(query))));
    }

    /**
     * Compares validated origins by scheme, host and effective port, independently of their paths.
     */
    public static boolean sameOrigin(URI first, URI second) {
        URI left = validate(first);
        URI right = validate(second);
        return left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost().equalsIgnoreCase(right.getHost())
                && port(left) == port(right);
    }

    /**
     * Returns a same-origin reference independent of the parent's path, including collections outside an account path.
     */
    public static String originRelative(URI parent, URI absoluteUri) {
        URI target = canonicalize(absoluteUri);
        if (!sameOrigin(parent, target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        String path = target.getRawPath();
        // A leading double slash would be parsed as an authority rather than as empty path segments.
        String reference = (path.startsWith("//") ? "/." + path : path) + querySuffix(target);
        if (!isRoundTrip(parent, target, reference)) {
            throw new IllegalArgumentException("CalDAV reference does not preserve resource identity");
        }
        return reference;
    }

    /**
     * Compacts descendants relative to a directory parent; other resources use a lossless origin-relative fallback.
     * A nonempty parent path without a trailing slash is not treated as a directory; no parent traversal is generated.
     */
    public static String compact(URI parent, URI absoluteUri) {
        URI base = canonicalize(parent);
        URI target = canonicalize(absoluteUri);
        if (!sameOrigin(base, target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        String[] parentSegments = base.getRawPath().split("/", -1);
        String[] targetSegments = target.getRawPath().split("/", -1);
        int prefixLength = parentSegments.length - 1;
        if (base.getRawPath().endsWith("/") && targetSegments.length > prefixLength
                && Arrays.equals(parentSegments, 0, prefixLength, targetSegments, 0, prefixLength)) {
            String path = String.join("/", Arrays.copyOfRange(targetSegments, prefixLength, targetSegments.length));
            // Protect empty leading segments and a colon in the first segment from URI syntax ambiguity.
            if (path.isEmpty() || path.startsWith("/") || path.split("/", 2)[0].contains(":")) {
                path = "./" + path;
            }
            String reference = path + querySuffix(target);
            if (isRoundTrip(parent, target, reference)) {
                return reference;
            }
        }
        return originRelative(parent, target);
    }

    /**
     * Reconstructs a canonical absolute network identity from compact or absolute references.
     */
    public static URI reconstruct(URI parent, String reference) {
        URI base = canonicalize(parent);
        // Canonicalize before dot removal: encoded and literal dots can otherwise select different resources.
        URI target = canonicalize(resolveReference(base, reference));
        if (!sameOrigin(base, target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        return target;
    }

    /**
     * Checks both RFC resolution and canonical reconstruction against the same identity; invalid references return
     * false.
     */
    public static boolean isRoundTrip(URI parent, URI absoluteUri, String reference) {
        try {
            URI target = canonicalize(absoluteUri);
            return sameOrigin(parent, target) && canonicalize(resolve(parent, reference)).equals(target)
                    && reconstruct(parent, reference).equals(target);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String querySuffix(URI uri) {
        @Nullable
        String query = uri.getRawQuery();
        return query == null ? "" : "?" + query;
    }

    private static String normalizeEscapes(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '%') {
                int code = Integer.parseInt(value.substring(i + 1, i + 3), 16);
                if (code >= 'a' && code <= 'z' || code >= 'A' && code <= 'Z' || code >= '0' && code <= '9'
                        || code == '-' || code == '.' || code == '_' || code == '~') {
                    result.append((char) code);
                } else {
                    result.append('%').append(value.substring(i + 1, i + 3).toUpperCase(Locale.ROOT));
                }
                i += 2;
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static String removeDotSegments(String path) {
        // URI.normalize/resolve collapse empty segments; DAV resource paths must preserve them (RFC 3986 section 5.2).
        String[] parts = path.split("/", -1);
        var segments = new ArrayList<String>(parts.length);
        for (int i = 0; i < parts.length; i++) {
            switch (parts[i]) {
                case "." -> {
                    if (i == parts.length - 1) {
                        segments.add("");
                    }
                }
                case ".." -> {
                    // Valid HTTP paths start with an empty root segment, which a parent reference cannot remove.
                    if (segments.size() > 1) {
                        segments.removeLast();
                    }
                    if (i == parts.length - 1) {
                        segments.add("");
                    }
                }
                default -> segments.add(parts[i]);
            }
        }
        return String.join("/", segments);
    }

    private static int port(URI uri) {
        return uri.getPort() < 0 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
    }
}
