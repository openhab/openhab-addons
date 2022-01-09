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
package org.openhab.binding.mikrotik.internal.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class ConverterTest {

    @Test
    public void testFromRouterosTime() {
        assertThat(Converter.fromRouterosTime("dec/11/2020 20:45:40"),
                is(equalTo(LocalDateTime.of(2020, 12, 11, 20, 45, 40, 0))));
        assertThat(Converter.fromRouterosTime("jan/07/2021 09:14:11"),
                is(equalTo(LocalDateTime.of(2021, 1, 7, 9, 14, 11, 0))));
        assertThat(Converter.fromRouterosTime("feb/13/2021 23:59:59"),
                is(equalTo(LocalDateTime.of(2021, 2, 13, 23, 59, 59, 0))));
    }

    @Test
    public void testFromRouterosPeriod() {
        LocalDateTime fromDateTime = LocalDateTime.of(2021, 2, 1, 0, 0, 0, 0);

        assertThat(Converter.routerosPeriodBack("1y3w4d5h6m7s11ms", fromDateTime),
                is(equalTo(LocalDateTime.parse("2020-01-06T18:53:53.011"))));

        assertNull(Converter.routerosPeriodBack(null));

        /*
         * uptime = 6w6h31m31s
         * uptime = 3d7h6m43s710ms
         * uptime = 16h39m58s220ms
         * uptime = 1h38m53s110ms
         * uptime = 53m53s950ms
         */

        assertThat(Converter.routerosPeriodBack("6w6h31m31s", fromDateTime),
                is(equalTo(LocalDateTime.parse("2020-12-20T17:28:29"))));

        assertThat(Converter.routerosPeriodBack("3d7h6m43s710ms", fromDateTime),
                is(equalTo(LocalDateTime.parse("2021-01-28T16:53:17.710"))));

        assertThat(Converter.routerosPeriodBack("16h39m58s220ms", fromDateTime),
                is(equalTo(LocalDateTime.parse("2021-01-31T07:20:02.220"))));

        assertThat(Converter.routerosPeriodBack("1h38m53s110ms", fromDateTime),
                is(equalTo(LocalDateTime.parse("2021-01-31T22:21:07.110"))));

        assertThat(Converter.routerosPeriodBack("53m53s950ms", fromDateTime),
                is(equalTo(LocalDateTime.parse("2021-01-31T23:06:07.950"))));
    }
}
