/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.codes;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link WorxLandroidErrorCodes} hosts error codes
 *
 * @author Nils Billing - Initial contribution
 */
@NonNullByDefault
public enum WorxLandroidErrorCodes {
    @SerializedName("-1")
    UNKNOWN,
    @SerializedName("0")
    NO_ERR,
    @SerializedName("1")
    TRAPPED,
    @SerializedName("2")
    LIFTED,
    @SerializedName("3")
    WIRE_MISSING,
    @SerializedName("4")
    OUTSIDE_WIRE,
    @SerializedName("5")
    RAINING,
    @SerializedName("6")
    CLOSE_DOOR_TO_MOW,
    @SerializedName("7")
    CLOSE_DOOR_TO_GO_HOME,
    @SerializedName("8")
    BLADE_MOTOR_BLOCKED,
    @SerializedName("9")
    WHEEL_MOTOR_BLOCKED,
    @SerializedName("10")
    TRAPPED_TIMEOUT,
    @SerializedName("11")
    UPSIDE_DOWN,
    @SerializedName("12")
    BATTERY_LOW,
    @SerializedName("13")
    REVERSE_WIRE,
    @SerializedName("14")
    CHARGE_ERROR,
    @SerializedName("15")
    TIMEOUT_FINDING_HOME,
    @SerializedName("16")
    MOWER_LOCKED,
    @SerializedName("17")
    BATTERY_OVER_TEMPERATURE,
    @SerializedName("20")
    MOWER_OUTSIDE_WIRE;
}
