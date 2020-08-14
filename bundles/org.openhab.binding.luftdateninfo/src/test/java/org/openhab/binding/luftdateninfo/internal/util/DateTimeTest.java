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
package org.openhab.binding.luftdateninfo.internal.util;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.utils.DateTimeUtils;

/**
 * The {@link DateTimeTest} Test DateTimeFormatter provided in utils package
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DateTimeTest {

    @Test
    public void testJSonTime() {
        String jsonDateString = "2020-08-14 14:51:08";
        DateTime dt = DateTimeUtils.DTF.parseDateTime(jsonDateString);
        assertEquals("Day ", 14, dt.getDayOfMonth());
        assertEquals("Month ", 8, dt.getMonthOfYear());
        assertEquals("Year ", 2020, dt.getYear());

        String s = DateTimeUtils.DTF.print(dt);
        assertEquals("String ", jsonDateString, s);
    }
}
