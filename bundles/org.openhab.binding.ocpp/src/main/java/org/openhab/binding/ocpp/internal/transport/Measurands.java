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
package org.openhab.binding.ocpp.internal.transport;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helpers for negotiating the MeterValues measurand list with a charger.
 *
 * <p>
 * OCPP 1.6 has no way to ask a charger which measurands it supports; the only signal that one is
 * unsupported is a ChangeConfiguration Reject. So the supported set is found by elimination — drop
 * the last measurand and try again — rather than from any hardcoded list of "difficult" measurands.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class Measurands {

    private static final String SEPARATOR = ",";

    private Measurands() {
    }

    /**
     * Return {@code list} with its final comma-separated entry removed and every remaining entry
     * trimmed. Returns the empty string when there is nothing left to drop to (null input, or a list
     * that holds at most one entry).
     */
    public static String dropLast(@Nullable String list) {
        if (list == null) {
            return "";
        }
        int finalSeparator = list.lastIndexOf(SEPARATOR);
        if (finalSeparator < 0) {
            return "";
        }
        String retained = list.substring(0, finalSeparator);
        return Arrays.stream(retained.split(SEPARATOR)) //
                .map(String::trim) //
                .filter(entry -> !entry.isEmpty()) //
                .collect(Collectors.joining(SEPARATOR));
    }
}
