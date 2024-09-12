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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
        assertThat(group.getColor(), is(ApplicationGroup.Color.YELLOW));
        assertThat(group, is(ApplicationGroup.LIGHTS));
    }

    @Test
    void test2BlindsGray() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 2);
        assertThat(group.getColor(), is(ApplicationGroup.Color.GREY));
        assertThat(group, is(ApplicationGroup.BLINDS));
    }

    @Test
    void test3HeatingBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 3);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.HEATING));
    }

    @Test
    void test9CoolingBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 9);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.COOLING));
    }

    @Test
    void test10VentilationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 10);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.VENTILATION));
    }

    @Test
    void test11WindowBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 11);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.WINDOW));
    }

    @Test
    void test12RecirculationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 12);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.RECIRCULATION));
    }

    @Test
    void test64ApartmentVentilationBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 64);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.APARTMENT_VENTILATION));
    }

    @Test
    void test48TemperatureControlBlue() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 48);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLUE));
        assertThat(group, is(ApplicationGroup.TEMPERATURE_CONTROL));
    }

    @Test
    void test4AudioCyan() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 4);
        assertThat(group.getColor(), is(ApplicationGroup.Color.CYAN));
        assertThat(group, is(ApplicationGroup.AUDIO));
    }

    @Test
    void test5VideoMagenta() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 5);
        assertThat(group.getColor(), is(ApplicationGroup.Color.MAGENTA));
        assertThat(group, is(ApplicationGroup.VIDEO));
    }

    @Test
    void test8JokerBlack() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 8);
        assertThat(group.getColor(), is(ApplicationGroup.Color.BLACK));
        assertThat(group, is(ApplicationGroup.JOKER));
    }

    @Test
    void testNASingleDeviceWhite() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -1);
        assertThat(group.getColor(), is(ApplicationGroup.Color.WHITE));
        assertThat(group, is(ApplicationGroup.SINGLE_DEVICE));
    }

    @Test
    void testNASecurityRed() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -2);
        assertThat(group.getColor(), is(ApplicationGroup.Color.RED));
        assertThat(group, is(ApplicationGroup.SECURITY));
    }

    @Test
    void testNAAccessGreen() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) -3);
        assertThat(group.getColor(), is(ApplicationGroup.Color.GREEN));
        assertThat(group, is(ApplicationGroup.ACCESS));
    }

    @Test
    void testUndefinedGroup100() {
        ApplicationGroup group = ApplicationGroup.getGroup((short) 100);
        assertThat(group.getColor(), is(ApplicationGroup.Color.UNDEFINED));
        assertThat(group, is(ApplicationGroup.UNDEFINED));
    }

    @Test
    void testUndefinedGroupNull() {
        ApplicationGroup group = ApplicationGroup.getGroup(null);
        assertThat(group.getColor(), is(ApplicationGroup.Color.UNDEFINED));
        assertThat(group, is(ApplicationGroup.UNDEFINED));
    }

    @Test
    void testGetShortId() {
        Short id = ApplicationGroup.BLINDS.getId();
        assertThat(ApplicationGroup.getGroup(id).getId(), is(id));
    }
}
