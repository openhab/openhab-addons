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
 * Sanitizes a free-text name (e.g. the {@code friendlyName} config parameter) into a value safe
 * to use as an EEBus SHIP mDNS service instance name.
 * <p>
 * That name is advertised via mDNS and echoed back as the TLS SNI value by at least some SHIP
 * clients connecting in - a name containing characters outside the LDH (letters/digits/hyphen)
 * charset, e.g. plain spaces, crashes the inbound TLS handshake with an unhandled
 * {@link IllegalArgumentException} in the JDK's strict SNI hostname validation. Confirmed live
 * against a real EEBus peer (meisel2000/eebus-cbsim) on 2026-08-01.
 *
 * @author openHAB EEBus Binding Contributors - Initial contribution
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
