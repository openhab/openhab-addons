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

    private @Nullable Integer voltage;
    private @Nullable Integer current;
    private @Nullable Integer temperature;
    private @Nullable BatteryCapacity capacity;

    public @Nullable Integer getVoltage() {
        return voltage;
    }

    public @Nullable Integer getCurrent() {
        return current;
    }

    public @Nullable Integer getTemperature() {
        return temperature;
    }

    public @Nullable BatteryCapacity getCapacity() {
        return capacity;
    }
}
