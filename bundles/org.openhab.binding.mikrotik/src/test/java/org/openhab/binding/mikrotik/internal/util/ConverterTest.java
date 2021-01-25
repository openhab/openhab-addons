/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Oleg Vivtash - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class ConverterTest {

    @Test
    public void testFromRouterosTime() {
        assertThat(Converter.fromRouterosTime("dec/11/2020 20:45:40"), is(equalTo(new DateTime(2020, 12, 11, 20, 45, 40))));
        assertThat(Converter.fromRouterosTime("jan/07/2021 09:14:11"), is(equalTo(new DateTime(2021, 1, 7, 9, 14, 11))));
        assertThat(Converter.fromRouterosTime("feb/13/2021 23:59:59"), is(equalTo(new DateTime(2021, 2, 13, 23, 59, 59))));
    }

    @Test
    public void testFromRouterosPeriod() {
        assertThat(Converter.fromRouterosPeriod("1y3w4d5h6m7s11ms"), is(equalTo(new Period(1,0,3,4,5,6,7,11))));
        assertNull(Converter.fromRouterosPeriod(null));

        /*
         * uptime = 6w6h31m31s
         * uptime = 3d7h6m43s710ms
         * uptime = 16h39m58s220ms
         * uptime = 1h38m53s110ms
         * uptime = 53m53s950ms
         */

        assertThat(Converter.fromRouterosPeriod("6w6h31m31s"), is(equalTo(new Period(0,0,6,0,6,31,31,0))));
        assertThat(Converter.fromRouterosPeriod("3d7h6m43s710ms"), is(equalTo(new Period(0,0,0,3,7,6,43,710))));
        assertThat(Converter.fromRouterosPeriod("16h39m58s220ms"), is(equalTo(new Period(0,0,0,0,16,39,58,220))));
        assertThat(Converter.fromRouterosPeriod("1h38m53s110ms"), is(equalTo(new Period(0,0,0,0,1,38,53,110))));
        assertThat(Converter.fromRouterosPeriod("53m53s950ms"), is(equalTo(new Period(0,0,0,0,0,53,53,950))));


    }

}
