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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helpers for negotiating the MeterValues measurand list with a charger.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class Measurands {

    private static final String SEPARATOR = ",";

    private Measurands() {
    }

    public static String dropLast(@Nullable String list) {
        if (list == null) {
            return "";
        }
        List<String> entries = Arrays.stream(list.split(SEPARATOR)) //
                .map(String::trim) //
                .filter(entry -> !entry.isEmpty()) //
                .collect(Collectors.toList());
        if (entries.isEmpty()) {
            return "";
        }
        return String.join(SEPARATOR, entries.subList(0, entries.size() - 1));
    }
}
