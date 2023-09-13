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
package org.openhab.binding.nobohub.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Nobø serial numbers are 12 digits where 3 and 3 digits form 2 bytes as decimal. In total 32 bits.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class Temperature {

    private final SerialNumber serialNumber;
    private final double temperature;

    public Temperature(SerialNumber serialNumber, double temperature) {
        this.serialNumber = serialNumber;
        this.temperature = temperature;
    }

    public static Temperature fromY02(String y02) throws NoboDataException {
        String[] parts = y02.split(" ", 3);
        if (parts.length != 3) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on Y02 call: %d", parts.length));
        }

        if (parts[2] == null) {
            throw new NoboDataException("Missing temperature data");
        }

        SerialNumber serialNumber = new SerialNumber(parts[1]);
        double temp = Double.NaN;

        if (!"N/A".equals(parts[2])) {
            try {
                temp = Double.parseDouble(parts[2]);
            } catch (NumberFormatException nfe) {
                throw new NoboDataException(
                        String.format("Failed to parse temperature %s: %s", parts[2], nfe.getMessage()), nfe);
            }
        }

        return new Temperature(serialNumber, temp);
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public double getTemperature() {
        return temperature;
    }
}
