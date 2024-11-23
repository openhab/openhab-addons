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
package org.openhab.binding.plugwise.internal.protocol;

import static java.time.ZoneOffset.UTC;
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_BUFFER_RESPONSE;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.Energy;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.PowerCalibration;

/**
 * Contains the historical pulse measurements at a certain log address from a device (Circle, Circle+, Stealth). This
 * message is the response of a {@link PowerBufferRequestMessage}. The consumed/produced {@link Energy} (kWh) of the
 * datapoints can be calculated using {@link PowerCalibration} data.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class PowerBufferResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern
            .compile("(\\w{16})(\\w{8})(\\w{8})(\\w{8})(\\w{8})(\\w{8})(\\w{8})(\\w{8})(\\w{8})(\\w{8})");
    private static final String EMPTY_TIMESTAMP = "FFFFFFFF";

    private Energy[] datapoints;
    private int logAddress;

    public PowerBufferResponseMessage(int sequenceNumber, String payload) {
        super(POWER_BUFFER_RESPONSE, sequenceNumber, payload);
    }

    public Energy[] getDatapoints() {
        return datapoints;
    }

    public int getLogAddress() {
        return logAddress;
    }

    public Energy getMostRecentDatapoint() {
        Energy result = null;
        for (Energy datapoint : datapoints) {
            if (datapoint != null) {
                result = datapoint;
            }
        }
        return result;
    }

    private Energy parseEnergy(String timeHex, String pulsesHex) {
        ZonedDateTime utcDateTime = !timeHex.equals(EMPTY_TIMESTAMP) ? parseDateTime(timeHex) : null;
        if (utcDateTime == null) {
            return null;
        }
        long pulses = Long.parseLong(pulsesHex, 16);
        return new Energy(utcDateTime, pulses);
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            datapoints = new Energy[4];
            datapoints[0] = parseEnergy(matcher.group(2), matcher.group(3));
            datapoints[1] = parseEnergy(matcher.group(4), matcher.group(5));
            datapoints[2] = parseEnergy(matcher.group(6), matcher.group(7));
            datapoints[3] = parseEnergy(matcher.group(8), matcher.group(9));
            logAddress = (Integer.parseInt(matcher.group(10), 16) - 278528) / 32;
        } else {
            throw new PlugwisePayloadMismatchException(POWER_BUFFER_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

    private ZonedDateTime parseDateTime(String timeHex) {
        int year = Integer.parseInt(timeHex.substring(0, 2), 16) + 2000;
        int month = Integer.parseInt(timeHex.substring(2, 4), 16);
        int minutes = Integer.parseInt(timeHex.substring(4, 8), 16);

        return ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, UTC).plusMinutes(minutes);
    }
}
