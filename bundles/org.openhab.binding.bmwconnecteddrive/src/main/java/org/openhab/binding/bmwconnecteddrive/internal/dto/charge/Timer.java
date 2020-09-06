/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

/**
 * The {@link Timer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Timer {
    public String departureTime;// ": "05:00",
    public boolean timerEnabled;// ": false,
    public List<String> weekdays;

    /**
     * "MONDAY",
     * "TUESDAY",
     * "WEDNESDAY",
     * "THURSDAY",
     * "FRIDAY"
     * ] '
     */
    public String getDays() {
        if (weekdays == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        }
        StringBuffer days = new StringBuffer();
        weekdays.forEach(entry -> {
            if (days.length() == 0) {
                days.append(Constants.DAYS.get(entry));
            } else {
                days.append(Constants.COMMA).append(Constants.DAYS.get(entry));
            }
        });
        return days.toString();
    }

}
