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
 * The {@link WorxLandroidStatusCodes} hosts status codes
 *
 * @author Nils Billing - Initial contribution
 */
@NonNullByDefault
public enum WorxLandroidStatusCodes {
    @SerializedName("-1")
    UNKNOWN,
    @SerializedName("0")
    IDLE,
    @SerializedName("1")
    HOME,
    @SerializedName("2")
    START_SEQUENCE,
    @SerializedName("3")
    LEAVING_HOME,
    @SerializedName("4")
    FOLLOW_WIRE,
    @SerializedName("5")
    SEARCHING_HOME,
    @SerializedName("6")
    SEARCHING_WIRE,
    @SerializedName("7")
    MOWING,
    @SerializedName("8")
    LIFTED,
    @SerializedName("9")
    TRAPPED,
    @SerializedName("10")
    BLADE_BLOCKED,
    @SerializedName("11")
    DEBUG,
    @SerializedName("12")
    REMOTE_CONTROL,
    @SerializedName("13")
    ESCAPE_FROM_OLM,
    @SerializedName("30")
    GOING_HOME,
    @SerializedName("31")
    ZONE_TRAINING,
    @SerializedName("32")
    BORDER_CUT,
    @SerializedName("33")
    SEARCHING_ZONE,
    @SerializedName("34")
    PAUSE,
    @SerializedName("99")
    MANUAL_STOP;
}
