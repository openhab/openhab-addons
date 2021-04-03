package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ApplicationGroupTest {

    @Test
    void test_1_Lights_Yellow() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 1);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.YELLOW);
        assertThat(group).isEqualTo(ApplicationGroup.LIGHTS);
    }

    @Test
    void test_2_Blinds_Gray() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 2);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.GREY);
        assertThat(group).isEqualTo(ApplicationGroup.BLINDS);
    }

    @Test
    void test_3_Heating_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 3);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.HEATING);
    }

    @Test
    void test_9_Cooling_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 9);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.COOLING);
    }

    @Test
    void test_10_Ventilation_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 10);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.VENTILATION);
    }

    @Test
    void test_11_Window_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 11);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.WINDOW);
    }

    @Test
    void test_12_Recirculation_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 12);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.RECIRCULATION);
    }

    @Test
    void test_64_ApartmentVentilation_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 64);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.APARTMENT_VENTILATION);
    }

    @Test
    void test_48_TemperatureControl_Blue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 48);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.TEMPERATURE_CONTROL);
    }

    @Test
    void test_4_Audio_Cyan() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 4);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.CYAN);
        assertThat(group).isEqualTo(ApplicationGroup.AUDIO);
    }

    @Test
    void test_5_Video_Magenta() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 5);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.MAGENTA);
        assertThat(group).isEqualTo(ApplicationGroup.VIDEO);
    }

    @Test
    void test_8_Joker_Black() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 8);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLACK);
        assertThat(group).isEqualTo(ApplicationGroup.JOKER);
    }

    @Test
    void test_na_SingleDevice_White() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -1);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.WHITE);
        assertThat(group).isEqualTo(ApplicationGroup.SINGLE_DEVICE);
    }

    @Test
    void test_na_Security_Red() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -2);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.RED);
        assertThat(group).isEqualTo(ApplicationGroup.SECURITY);
    }

    @Test
    void test_na_Access_Green() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -3);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.GREEN);
        assertThat(group).isEqualTo(ApplicationGroup.ACCESS);
    }

    @Test
    void test_undefinedGroup100() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 100);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.UNDEFINED);
        assertThat(group).isEqualTo(ApplicationGroup.UNDEFINED);
    }

    @Test
    void test_undefinedGroupNull() {
        ApplicationGroup group = ApplicationGroup.getGroup(null);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.UNDEFINED);
        assertThat(group).isEqualTo(ApplicationGroup.UNDEFINED);
    }

    @Test
    void test_getShortId() {
        Short id = ApplicationGroup.BLINDS.getId();
        assertThat(ApplicationGroup.getGroup(id).getId()).isEqualTo(id);
    }
}
