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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * How the project shows a group address. ETS stores every group address as its raw 16-bit number.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum KnxGroupAddressStyle {
    /** main / middle / sub, 5 / 3 / 8 bits */
    THREE_LEVEL("ThreeLevel"),
    /** main / sub, 5 / 11 bits */
    TWO_LEVEL("TwoLevel"),
    /** the raw number */
    FREE("Free");

    private final String projectValue;

    KnxGroupAddressStyle(String projectValue) {
        this.projectValue = projectValue;
    }

    /** Formats a raw group address number (0 to 65535) in this style. */
    public String format(int raw) {
        return switch (this) {
            case THREE_LEVEL -> (raw >> 11) + "/" + ((raw >> 8) & 0x7) + "/" + (raw & 0xFF);
            case TWO_LEVEL -> (raw >> 11) + "/" + (raw & 0x7FF);
            case FREE -> Integer.toString(raw);
        };
    }

    /** The style for a {@code GroupAddressStyle} value of {@code project.xml}, or {@code null} if unknown. */
    static @Nullable KnxGroupAddressStyle fromProjectValue(@Nullable String value) {
        for (KnxGroupAddressStyle style : values()) {
            if (style.projectValue.equals(value)) {
                return style;
            }
        }
        return null;
    }
}
