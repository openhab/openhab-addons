/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

import java.util.Calendar;

/**
 * The {@link ErrorLine} is responsible for holding information about a single error line.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class ErrorLine {
    private final byte error;
    private final String timestamp;

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

    public Calendar timestamp() {
        Calendar cal = Calendar.getInstance();

        int year = Integer.parseInt(timestamp.substring(0, 2)) + 1000;
        if (year < 1950) {
            year += 1000;
        }
        int month = Integer.parseInt(timestamp.substring(2, 4));
        int day = Integer.parseInt(timestamp.substring(4, 6));
        int hour = Integer.parseInt(timestamp.substring(7, 9));
        int min = Integer.parseInt(timestamp.substring(10, 12));
        int sec = Integer.parseInt(timestamp.substring(13, 15));
        cal.set(year, month - 1, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}
