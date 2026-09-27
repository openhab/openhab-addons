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
package org.openhab.binding.bluelink.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Tire pressure warning status.
 *
 * @author Florian Hotze - Initial contribution
 */
public record TirePressureWarning(@Override @SerializedName("tirePressureLampAll") int rawAll,
        @Override @SerializedName("tirePressureLampFL") int rawFrontLeft,
        @Override @SerializedName("tirePressureLampFR") int rawFrontRight,
        @Override @SerializedName("tirePressureLampRL") int rawRearLeft,
        @Override @SerializedName("tirePressureLampRR") int rawRearRight) implements ITirePressureWarning {

    @Override
    public boolean all() {
        return rawAll > 0;
    }

    @Override
    public boolean frontLeft() {
        return rawFrontLeft > 0;
    }

    @Override
    public boolean frontRight() {
        return rawFrontRight > 0;
    }

    @Override
    public boolean rearLeft() {
        return rawRearLeft > 0;
    }

    @Override
    public boolean rearRight() {
        return rawRearRight > 0;
    }
}
