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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains information about the Hub we are communicating with.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Hub {

    private final SerialNumber serialNumber;

    private final String name;

    private int activeOverrideId;

    private final int defaultAwayOverrideLength;

    private final String softwareVersion;

    private final String hardwareVersion;

    private final String productionDate;

    public Hub(SerialNumber serialNumber, String name, int defaultAwayOverrideLength, int activeOverrideId,
            String softwareVersion, String hardwareVersion, String productionDate) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.defaultAwayOverrideLength = defaultAwayOverrideLength;
        this.activeOverrideId = activeOverrideId;
        this.softwareVersion = softwareVersion;
        this.hardwareVersion = hardwareVersion;
        this.productionDate = productionDate;
    }

    public static Hub fromH05(String h05) throws NoboDataException {
        String[] parts = h05.split(" ", 8);

        if (parts.length != 8) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on H5 call: %d", parts.length));
        }

        return new Hub(new SerialNumber(ModelHelper.toJavaString(parts[1])), ModelHelper.toJavaString(parts[2]),
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), ModelHelper.toJavaString(parts[5]),
                ModelHelper.toJavaString(parts[6]), ModelHelper.toJavaString(parts[7]));
    }

    public String generateCommandString(final String command) {
        return String.join(" ", command, serialNumber.toString(), ModelHelper.toHubString(name),
                Integer.toString(defaultAwayOverrideLength), Integer.toString(activeOverrideId),
                ModelHelper.toHubString(softwareVersion), ModelHelper.toHubString(hardwareVersion),
                ModelHelper.toHubString(productionDate));
    }

    public SerialNumber getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public Duration getDefaultAwayOverrideLength() {
        return Duration.ofMinutes(defaultAwayOverrideLength);
    }

    public int getActiveOverrideId() {
        return activeOverrideId;
    }

    public void setActiveOverrideId(int id) {
        activeOverrideId = id;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getProductionDate() {
        return productionDate;
    }
}
