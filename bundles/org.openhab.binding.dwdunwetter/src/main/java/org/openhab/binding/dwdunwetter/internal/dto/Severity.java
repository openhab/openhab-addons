/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Severity enum to make the severity comparable
 *
 * @author Martin Koehler - Initial contribution
 */
public enum Severity {

    EXTREME(1, "Extreme"),
    SEVERE(2, "Severe"),
    MODERATE(3, "Moderate"),
    MINOR(4, "Minor"),
    UNKNOWN(5, "Unknown");

    private int order;
    private String text;

    private Severity(int order, String text) {
        this.order = order;
        this.text = text;
    }

    public int getOrder() {
        return order;
    }

    public String getText() {
        return text;
    }

    public static Severity getSeverity(String input) {
        return Arrays.asList(Severity.values()).stream().filter(sev -> input.equalsIgnoreCase(sev.getText())).findAny()
                .orElse(UNKNOWN);
    }
}
