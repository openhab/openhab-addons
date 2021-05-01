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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link ApplicationGroup}
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ApplicationGroupTest {

    @Test
    void test1LightsYellow() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 1);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.YELLOW);
        assertThat(group).isEqualTo(ApplicationGroup.LIGHTS);
    }

    @Test
    void test2BlindsGray() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 2);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.GREY);
        assertThat(group).isEqualTo(ApplicationGroup.BLINDS);
    }

    @Test
    void test3HeatingBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 3);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.HEATING);
    }

    @Test
    void test9CoolingBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 9);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.COOLING);
    }

    @Test
    void test10VentilationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 10);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.VENTILATION);
    }

    @Test
    void test11WindowBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 11);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.WINDOW);
    }

    @Test
    void test12RecirculationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 12);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.RECIRCULATION);
    }

    @Test
    void test64ApartmentVentilationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 64);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.APARTMENT_VENTILATION);
    }

    @Test
    void test48TemperatureControlBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 48);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLUE);
        assertThat(group).isEqualTo(ApplicationGroup.TEMPERATURE_CONTROL);
    }

    @Test
    void test4AudioCyan() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 4);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.CYAN);
        assertThat(group).isEqualTo(ApplicationGroup.AUDIO);
    }

    @Test
    void test5VideoMagenta() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 5);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.MAGENTA);
        assertThat(group).isEqualTo(ApplicationGroup.VIDEO);
    }

    @Test
    void test8JokerBlack() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 8);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.BLACK);
        assertThat(group).isEqualTo(ApplicationGroup.JOKER);
    }

    @Test
    void testNASingleDeviceWhite() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -1);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.WHITE);
        assertThat(group).isEqualTo(ApplicationGroup.SINGLE_DEVICE);
    }

    @Test
    void testNASecurityRed() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -2);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.RED);
        assertThat(group).isEqualTo(ApplicationGroup.SECURITY);
    }

    @Test
    void testNAAccessGreen() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -3);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.GREEN);
        assertThat(group).isEqualTo(ApplicationGroup.ACCESS);
    }

    @Test
    void testUndefinedGroup100() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 100);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.UNDEFINED);
        assertThat(group).isEqualTo(ApplicationGroup.UNDEFINED);
    }

    @Test
    void testUndefinedGroupNull() {
        ApplicationGroup group = ApplicationGroup.getGroup(null);
        assertThat(group.getColor()).isEqualTo(ApplicationGroup.Color.UNDEFINED);
        assertThat(group).isEqualTo(ApplicationGroup.UNDEFINED);
    }

    @Test
    void testGetShortId() {
        Short id = ApplicationGroup.BLINDS.getId();
        assertThat(ApplicationGroup.getGroup(id).getId()).isEqualTo(id);
    }
}
