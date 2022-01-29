/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GreeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeConfiguration {
    public String ipAddress = "";
    public String broadcastAddress = "";
    public int refresh = 60;
    /**
     * The currentTemperatureOffset is configureable in case the user wants to offset this temperature for calibration
     * of the temperature sensor.
     */
    public BigDecimal currentTemperatureOffset = new BigDecimal(0.0);

    @Override
    public String toString() {
        return "Config: ipAddress=" + ipAddress + ", broadcastAddress=" + broadcastAddress + ", refresh=" + refresh
                + ", currentTemperatureOffset=" + currentTemperatureOffset;
    }
}
