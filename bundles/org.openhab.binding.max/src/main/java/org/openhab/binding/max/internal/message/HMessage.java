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
package org.openhab.binding.max.internal.message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;

/**
 * The H message contains information about the MAX! Cube.
 *
 * @author Andreas Heil - Initial contribution
 * @author Marcel Verpaalen - Details parsing, OH2 version
 */
@NonNullByDefault
public final class HMessage extends Message {

    private ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault());
    public Map<String, Object> properties = new HashMap<>();

    private String rawSerialNumber;
    private String rawRFAddress;
    private String rawFirmwareVersion;
    private String rawConnectionId;
    private String rawDutyCycle;
    private String rawFreeMemorySlots;
    private String rawCubeTimeState;
    private String rawNTPCounter;

    // yet unknown fields
    private String rawUnknownfield4;

    public HMessage(String raw) {
        super(raw);

        String[] tokens = this.getPayload().split(Message.DELIMETER);

        if (tokens.length < 11) {
            throw new ArrayIndexOutOfBoundsException("MAX!Cube raw H Message corrupt");
        }

        rawSerialNumber = tokens[0];
        rawRFAddress = tokens[1];
        rawFirmwareVersion = tokens[2].substring(0, 2) + "." + tokens[2].substring(2, 4);
        rawUnknownfield4 = tokens[3];
        rawConnectionId = tokens[4];
        rawDutyCycle = Integer.toString(Utils.fromHex(tokens[5]));
        rawFreeMemorySlots = Integer.toString(Utils.fromHex(tokens[6]));

        setDateTime(tokens[7], tokens[8]);

        rawCubeTimeState = tokens[9];
        rawNTPCounter = Integer.toString(Utils.fromHex(tokens[10]));
        properties.put("Serial number", rawSerialNumber);
        properties.put("RF address (HEX)", rawRFAddress);
        properties.put("Firmware version", rawFirmwareVersion);
        properties.put("Connection ID", rawConnectionId);
        properties.put("Unknown", rawUnknownfield4);
        properties.put("Duty Cycle", rawDutyCycle);
        properties.put("FreeMemorySlots", rawFreeMemorySlots);
        properties.put("CubeTimeState", rawCubeTimeState);
        properties.put("NTPCounter", rawNTPCounter);
    }

    public String getSerialNumber() {
        return rawSerialNumber;
    }

    public String getRFAddress() {
        return rawRFAddress;
    }

    public String getFirmwareVersion() {
        return rawFirmwareVersion;
    }

    public String getConnectionId() {
        return rawConnectionId;
    }

    public int getDutyCycle() {
        return Integer.parseInt(rawDutyCycle);
    }

    public int getFreeMemorySlots() {
        return Integer.parseInt(rawFreeMemorySlots);
    }

    public String getCubeTimeState() {
        return rawCubeTimeState;
    }

    public String getNTPCounter() {
        return rawNTPCounter;
    }

    private final void setDateTime(String hexDate, String hexTime) {
        // we have to add 2000, otherwise we get a wrong timestamp
        int year = 2000 + Utils.fromHex(hexDate.substring(0, 2));
        int month = Utils.fromHex(hexDate.substring(2, 4));
        int dayOfMonth = Utils.fromHex(hexDate.substring(4, 6));

        int hours = Utils.fromHex(hexTime.substring(0, 2));
        int minutes = Utils.fromHex(hexTime.substring(2, 4));

        zonedDateTime = ZonedDateTime.of(year, month, dayOfMonth, hours, minutes, 0, 0, ZoneId.systemDefault());
    }

    public Date getDateTime() {
        return Date.from(zonedDateTime.toInstant());
    }

    @Override
    public void debug(Logger logger) {
        logger.debug("=== H Message === ");
        logger.trace("\tRAW:            : {}", getPayload());
        logger.trace("\tReading Time    : {}", getDateTime());
        for (String key : properties.keySet()) {
            logger.debug("\t{}: {}", key, properties.get(key));
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.H;
    }
}
