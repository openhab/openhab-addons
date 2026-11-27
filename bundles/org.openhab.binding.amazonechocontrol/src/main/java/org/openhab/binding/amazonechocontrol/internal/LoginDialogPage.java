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
package org.openhab.binding.amazonechocontrol.internal;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LoginDialogPage} is one page of Amazon's login dialog as the servlet forwards it to the browser, addressed
 * as {@code /FORWARD/<host>/<pathAndQuery>} below the account path. The dialog starts on
 * {@link AmazonEchoControlBindingConstants#SIGN_IN_HOST} and may continue on the account's retail host; pages on any
 * other host are not forwarded.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
record LoginDialogPage(String host, String pathAndQuery) {
    static final String FORWARD_URI_PART = "/FORWARD/";
    private static final String SCHEME = "https";

    static LoginDialogPage signIn() {
        return new LoginDialogPage(AmazonEchoControlBindingConstants.SIGN_IN_HOST, "/ap/signin");
    }

    static @Nullable LoginDialogPage fromRequest(String request, Collection<String> dialogHosts) {
        if (!request.startsWith(FORWARD_URI_PART)) {
            return null;
        }
        String hostAndPath = request.substring(FORWARD_URI_PART.length());
        int pathStart = hostAndPath.indexOf('/');
        String host = pathStart < 0 ? hostAndPath : hostAndPath.substring(0, pathStart);
        String pathAndQuery = pathStart < 0 ? "/" : hostAndPath.substring(pathStart);
        return dialogHosts.contains(host) ? new LoginDialogPage(host, pathAndQuery) : null;
    }

    static @Nullable LoginDialogPage fromLocation(String location, LoginDialogPage current,
            Collection<String> dialogHosts) {
        if (location.startsWith("/") && !location.startsWith("//")) {
            return new LoginDialogPage(current.host, location);
        }
        URI uri;
        try {
            uri = URI.create(location);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String host = uri.getHost();
        if (host == null || uri.getUserInfo() != null || !SCHEME.equalsIgnoreCase(uri.getScheme())
                || (uri.getPort() != -1 && uri.getPort() != 443)) {
            return null;
        }
        host = host.toLowerCase(Locale.ROOT);
        if (!dialogHosts.contains(host)) {
            return null;
        }
        String path = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String query = uri.getRawQuery();
        return new LoginDialogPage(host, query == null ? path : path + "?" + query);
    }

    /**
     * Amazon echoes rewritten links back as data (a hidden {@code openid.return_to}, a query parameter, a redirect), so
     * everything that goes back to Amazon is translated to the page URL first
     */
    static String unrewrite(String text, ServletUri uriParts) {
        String forwardBase = uriParts.buildFor(FORWARD_URI_PART);
        String originPrefix = SCHEME + "://";
        return text.replace(forwardBase, originPrefix).replace(URLEncoder.encode(forwardBase, StandardCharsets.UTF_8),
                URLEncoder.encode(originPrefix, StandardCharsets.UTF_8));
    }

    String origin() {
        return SCHEME + "://" + host;
    }

    String url() {
        return origin() + pathAndQuery;
    }

    String servletPath(ServletUri uriParts) {
        return uriParts.buildFor(FORWARD_URI_PART + host + pathAndQuery);
    }

    /** the servlet path that links of this page are rewritten to, so that they stay on the page's host */
    String linkBase(ServletUri uriParts) {
        return uriParts.buildFor(FORWARD_URI_PART + host + "/");
    }
}
