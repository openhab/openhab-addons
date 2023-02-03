/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;
import org.openhab.core.library.types.PercentType;

/**
 * Tests cases Util
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class UtilTest {

    @Test
    public void coercingOfDmxValues() {
        // overrange
        int value = Util.toDmxValue(300);
        assertThat(value, is(DmxChannel.MAX_VALUE));

        // underrange
        value = Util.toDmxValue(-1);
        assertThat(value, is(DmxChannel.MIN_VALUE));

        // inrange
        value = Util.toDmxValue(100);
        assertThat(value, is(100));
    }

    @Test
    public void conversionString() {
        int value = Util.toDmxValue("100");
        assertThat(value, is(100));
    }

    @Test
    public void conversionFromPercentType() {
        // borders
        int value = Util.toDmxValue(new PercentType(100));
        assertThat(value, is(255));

        value = Util.toDmxValue(new PercentType(0));
        assertThat(value, is(0));

        // middle
        value = Util.toDmxValue(new PercentType(50));
        assertThat(value, is(127));
    }

    @Test
    public void conversionToPercentType() {
        // borders
        PercentType value = Util.toPercentValue(255);
        assertThat(value.intValue(), is(100));

        value = Util.toPercentValue(0);
        assertThat(value.intValue(), is(0));

        // middle
        value = Util.toPercentValue(127);
        assertThat(value.intValue(), is(49));
    }

    @Test
    public void fadeTimeFraction() {
        // target already reached
        int value = Util.fadeTimeFraction(123, 123, 1000);
        assertThat(value, is(0));

        // full fade
        value = Util.fadeTimeFraction(0, 255, 1000);
        assertThat(value, is(1000));

        // fraction
        value = Util.fadeTimeFraction(100, 155, 2550);
        assertThat(value, is(550));
    }
}
