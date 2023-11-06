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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.ThermostatMode;

/**
 * The {@link SensorConfig} is send by the the Rest API.
 * It is part of a {@link SensorMessage}.
 *
 * This should be in sync with the supported sensors from
 * https://dresden-elektronik.github.io/deconz-rest-doc/sensors/.
 *
 * @author David Graeff - Initial contribution
 * @author Lukas Agethen - Add Thermostat parameters
 */
@NonNullByDefault
public class SensorConfig {
    public boolean on = true;
    public boolean reachable = true;
    public @Nullable Integer battery;
    public @Nullable Float temperature;
    public @Nullable Integer heatsetpoint;
    public @Nullable ThermostatMode mode;
    public @Nullable Integer offset;
    public @Nullable Boolean locked;
    public @Nullable Boolean externalwindowopen;

    @Override
    public String toString() {
        return "SensorConfig{" + "on=" + on + ", reachable=" + reachable + ", battery=" + battery + ", temperature="
                + temperature + ", heatsetpoint=" + heatsetpoint + ", mode=" + mode + ", offset=" + offset + ", locked="
                + locked + ", externalwindowopen=" + externalwindowopen + "}";
    }
}
