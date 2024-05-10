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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ChannelTypeTest {

    @Test
    public void testFromJsonDataInteger() {
        assertEquals(ChannelType.INTEGER, ChannelType.fromJsonData("", "55"));
        assertEquals(ChannelType.INTEGER, ChannelType.fromJsonData("", "Off"));
    }

    @Test
    public void testFromJsonDataDouble() {
        assertEquals(ChannelType.DOUBLE, ChannelType.fromJsonData("", "5.5"));
        assertEquals(ChannelType.DOUBLE, ChannelType.fromJsonData("", "160.3"));
    }

    @Test
    public void testFromJsonDataTemperature() {
        assertEquals(ChannelType.TEMPERATURE, ChannelType.fromJsonData("°C", "xxx"));
    }

    @Test
    public void testFromJsonDataEnergy() {
        assertEquals(ChannelType.ENERGY, ChannelType.fromJsonData("kWh", "xxx"));
    }

    @Test
    public void testFromJsonDataTime() {
        assertEquals(ChannelType.TIME, ChannelType.fromJsonData("h", "xxx"));
    }
}
