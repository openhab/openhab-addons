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
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.POWER_INFORMATION_RESPONSE;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.Energy;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains the real-time energy consumption of a relay device (Circle, Circle+, Stealth). This
 * message is the response of a {@link PowerInformationRequestMessage}.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class PowerInformationResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("(\\w{16})(\\w{4})(\\w{4})(\\w{8})(\\w{8})(\\w{4})");
    private static final double NANOSECONDS_CORRECTION_DIVISOR = 0.000046875; // 46875 divided by nanos per second
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    private Energy oneSecond;
    private Energy eightSecond;
    private Energy oneHourConsumed;
    private Energy oneHourProduced;
    private long nanosCorrection;

    public PowerInformationResponseMessage(int sequenceNumber, String payload) {
        super(POWER_INFORMATION_RESPONSE, sequenceNumber, payload);
    }

    public Energy getEightSecond() {
        return eightSecond;
    }

    public Energy getOneHourConsumed() {
        return oneHourConsumed;
    }

    public Energy getOneHourProduced() {
        return oneHourProduced;
    }

    public Energy getOneSecond() {
        return oneSecond;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            ZonedDateTime utcNow = ZonedDateTime.now(UTC);
            macAddress = new MACAddress(matcher.group(1));
            nanosCorrection = Math.round(Integer.parseInt(matcher.group(6), 16) / NANOSECONDS_CORRECTION_DIVISOR);
            oneSecond = new Energy(utcNow, (short) Integer.parseInt(matcher.group(2), 16),
                    Duration.ofSeconds(1, nanosCorrection));
            eightSecond = new Energy(utcNow, (short) Integer.parseInt(matcher.group(3), 16),
                    Duration.ofSeconds(8, nanosCorrection));
            oneHourConsumed = new Energy(utcNow, Long.parseLong(matcher.group(4), 16), ONE_HOUR);
            oneHourProduced = new Energy(utcNow, Long.parseLong(matcher.group(5), 16), ONE_HOUR);
        } else {
            throw new PlugwisePayloadMismatchException(POWER_INFORMATION_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }
}
