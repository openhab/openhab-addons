/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SbusContactConfig} class extends device configuration with contact sensor specific parameters.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusContactConfig extends SbusDeviceConfig {
    /**
     * Sensor type for contact sensor.
     * String value is used for configuration binding, converted to enum via ContactSensorType.fromConfigValue()
     */
    public String type = ContactSensorType.SENSOR_24Z.getConfigValue(); // Default to traditional behavior

    /**
     * Get the sensor type as an enum.
     *
     * @return the ContactSensorType enum value
     */
    public ContactSensorType getSensorType() {
        return ContactSensorType.fromConfigValue(type);
    }
}
