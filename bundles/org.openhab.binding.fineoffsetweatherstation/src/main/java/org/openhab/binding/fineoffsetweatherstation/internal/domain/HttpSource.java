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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Declares where a measurand's value is found in the Ecowitt HTTP {@code get_livedata_info} response,
 * attached to a measurand through its {@code http} / {@code httpAlt} fluent methods.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
final class HttpSource {
    private final HttpGroup group;
    // explicit HTTP item code (used when it differs from the TCP code, e.g. the piezo rain channels)
    private final @Nullable Integer httpCode;
    // string id (e.g. "srain_piezo") or named field (e.g. "intemp"); null means "reuse the TCP item code"
    private final @Nullable String key;
    // whether this measurand is the dimension alternate for an already-registered code (see SOLAR_RADIATION)
    private final boolean alternate;

    HttpSource(HttpGroup group, @Nullable Integer httpCode, @Nullable String key, boolean alternate) {
        this.group = group;
        this.httpCode = httpCode;
        this.key = key;
        this.alternate = alternate;
    }

    /**
     * @return the lookup key: the explicit string id/field, the explicit HTTP item code, or - as a fallback for a
     *         measurand declaring {@code http(group)} - its single TCP item code
     */
    String resolveKey(@Nullable Integer tcpCode) {
        String explicitKey = key;
        if (explicitKey != null) {
            return explicitKey;
        }
        Integer explicitCode = httpCode;
        if (explicitCode != null) {
            return codeKey(explicitCode);
        }
        return codeKey(Objects.requireNonNull(tcpCode));
    }

    HttpGroup getGroup() {
        return group;
    }

    boolean isAlternate() {
        return alternate;
    }

    private static String codeKey(int code) {
        return "0x" + Integer.toHexString(code & 0xFF);
    }
}
