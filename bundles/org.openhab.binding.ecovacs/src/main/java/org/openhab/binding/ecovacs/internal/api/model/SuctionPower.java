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
package org.openhab.binding.ecovacs.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public enum SuctionPower {
    @SerializedName("standard")
    NORMAL,
    @SerializedName("strong")
    HIGH,
    HIGHER,
    SILENT;

    public static SuctionPower fromJsonValue(int value) {
        switch (value) {
            case 1000:
                return SILENT;
            case 1:
                return HIGH;
            case 2:
                return HIGHER;
            default:
                return NORMAL;
        }
    }

    public int toJsonValue() {
        switch (this) {
            case HIGH:
                return 1;
            case HIGHER:
                return 2;
            case SILENT:
                return 1000;
            default: // NORMAL
                return 0;
        }
    }

    public String toXmlValue() {
        if (this == HIGH) {
            return "strong";
        }
        return "standard";
    }
}
