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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ErrorLine} is responsible for holding information about a single error line.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class ErrorLine {
    private final byte error;
    private final String timestamp;

    public static final ErrorLine NO_ERROR = new ErrorLine((byte) 0, "");

    public ErrorLine(byte error, String timestamp) {
        this.error = error;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%d @ %s", error, timestamp);
    }

    public byte error() {
        return error;
    }

    public String timestampAsString() {
        return timestamp;
    }

    public ZonedDateTime timestamp() {
        int year = Integer.parseInt(timestamp.substring(0, 2)) + 1000;
        if (year < 1950) {
            year += 1000;
        }
        int month = Integer.parseInt(timestamp.substring(2, 4));
        int day = Integer.parseInt(timestamp.substring(4, 6));
        int hour = Integer.parseInt(timestamp.substring(7, 9));
        int min = Integer.parseInt(timestamp.substring(10, 12));
        int sec = Integer.parseInt(timestamp.substring(13, 15));

        return ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.systemDefault());
    }
}
