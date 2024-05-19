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
package org.openhab.binding.myuplink.internal.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests to verify behaviour of ChannelType implementation.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ChannelTypeTest {

    @Test
    public void testFromJsonDataNoUnit() {
        assertNull(ChannelType.fromJsonData("", false));
        assertNull(ChannelType.fromJsonData("", true));
    }

    @Test
    public void testFromJsonDataTemperature() {
        assertThat(ChannelType.fromJsonData("°C", false), is(ChannelType.TEMPERATURE));
    }

    @Test
    public void testFromJsonDataEnergy() {
        assertThat(ChannelType.fromJsonData("kWh", false), is(ChannelType.ENERGY));
    }

    @Test
    public void testFromJsonDataTime() {
        assertThat(ChannelType.fromJsonData("h", false), is(ChannelType.TIME));
    }

    @Test
    public void testFromRwTypeSwitch() {
        assertThat(ChannelType.fromTypeName("rwtype-switch"), is(ChannelType.RW_SWITCH));
    }

    @Test
    public void testFromTypeOnOff() {
        assertThat(ChannelType.fromTypeName("type-on-off"), is(ChannelType.ON_OFF));
    }
}
