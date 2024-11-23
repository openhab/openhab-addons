/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.climate;

/**
 * The {@link TemperatureControlSensorTransmitter} can be implement by subclasses to implement a
 * transmitter which can be used to push the target temperature or control value to a digitalSTROM zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface TemperatureControlSensorTransmitter {

    /**
     * Maximal temperature, which can be set as target temperature in digitalSTROM.
     */
    static float MAX_TEMP = 50f;

    /**
     * Minimal temperature, which can be set as target temperature in digitalSTROM.
     */
    static float MIN_TEMP = -43.15f;

    /**
     * Maximal control value, which can be set as target temperature in digitalSTROM.
     */
    static float MAX_CONTROLL_VALUE = 100f;

    /**
     * Minimal control value, which can be set as target temperature in digitalSTROM.
     */
    static float MIN_CONTROLL_VALUE = 0f;

    /**
     * Pushes a new target temperature to a digitalSTROM zone.
     *
     * @param zoneID (must not be null)
     * @param newValue (must not be null)
     * @return true, if the push was successfully
     */
    boolean pushTargetTemperature(Integer zoneID, Float newValue);

    /**
     * Pushes a new control value to a digitalSTROM zone.
     *
     * @param zoneID (must not be null)
     * @param newValue (must not be null)
     * @return true, if the push was successfully
     */
    boolean pushControlValue(Integer zoneID, Float newValue);
}
