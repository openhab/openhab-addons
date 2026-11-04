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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Battery measurements returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Battery {

    private @Nullable Integer id;
    private @Nullable Integer charge;
    private @Nullable Integer voltage;
    private @Nullable Integer current;
    private @Nullable Integer temperature;
    private @Nullable BatteryCapacity capacity;

    /**
     * Returns the battery identifier.
     *
     * @return the battery identifier, or {@code null} if absent
     */
    public @Nullable Integer getId() {
        return id;
    }

    /**
     * Returns the battery charge level as a percentage.
     *
     * @return the battery charge level as a percentage, or {@code null} if absent
     */
    public @Nullable Integer getCharge() {
        return charge;
    }

    /**
     * Returns the battery voltage in millivolts.
     *
     * @return the battery voltage in millivolts, or {@code null} if absent
     */
    public @Nullable Integer getVoltage() {
        return voltage;
    }

    /**
     * Returns the battery current in milliamperes.
     *
     * @return the battery current in milliamperes, or {@code null} if absent
     */
    public @Nullable Integer getCurrent() {
        return current;
    }

    /**
     * Returns the battery temperature in tenths of a degree Celsius.
     *
     * @return the battery temperature in tenths of a degree Celsius, or {@code null} if absent
     */
    public @Nullable Integer getTemperature() {
        return temperature;
    }

    /**
     * Returns the battery capacity information.
     *
     * @return the battery capacity information, or {@code null} if absent
     */
    public @Nullable BatteryCapacity getCapacity() {
        return capacity;
    }
}
