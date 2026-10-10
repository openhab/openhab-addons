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
package org.openhab.binding.veluxactive.internal.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Supported Velux window/cover types.
 * Unknown codes deserialize to null.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum VeluxType {
    @SerializedName("window")
    WINDOW(true),
    @SerializedName("blind")
    BLIND(false),
    @SerializedName("venetian_blind")
    VENETIAN_BLIND(false),
    @SerializedName("shutter")
    SHUTTER(true),
    @SerializedName("awning_blind")
    AWNING_BLIND(false);

    private final boolean isInverted;

    private VeluxType(boolean isInverted) {
        this.isInverted = isInverted;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String toCamelCase() {
        String[] parts = serializedName().split("_");
        return parts[0] + Arrays.stream(parts, 1, parts.length)
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1)).collect(Collectors.joining());
    }

    public boolean isInverted() {
        return isInverted;
    }
}
