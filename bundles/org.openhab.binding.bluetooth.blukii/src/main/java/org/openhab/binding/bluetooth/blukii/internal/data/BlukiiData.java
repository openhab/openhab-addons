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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Blukii data.
 *
 * @author Markus Rathgeb - Initial contribution (migrate from handler)
 */
@NonNullByDefault
public class BlukiiData {

    public final int battery;
    public final Optional<Magnetometer> magnetometer;
    public final Optional<Environment> environment;
    public final Optional<Accelerometer> accelerometer;

    public BlukiiData(final int battery, final Optional<Magnetometer> magnetometer,
            final Optional<Environment> environment, final Optional<Accelerometer> accelerometer) {
        this.battery = battery;
        this.magnetometer = magnetometer;
        this.environment = environment;
        this.accelerometer = accelerometer;
    }

    @Override
    public String toString() {
        return "BlukiiData [battery=" + battery + ", magnetometer=" + magnetometer + ", environment=" + environment
                + ", accelerometer=" + accelerometer + "]";
    }
}
