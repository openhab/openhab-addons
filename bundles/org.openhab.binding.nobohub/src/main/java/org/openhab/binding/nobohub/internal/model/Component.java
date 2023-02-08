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

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A Component in the Nobø Hub can be an oven, a floor or a switch.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class Component {

    private final SerialNumber serialNumber;
    private final String name;
    private final boolean reverse;
    private final int zoneId;
    private final int temperatureSensorForZoneId;
    private double temperature;

    public Component(SerialNumber serialNumber, String name, boolean reverse, int zoneId,
            int temperatureSensorForZoneId) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.reverse = reverse;
        this.zoneId = zoneId;
        this.temperatureSensorForZoneId = temperatureSensorForZoneId;
    }

    public static Component fromH02(String h02) throws NoboDataException {
        String[] parts = h02.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on H2 call: %d", parts.length));
        }

        SerialNumber serial = new SerialNumber(ModelHelper.toJavaString(parts[1]));
        if (!serial.isWellFormed()) {
            throw new NoboDataException(String.format("Illegal serial number: '%s'", serial));
        }

        return new Component(serial, ModelHelper.toJavaString(parts[3]), "1".equals(parts[4]),
                Integer.parseInt(parts[5]), Integer.parseInt(parts[7]));
    }

    public String generateCommandString(final String command) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(command).add(ModelHelper.toHubString(serialNumber.toString()));

        // Status not yet implemented in hub
        joiner.add("0");

        joiner.add(ModelHelper.toHubString(name)).add(reverse ? "1" : "0").add(Integer.toString(zoneId)).add("-1");

        // Active Override ID not implemented in hub for components yet
        joiner.add(Integer.toString(temperatureSensorForZoneId));
        return joiner.toString();
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public boolean inReverse() {
        return reverse;
    }

    public int getZoneId() {
        return zoneId;
    }

    public int getTemperatureSensorForZoneId() {
        return temperatureSensorForZoneId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
