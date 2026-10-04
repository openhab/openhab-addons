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
package org.openhab.io.eebus.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reduces a configured name to letters, digits and hyphens (LDH), for use as the SHIP ID, the mDNS
 * service instance name and the certificate CN.
 * <p>
 * SHIP clients connecting in send the mDNS host name (derived from the SHIP ID) or the service
 * instance name as TLS SNI, and the JDK rejects a non-LDH SNI value during the handshake.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ServiceNameSanitizer {

    private static final Pattern UNSAFE_CHARS = Pattern.compile("[^A-Za-z0-9-]+");
    private static final Pattern LEADING_TRAILING_HYPHENS = Pattern.compile("^-+|-+$");
    private static final String FALLBACK = "openHAB";

    private ServiceNameSanitizer() {
    }

    public static String sanitize(String name) {
        String sanitized = UNSAFE_CHARS.matcher(name).replaceAll("-");
        sanitized = LEADING_TRAILING_HYPHENS.matcher(sanitized).replaceAll("");
        return sanitized.isEmpty() ? FALLBACK : sanitized;
    }
}
