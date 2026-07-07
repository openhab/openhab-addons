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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the onLastTimeStats MQTT event from GOAT mowers, containing
 * summary statistics about the last completed mowing session.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class LastTimeStatsReport {
    @SerializedName("start")
    public String startTimestamp = "0";

    @SerializedName("time")
    public int durationSeconds;

    @SerializedName("area")
    public int areaSqCm;
}
