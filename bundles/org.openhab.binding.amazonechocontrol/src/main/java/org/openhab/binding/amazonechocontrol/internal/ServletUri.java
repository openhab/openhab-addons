/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.eclipse.jetty.util.StringUtil.isNotBlank;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlServlet.SERVLET_PATH;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ServletUri} is the record for structured handling of the servlet URI
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault({})
public record ServletUri(String account, String request) {
    private static final Pattern URI_PART_PATTERN = Pattern.compile(SERVLET_PATH + "(?:/(\\w+)(/.+)?)?/?");

    public String buildFor(String uri) {
        if (uri.startsWith("/")) {
            return SERVLET_PATH + "/" + account() + uri;
        } else {
            return SERVLET_PATH + "/" + account() + "/" + uri;
        }
    }

    public static @Nullable ServletUri fromFullUri(@Nullable String requestUri) throws IllegalArgumentException {
        if (requestUri == null) {
            return null;
        }
        Matcher matcher = URI_PART_PATTERN.matcher(requestUri);
        if (!matcher.matches()) {
            return null;
        }
        return new ServletUri(isNotBlank(matcher.group(1)) ? matcher.group(1) : "",
                isNotBlank(matcher.group(2)) ? matcher.group(2) : "");
    }
}
