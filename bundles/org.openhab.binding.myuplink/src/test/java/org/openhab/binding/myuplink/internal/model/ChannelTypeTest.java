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
    public void testFromJsonDataInteger() {
        assertThat(ChannelType.fromJsonData("", "55"), is(ChannelType.INTEGER));
        assertThat(ChannelType.fromJsonData("", "Off"), is(ChannelType.INTEGER));
    }

    @Test
    public void testFromJsonDataDouble() {
        assertThat(ChannelType.fromJsonData("", "5.5"), is(ChannelType.DOUBLE));
        assertThat(ChannelType.fromJsonData("", "160.3"), is(ChannelType.DOUBLE));
    }

    @Test
    public void testFromJsonDataTemperature() {
        assertThat(ChannelType.fromJsonData("°C", "xxx"), is(ChannelType.TEMPERATURE));
    }

    @Test
    public void testFromJsonDataEnergy() {
        assertThat(ChannelType.fromJsonData("kWh", "xxx"), is(ChannelType.ENERGY));
    }

    @Test
    public void testFromJsonDataTime() {
        assertThat(ChannelType.fromJsonData("h", "xxx"), is(ChannelType.TIME));
    }
}
