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
package org.openhab.binding.motionblinds.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceStatus} is the {@code data} part of a status message. All fields are optional: motors only
 * report what they support, and {@code operation} is omitted from some messages.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class DeviceStatus {
    /** 0 = fully open, 100 = fully closed */
    public @Nullable Integer currentPosition;
    /** 0..180 degrees */
    public @Nullable Integer currentAngle;
    /** 0 = closing, 1 = opening, 2 = stopped */
    public @Nullable Integer operation;
    /** Limit status */
    public @Nullable Integer currentState;
    /** Blind type */
    public @Nullable Integer type;
    /** Wi-Fi signal strength in dBm */
    @SerializedName("RSSI")
    public @Nullable Integer rssi;
}
