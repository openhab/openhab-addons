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
package org.openhab.binding.ecobee.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AckType} represents the types of alert acknowledgements.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum AckType {

    @SerializedName("accept")
    ACCEPT("accept"),

    @SerializedName("decline")
    DECLINE("decline"),

    @SerializedName("defer")
    DEFER("defer"),

    @SerializedName("unacknowledged")
    UNACKNOWLEDGED("unacknowledged");

    private final String type;

    private AckType(String type) {
        this.type = type;
    }

    public static AckType forValue(@Nullable String v) {
        if (v != null) {
            for (AckType at : AckType.values()) {
                if (at.type.equals(v)) {
                    return at;
                }
            }
        }
        throw new IllegalArgumentException("Invalid or null ack type: " + v);
    }

    @Override
    public String toString() {
        return this.type;
    }
}
