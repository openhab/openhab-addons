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

import java.util.List;

/**
 * EU vehicles list response.
 *
 * @author Florian Hotze - Initial contribution
 */
public record VehiclesResponse(List<VehicleInfo> vehicles) {

    public record VehicleInfo(String vin, String vehicleId, String vehicleName, String type, String nickname,
            int ccuCCS2ProtocolSupport) {
    }
}
