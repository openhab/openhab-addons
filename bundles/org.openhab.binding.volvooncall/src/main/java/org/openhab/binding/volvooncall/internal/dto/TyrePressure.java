/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TyrePressure} is responsible for storing
 * Tyre Pressure informations returned by vehicule status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TyrePressure {

    public enum PressureLevel {
        @SerializedName("Normal")
        NORMAL,
        @SerializedName("LowSoft")
        LOW_SOFT,
        UNKNOWN
    }

    public PressureLevel frontLeftTyrePressure = PressureLevel.UNKNOWN;
    public PressureLevel frontRightTyrePressure = PressureLevel.UNKNOWN;
    public PressureLevel rearLeftTyrePressure = PressureLevel.UNKNOWN;
    public PressureLevel rearRightTyrePressure = PressureLevel.UNKNOWN;

    /*
     * Currently unused in the binding, maybe interesting in the future
     * private ZonedDateTime timestamp;
     */
}
