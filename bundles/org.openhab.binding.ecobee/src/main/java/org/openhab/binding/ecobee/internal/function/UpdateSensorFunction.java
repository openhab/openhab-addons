/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The update sensor function allows the caller to update the name of an ecobee3 remote sensor.
 * Each ecobee3 remote sensor "enclosure" contains two distinct sensors types temperature
 * and occupancy. Only one of the sensors is required in the request. Both of the sensors'
 * names will be updated to ensure consistency as they are part of the same remote sensor
 * enclosure. This also reflects accurately what happens on the Thermostat itself. Note: This
 * function is restricted to the ecobee3 thermostat model only.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class UpdateSensorFunction extends AbstractFunction {

    public UpdateSensorFunction(@Nullable final String name, @Nullable final String deviceId,
            @Nullable final String sensorId) {
        super("updateSensor");

        if (name == null || deviceId == null || sensorId == null) {
            throw new IllegalArgumentException("name, deviceId and sensorId arguments are required.");
        }
        params.put("name", name);
        params.put("deviceId", deviceId);
        params.put("sensorId", sensorId);
    }
}
