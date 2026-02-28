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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.QuantityType;

import com.google.gson.annotations.SerializedName;

/**
 * Climate control request for ICE (internal combustion engine) vehicles.
 *
 * @author Marcus Better - Initial contribution
 */
public record ClimateRequestIce(int airCtrl, AirTemperature airTemp, boolean defrost, int heating1, int igniOnDuration,
        SeatHeaterVentInfo seatHeaterVentInfo, @SerializedName("Ims") int ims, String username, String vin) {

    public record SeatHeaterVentInfo(@SerializedName("drvSeatHeatState") int driverSeat,
            @SerializedName("astSeatHeatState") int passengerSeat, @SerializedName("rlSeatHeatState") int rearLeftSeat,
            @SerializedName("rrSeatHeatState") int rearRightSeat) {

        public static final SeatHeaterVentInfo OFF = new SeatHeaterVentInfo(0, 0, 0, 0);
    }

    public static ClimateRequestIce create(final @NonNull QuantityType<@NonNull Temperature> temperature,
            final boolean heat, final boolean defrost, final String username, final String vin) {
        return new ClimateRequestIce(1, AirTemperature.of(temperature), defrost, heat ? 1 : 0, 10,
                SeatHeaterVentInfo.OFF, 0, username, vin);
    }
}
