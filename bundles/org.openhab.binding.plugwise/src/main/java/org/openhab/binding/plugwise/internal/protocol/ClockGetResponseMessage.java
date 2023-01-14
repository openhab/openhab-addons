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
package org.openhab.binding.plugwise.internal.protocol;

import static java.time.ZoneOffset.UTC;
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.CLOCK_GET_RESPONSE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains the current clock value of a device. This message is the response of a {@link ClockGetRequestMessage}. Not
 * all response fields have been reverse engineered.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class ClockGetResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern
            .compile("(\\w{16})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})");

    private int hour;
    private int minutes;
    private int seconds;
    private int weekday;

    public ClockGetResponseMessage(int sequenceNumber, String payload) {
        super(CLOCK_GET_RESPONSE, sequenceNumber, payload);
    }

    public int getHour() {
        return hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getWeekday() {
        return weekday;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            hour = Integer.parseInt(matcher.group(2), 16);
            minutes = Integer.parseInt(matcher.group(3), 16);
            seconds = Integer.parseInt(matcher.group(4), 16);
            weekday = Integer.parseInt(matcher.group(5), 16);
        } else {
            throw new PlugwisePayloadMismatchException(CLOCK_GET_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }

    public String getTime() {
        ZonedDateTime utcDateTime = ZonedDateTime.now(UTC).withHour(hour).withMinute(minutes).withSecond(seconds)
                .withNano(0);
        ZonedDateTime localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return DateTimeFormatter.ISO_LOCAL_TIME.format(localDateTime);
    }
}
