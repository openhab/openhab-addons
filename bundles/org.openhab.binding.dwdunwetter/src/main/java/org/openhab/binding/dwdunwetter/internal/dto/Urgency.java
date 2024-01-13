/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.util.Arrays;

/**
 * Enum for the urgency of the warning.
 *
 * @author Martin Koehler - Initial contribution
 */
public enum Urgency {

    IMMEDIATE("Immediate"),
    FUTURE("Future"),
    UNKNOWN("Unknown");

    private final String text;

    private Urgency(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static Urgency getUrgency(String input) {
        return Arrays.asList(Urgency.values()).stream().filter(urg -> input.equalsIgnoreCase(urg.getText())).findAny()
                .orElse(UNKNOWN);
    }
}
