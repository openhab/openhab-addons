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
package org.openhab.binding.bluelink.internal.dto.eu;

import org.openhab.binding.bluelink.internal.api.Vehicle;

import com.google.gson.annotations.SerializedName;

/**
 * Vehicle information from the vehicles API (Bluelink EU).
 * 
 * @author Florian Hotze - Initial contribution
 */
public record VehicleInfoEU(@SerializedName("vin") String vin, @SerializedName("vehicleId") String id,
        @SerializedName("vehicleName") String model, @SerializedName("type") String type,
        @SerializedName("nickname") String nickname,
        @SerializedName("ccuCCS2ProtocolSupport") int ccuCcs2ProtocolSupport) {
    public String getEngineType() {
        if (type == null || type.isBlank()) {
            return null;
        }
        return type.substring(0, type.length() - 1);
    }

    public Vehicle mapToVehicle() {
        return new Vehicle(id, nickname, model, vin, getEngineType(), "EV".equals(type), null, null,
                ccuCcs2ProtocolSupport == 1);
    }
}
