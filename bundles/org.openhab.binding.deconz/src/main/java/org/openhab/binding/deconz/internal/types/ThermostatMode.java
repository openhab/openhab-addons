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
package org.openhab.binding.deconz.internal.types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thermostat mode as reported by the REST API for usage in
 * {@link org.openhab.binding.deconz.internal.dto.SensorConfig}
 *
 * @author Lukas Agethen - Initial contribution
 */
@NonNullByDefault
public enum ThermostatMode {
    AUTO("auto"),
    HEAT("heat"),
    OFF("off"),
    UNKNOWN("");

    private static final Map<String, ThermostatMode> MAPPING = Arrays.stream(ThermostatMode.values())
            .collect(Collectors.toMap(v -> v.deconzValue, v -> v));
    private static final Logger LOGGER = LoggerFactory.getLogger(ThermostatMode.class);

    private final String deconzValue;

    ThermostatMode(String deconzValue) {
        this.deconzValue = deconzValue;
    }

    public String getDeconzValue() {
        return deconzValue;
    }

    public static ThermostatMode fromString(String s) {
        ThermostatMode thermostatMode = MAPPING.getOrDefault(s, UNKNOWN);
        if (thermostatMode == UNKNOWN) {
            LOGGER.debug("Unknown thermostat mode '{}' found. This should be reported.", s);
        }
        return thermostatMode;
    }
}
