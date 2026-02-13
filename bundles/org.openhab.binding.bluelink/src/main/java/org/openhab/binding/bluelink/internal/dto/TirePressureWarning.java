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
public record TirePressureWarning(@Override @SerializedName("tirePressureLampAll") int all,
        @Override @SerializedName("tirePressureLampFL") int frontLeft,
        @Override @SerializedName("tirePressureLampFR") int frontRight,
        @Override @SerializedName("tirePressureLampRL") int rearLeft,
        @Override @SerializedName("tirePressureLampRR") int rearRight) implements TirePressureWarnings {
}
