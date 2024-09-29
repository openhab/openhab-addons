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
package org.openhab.binding.bluetooth.blukii.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Blukii environment data.
 *
 * @author Markus Rathgeb - Initial contribution (migrated from handler)
 */
@NonNullByDefault
public class Environment {
    public final double pressure;
    public final int luminance;
    public final int humidity;
    public final double temperature;

    public Environment(final double pressure, final int luminance, final int humidity, final double temperature) {
        this.pressure = pressure;
        this.luminance = luminance;
        this.humidity = humidity;
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "Environment [pressure=" + pressure + ", luminance=" + luminance + ", humidity=" + humidity
                + ", temperature=" + temperature + "]";
    }
}
