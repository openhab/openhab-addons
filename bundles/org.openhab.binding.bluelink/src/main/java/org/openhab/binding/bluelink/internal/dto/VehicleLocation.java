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
 * Vehicle location data from the Bluelink API.
 *
 * @author Marcus Better - Initial contribution
 */
public record VehicleLocation(Coordinates coord) {

    public record Coordinates(@SerializedName("lat") double latitude, @SerializedName("lon") double longitude,
            @SerializedName("alt") double altitude) {
    }
}
