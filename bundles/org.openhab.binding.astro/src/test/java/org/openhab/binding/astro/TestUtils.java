/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.astro;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/***
 * A set of test utilities methods.
 * 
 * @author Witold Markowski
 *
 */
public class TestUtils {

    /***
     * Constructs a <code>GregorianCalendar</code> with the given date and time set
     * for the provided time zone.
     * 
     * @param year
     *            the value used to set the <code>YEAR</code> calendar field in the
     *            calendar.
     * @param month
     *            the value used to set the <code>MONTH</code> calendar field in the
     *            calendar. Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth
     *            the value used to set the <code>DAY_OF_MONTH</code> calendar field
     *            in the calendar.
     * @param hourOfDay
     *            the value used to set the <code>HOUR_OF_DAY</code> calendar field
     *            in the calendar.
     * @param minute
     *            the value used to set the <code>MINUTE</code> calendar field in
     *            the calendar.
     * @param zone
     *            the given time zone.
     * @return
     */
    public static Calendar newCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, TimeZone zone) {
        Calendar result = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute);
        result.setTimeZone(zone);

        return result;
    }
}
