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
package org.openhab.binding.sensorcommunity.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sensorcommunity.internal.utils.DateTimeUtils;

/**
 * The {@link DateTimeTest} Test DateTimeFormatter provided in utils package
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DateTimeTest {

    @Test
    public void testJSonTime() {
        String jsonDateString = "2020-08-14 14:53:21";
        try {
            LocalDateTime dt = LocalDateTime.from(DateTimeUtils.DTF.parse(jsonDateString));
            assertEquals(14, dt.getDayOfMonth(), "Day");
            assertEquals(8, dt.getMonthValue(), "Month");
            assertEquals(2020, dt.getYear(), "Year");

            String s = dt.format(DateTimeUtils.DTF);
            assertEquals(jsonDateString, s, "String");
        } catch (DateTimeParseException e) {
            assertFalse(true);
        }
    }
}
