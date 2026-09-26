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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validates every URL before credentials can be sent.
 * 
 * @author Andreas Vilippus - Initial contribution
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
        return uri.normalize();
    }

    public static URI resolve(URI origin, String reference) {
        URI target = validate(origin.resolve(reference));
        if (!origin.getScheme().equalsIgnoreCase(target.getScheme())
                || !origin.getHost().equalsIgnoreCase(target.getHost()) || port(origin) != port(target)) {
            throw new IllegalArgumentException("Cross-origin CalDAV resource is not permitted");
        }
        return target;
    }

    private static int port(URI uri) {
        return uri.getPort() < 0 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
    }
}
