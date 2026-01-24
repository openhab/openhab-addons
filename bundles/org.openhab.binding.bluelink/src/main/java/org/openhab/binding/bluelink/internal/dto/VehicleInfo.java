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
 * Vehicle information from the enrollment details API.
 *
 * @author Marcus Better - Initial contribution
 */
public record VehicleInfo(@SerializedName("regid") String registrationId, String nickName, String vin, String evStatus,
        String modelCode, String vehicleGeneration, double odometer) {

    public boolean isElectric() {
        return "E".equals(evStatus);
    }

    public int getGeneration() {
        final String gen = vehicleGeneration;
        if (gen == null) {
            return 2;
        }
        try {
            return Integer.parseInt(gen);
        } catch (final NumberFormatException e) {
            return 2;
        }
    }

    public String getDisplayName() {
        final String nick = nickName;
        if (nick != null && !nick.isBlank()) {
            return nick;
        }
        return modelCode;
    }
}
