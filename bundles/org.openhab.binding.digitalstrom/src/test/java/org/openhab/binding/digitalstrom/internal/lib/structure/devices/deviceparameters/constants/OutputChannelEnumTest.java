package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutputChannelEnumTest {
    @Test
    void test_1_Light_Brightness() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(1);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.BRIGHTNESS);
        assertThat(outputChannel.getName()).isEqualTo("brightness");
    }

    @Test
    void test_2_hue() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(2);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.HUE);
        assertThat(outputChannel.getName()).isEqualTo("hue");
    }

    @Test
    void test_3_saturation() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(3);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SATURATION);
        assertThat(outputChannel.getName()).isEqualTo("saturation");
    }

    @Test
    void test_4_colortemp() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(4);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.COLORTEMP);
        assertThat(outputChannel.getName()).isEqualTo("colortemp");
    }

    @Test
    void test_5_x() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(5);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.X);
        assertThat(outputChannel.getName()).isEqualTo("x");
    }

    @Test
    void test_6_y() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(6);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.Y);
        assertThat(outputChannel.getName()).isEqualTo("y");
    }

    @Test
    void test_7_shadePositionOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(7);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_POSITION_OUTSIDE);
        assertThat(outputChannel.getName()).isEqualTo("shadePositionOutside");
    }

    @Test
    void test_8_shadePositionIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(8);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_POSITION_INDOOR);
        assertThat(outputChannel.getName()).isEqualTo("shadePositionIndoor");
    }

    @Test
    void test_9_shadeOpeningAngleOutside() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(9);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE);
        assertThat(outputChannel.getName()).isEqualTo("shadeOpeningAngleOutside");
    }

    @Test
    void test_10_shadeOpeningAngleIndoor() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(10);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR);
        assertThat(outputChannel.getName()).isEqualTo("shadeOpeningAngleIndoor");
    }

    @Test
    void test_11_transparency() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(11);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.TRANSPARENCY);
        assertThat(outputChannel.getName()).isEqualTo("transparency");
    }

    @Test
    void test_12_airFlowIntensity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(12);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_INTENSITY);
        assertThat(outputChannel.getName()).isEqualTo("airFlowIntensity");
    }

    @Test
    void test_13_airFlowDirection() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(13);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_DIRECTION);
        assertThat(outputChannel.getName()).isEqualTo("airFlowDirection");
    }

    @Test
    void test_14_airFlapPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(14);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLAP_POSITION);
        assertThat(outputChannel.getName()).isEqualTo("airFlapPosition");
    }

    @Test
    void test_15_airLouverPosition() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(15);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_LOUVER_POSITION);
        assertThat(outputChannel.getName()).isEqualTo("airLouverPosition");
    }

    @Test
    void test_16_heatingPower() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(16);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.HEATING_POWER);
        assertThat(outputChannel.getName()).isEqualTo("heatingPower");
    }

    @Test
    void test_17_coolingCapacity() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(17);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.COOLING_CAPACITY);
        assertThat(outputChannel.getName()).isEqualTo("coolingCapacity");
    }

    @Test
    void test_18_audioVolume() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(18);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AUDIO_VOLUME);
        assertThat(outputChannel.getName()).isEqualTo("audioVolume");
    }

    @Test
    void test_19_powerState() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(19);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.POWER_STATE);
        assertThat(outputChannel.getName()).isEqualTo("powerState");
    }

    @Test
    void test_20_airLouverAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(20);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_LOUVER_AUTO);
        assertThat(outputChannel.getName()).isEqualTo("airLouverAuto");
    }

    @Test
    void test_21_airFlowAuto() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(21);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.AIR_FLOW_AUTO);
        assertThat(outputChannel.getName()).isEqualTo("airFlowAuto");
    }

    @Test
    void test_22_waterTemperature() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(22);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.WATER_TEMPERATURE);
        assertThat(outputChannel.getName()).isEqualTo("waterTemperature");
    }

    @Test
    void test_23_waterFlow() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(23);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.WATER_FLOW);
        assertThat(outputChannel.getName()).isEqualTo("waterFlow");
    }

    @Test
    void test_24_powerLevel() {
        OutputChannelEnum outputChannel = OutputChannelEnum.getChannel(24);
        assertThat(outputChannel).isEqualTo(OutputChannelEnum.POWER_LEVEL);
        assertThat(outputChannel.getName()).isEqualTo("powerLevel");
    }
}
