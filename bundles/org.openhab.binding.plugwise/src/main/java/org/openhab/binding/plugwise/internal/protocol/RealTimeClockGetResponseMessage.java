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
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.REAL_TIME_CLOCK_GET_RESPONSE;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Contains the real-time clock value of a Circle+. This message is the response of a
 * {@link RealTimeClockGetRequestMessage}. The Circle+ is the only device that holds a real-time clock value.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class RealTimeClockGetResponseMessage extends Message {

    private static final Pattern PAYLOAD_PATTERN = Pattern
            .compile("(\\w{16})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})(\\w{2})");

    private int seconds;
    private int minutes;
    private int hour;
    private int weekday;
    private int day;
    private int month;
    private int year;

    public RealTimeClockGetResponseMessage(int sequenceNumber, String payload) {
        super(REAL_TIME_CLOCK_GET_RESPONSE, sequenceNumber, payload);
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getMonth() {
        return month;
    }

    public int getSeconds() {
        return seconds;
    }

    public LocalDateTime getDateTime() {
        ZonedDateTime utcDateTime = ZonedDateTime.of(year, month, day, hour, minutes, seconds, 0, UTC);
        return utcDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public int getWeekday() {
        return weekday;
    }

    public int getYear() {
        return year;
    }

    @Override
    protected void parsePayload() {
        Matcher matcher = PAYLOAD_PATTERN.matcher(payload);
        if (matcher.matches()) {
            macAddress = new MACAddress(matcher.group(1));
            // Real-time clock values in the message are decimals and not hexadecimals
            seconds = Integer.parseInt(matcher.group(2));
            minutes = Integer.parseInt(matcher.group(3));
            hour = Integer.parseInt(matcher.group(4));
            weekday = Integer.parseInt(matcher.group(5));
            day = Integer.parseInt(matcher.group(6));
            month = Integer.parseInt(matcher.group(7));
            year = Integer.parseInt(matcher.group(8)) + 2000;
        } else {
            throw new PlugwisePayloadMismatchException(REAL_TIME_CLOCK_GET_RESPONSE, PAYLOAD_PATTERN, payload);
        }
    }
}
