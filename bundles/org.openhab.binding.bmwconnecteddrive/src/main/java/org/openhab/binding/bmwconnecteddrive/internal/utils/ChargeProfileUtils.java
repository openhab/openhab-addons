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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChargeProfileUtils} class holds static utility methods dealing with
 * conversion of String-, LocalTime- and integer hours + minutes format of charge-profile
 * timers and time-windows.
 *
 * @author Norbert Truchsess - Initial contribution
 */
@NonNullByDefault
public class ChargeProfileUtils {

    public static final DateTimeFormatter TIMEFORMATER = DateTimeFormatter.ofPattern("HH:mm");

    public static String withMinute(String time, int minute) {
        return LocalTime.parse(time, TIMEFORMATER).withMinute(minute).format(TIMEFORMATER);
    }

    public static String withHour(String time, int hour) {
        return LocalTime.parse(time, TIMEFORMATER).withHour(hour).format(TIMEFORMATER);
    }

    public static LocalTime parseTime(String time) {
        return LocalTime.parse(time, TIMEFORMATER);
    }
}
