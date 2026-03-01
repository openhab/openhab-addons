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
package org.openhab.binding.bluelink.internal.dto.ca;

import java.util.List;

/**
 * Response from the `/vhcllst` endpoint.
 *
 * @author Marcus Better - Initial contribution
 */
public record ListVehiclesResponse(Result result) {

    public record Result(List<VehicleInfo> vehicles) {

        public record VehicleInfo(String fuelKindCode, String vehicleId, String nickName, String modelName,
                String modelYear, String vin, String timezone) {
        }
    }
}
