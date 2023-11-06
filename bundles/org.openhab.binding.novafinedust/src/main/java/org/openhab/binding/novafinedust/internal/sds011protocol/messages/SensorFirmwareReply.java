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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data from the sensor containing information about the installed firmware
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SensorFirmwareReply extends SensorReply {

    private final byte year;
    private final byte month;
    private final byte day;

    public SensorFirmwareReply(byte[] receivedData) {
        super(receivedData);
        this.year = receivedData[3];
        this.month = receivedData[4];
        this.day = receivedData[5];
    }

    /**
     * Gets the firmware of the sensor as a String
     *
     * @return firmware of the sensor formatted as YY-MM-DD
     */
    public String getFirmware() {
        return year + "-" + month + "-" + day;
    }

    @Override
    public String toString() {
        return "FirmwareReply: [firmware=" + getFirmware() + "]";
    }
}
