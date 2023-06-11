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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OutputChannelEnum}
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@NonNullByDefault
class OutputChannelEnumTest {
    @Test
    void test1LightBrightness() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(1);
        assertThat(outputChannel, is(OutputChannelEnum.BRIGHTNESS));
        assertThat(outputChannel.getName(), is("brightness"));
    }

    @Test
    void test2Hue() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(2);
        assertThat(outputChannel, is(OutputChannelEnum.HUE));
        assertThat(outputChannel.getName(), is("hue"));
    }

    @Test
    void test3Saturation() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(3);
        assertThat(outputChannel, is(OutputChannelEnum.SATURATION));
        assertThat(outputChannel.getName(), is("saturation"));
    }

    @Test
    void test4Colortemp() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(4);
        assertThat(outputChannel, is(OutputChannelEnum.COLORTEMP));
        assertThat(outputChannel.getName(), is("colortemp"));
    }

    @Test
    void test5X() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(5);
        assertThat(outputChannel, is(OutputChannelEnum.X));
        assertThat(outputChannel.getName(), is("x"));
    }

    @Test
    void test6Y() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(6);
        assertThat(outputChannel, is(OutputChannelEnum.Y));
        assertThat(outputChannel.getName(), is("y"));
    }

    @Test
    void test7ShadePositionOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(7);
        assertThat(outputChannel, is(OutputChannelEnum.SHADE_POSITION_OUTSIDE));
        assertThat(outputChannel.getName(), is("shadePositionOutside"));
    }

    @Test
    void test8ShadePositionIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(8);
        assertThat(outputChannel, is(OutputChannelEnum.SHADE_POSITION_INDOOR));
        assertThat(outputChannel.getName(), is("shadePositionIndoor"));
    }

    @Test
    void test9ShadeOpeningAngleOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(9);
        assertThat(outputChannel, is(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE));
        assertThat(outputChannel.getName(), is("shadeOpeningAngleOutside"));
    }

    @Test
    void test10ShadeOpeningAngleIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(10);
        assertThat(outputChannel, is(OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR));
        assertThat(outputChannel.getName(), is("shadeOpeningAngleIndoor"));
    }

    @Test
    void test11Transparency() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(11);
        assertThat(outputChannel, is(OutputChannelEnum.TRANSPARENCY));
        assertThat(outputChannel.getName(), is("transparency"));
    }

    @Test
    void test12AirFlowIntensity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(12);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_FLOW_INTENSITY));
        assertThat(outputChannel.getName(), is("airFlowIntensity"));
    }

    @Test
    void test13AirFlowDirection() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(13);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_FLOW_DIRECTION));
        assertThat(outputChannel.getName(), is("airFlowDirection"));
    }

    @Test
    void test14AirFlapPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(14);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_FLAP_POSITION));
        assertThat(outputChannel.getName(), is("airFlapPosition"));
    }

    @Test
    void test15AirLouverPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(15);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_LOUVER_POSITION));
        assertThat(outputChannel.getName(), is("airLouverPosition"));
    }

    @Test
    void test16HeatingPower() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(16);
        assertThat(outputChannel, is(OutputChannelEnum.HEATING_POWER));
        assertThat(outputChannel.getName(), is("heatingPower"));
    }

    @Test
    void test17CoolingCapacity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(17);
        assertThat(outputChannel, is(OutputChannelEnum.COOLING_CAPACITY));
        assertThat(outputChannel.getName(), is("coolingCapacity"));
    }

    @Test
    void test18AudioVolume() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(18);
        assertThat(outputChannel, is(OutputChannelEnum.AUDIO_VOLUME));
        assertThat(outputChannel.getName(), is("audioVolume"));
    }

    @Test
    void test19PowerState() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(19);
        assertThat(outputChannel, is(OutputChannelEnum.POWER_STATE));
        assertThat(outputChannel.getName(), is("powerState"));
    }

    @Test
    void test20AirLouverAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(20);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_LOUVER_AUTO));
        assertThat(outputChannel.getName(), is("airLouverAuto"));
    }

    @Test
    void test21AirFlowAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(21);
        assertThat(outputChannel, is(OutputChannelEnum.AIR_FLOW_AUTO));
        assertThat(outputChannel.getName(), is("airFlowAuto"));
    }

    @Test
    void test22WaterTemperature() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(22);
        assertThat(outputChannel, is(OutputChannelEnum.WATER_TEMPERATURE));
        assertThat(outputChannel.getName(), is("waterTemperature"));
    }

    @Test
    void test23WaterFlow() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(23);
        assertThat(outputChannel, is(OutputChannelEnum.WATER_FLOW));
        assertThat(outputChannel.getName(), is("waterFlow"));
    }

    @Test
    void test24PowerLevel() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(24);
        assertThat(outputChannel, is(OutputChannelEnum.POWER_LEVEL));
        assertThat(outputChannel.getName(), is("powerLevel"));
    }
}
