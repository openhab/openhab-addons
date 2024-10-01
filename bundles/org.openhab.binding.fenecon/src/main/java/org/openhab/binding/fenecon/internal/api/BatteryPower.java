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
package org.openhab.binding.fenecon.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BatteryPower} is a small helper class to convert the power value from battery.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public record BatteryPower(int chargerPower, int dischargerPower) {

    public static BatteryPower get(FeneconResponse response) {
        // Actual AC-side battery discharge power of Energy Storage System.
        // Negative values for charge; positive for discharge
        Integer powerValue = Integer.valueOf(response.value());
        int chargerPower = 0;
        int dischargerPower = 0;
        if (powerValue < 0) {
            chargerPower = powerValue * -1;
        } else {
            dischargerPower = powerValue;
        }

        return new BatteryPower(chargerPower, dischargerPower);
    }
}
