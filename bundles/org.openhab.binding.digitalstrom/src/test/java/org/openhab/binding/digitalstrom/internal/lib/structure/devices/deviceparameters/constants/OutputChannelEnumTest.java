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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.BRIGHTNESS);
        assertThat(outputChannel.getName()).isEqualTo("brightness");
    }

    @Test
    void test2Hue() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(2);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.HUE);
        assertThat(outputChannel.getName()).isEqualTo("hue");
    }

    @Test
    void test3Saturation() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(3);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SATURATION);
        assertThat(outputChannel.getName()).isEqualTo("saturation");
    }

    @Test
    void test4Colortemp() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(4);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.COLORTEMP);
        assertThat(outputChannel.getName()).isEqualTo("colortemp");
    }

    @Test
    void test5X() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(5);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.X);
        assertThat(outputChannel.getName()).isEqualTo("x");
    }

    @Test
    void test6Y() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(6);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.Y);
        assertThat(outputChannel.getName()).isEqualTo("y");
    }

    @Test
    void test7ShadePositionOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(7);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_POSITION_OUTSIDE);
        assertThat(outputChannel.getName()).isEqualTo("shadePositionOutside");
    }

    @Test
    void test8ShadePositionIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(8);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_POSITION_INDOOR);
        assertThat(outputChannel.getName()).isEqualTo("shadePositionIndoor");
    }

    @Test
    void test9ShadeOpeningAngleOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(9);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE);
        assertThat(outputChannel.getName()).isEqualTo("shadeOpeningAngleOutside");
    }

    @Test
    void test10ShadeOpeningAngleIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(10);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR);
        assertThat(outputChannel.getName()).isEqualTo("shadeOpeningAngleIndoor");
    }

    @Test
    void test11Transparency() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(11);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.TRANSPARENCY);
        assertThat(outputChannel.getName()).isEqualTo("transparency");
    }

    @Test
    void test12AirFlowIntensity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(12);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_INTENSITY);
        assertThat(outputChannel.getName()).isEqualTo("airFlowIntensity");
    }

    @Test
    void test13AirFlowDirection() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(13);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_DIRECTION);
        assertThat(outputChannel.getName()).isEqualTo("airFlowDirection");
    }

    @Test
    void test14AirFlapPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(14);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLAP_POSITION);
        assertThat(outputChannel.getName()).isEqualTo("airFlapPosition");
    }

    @Test
    void test15AirLouverPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(15);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_LOUVER_POSITION);
        assertThat(outputChannel.getName()).isEqualTo("airLouverPosition");
    }

    @Test
    void test16HeatingPower() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(16);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.HEATING_POWER);
        assertThat(outputChannel.getName()).isEqualTo("heatingPower");
    }

    @Test
    void test17CoolingCapacity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(17);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.COOLING_CAPACITY);
        assertThat(outputChannel.getName()).isEqualTo("coolingCapacity");
    }

    @Test
    void test18AudioVolume() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(18);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AUDIO_VOLUME);
        assertThat(outputChannel.getName()).isEqualTo("audioVolume");
    }

    @Test
    void test19PowerState() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(19);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.POWER_STATE);
        assertThat(outputChannel.getName()).isEqualTo("powerState");
    }

    @Test
    void test20AirLouverAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(20);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_LOUVER_AUTO);
        assertThat(outputChannel.getName()).isEqualTo("airLouverAuto");
    }

    @Test
    void test21AirFlowAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(21);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_AUTO);
        assertThat(outputChannel.getName()).isEqualTo("airFlowAuto");
    }

    @Test
    void test22WaterTemperature() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(22);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.WATER_TEMPERATURE);
        assertThat(outputChannel.getName()).isEqualTo("waterTemperature");
    }

    @Test
    void test23WaterFlow() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(23);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.WATER_FLOW);
        assertThat(outputChannel.getName()).isEqualTo("waterFlow");
    }

    @Test
    void test24PowerLevel() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(24);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.POWER_LEVEL);
        assertThat(outputChannel.getName()).isEqualTo("powerLevel");
    }
}
