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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates every URL before credentials can be sent.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - RFC 3986 URI resolution
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
        URI ref = URI.create(reference);
        URI target;
        if (ref.isAbsolute()) {
            target = validate(ref);
        } else {
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
            target = validate(URI.create(base.getScheme() + "://" + authority + path
                    + (query == null ? "" : "?" + query) + (fragment == null ? "" : "#" + fragment)));
        }
        if (!base.getScheme().equalsIgnoreCase(target.getScheme()) || !base.getHost().equalsIgnoreCase(target.getHost())
                || port(base) != port(target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        return target;
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
