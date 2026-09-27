/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link TeslaChannelSelectorProxy}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class TeslaChannelSelectorProxyTest {

    @Test
    public void steeringWheelHeaterUpdatesItsChannel() {
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromRESTID("steering_wheel_heater");

        assertFalse(selector.isProperty());
        assertEquals("steeringwheelheater", selector.getChannelID());
        assertEquals(OnOffType.ON, state("steering_wheel_heater", "true"));
        assertEquals(OnOffType.OFF, state("steering_wheel_heater", "false"));
    }

    @ParameterizedTest
    @CsvSource({ "tpms_pressure_fl, tirepressurefrontleft", "tpms_pressure_fr, tirepressurefrontright",
            "tpms_pressure_rl, tirepressurerearleft", "tpms_pressure_rr, tirepressurerearright" })
    public void tirePressureIsReportedInBar(String restId, String channelId) {
        assertEquals(channelId, TeslaChannelSelector.getValueSelectorFromRESTID(restId).getChannelID());
        assertEquals(new QuantityType<>(3.175, Units.BAR), state(restId, "3.175"));
    }

    @ParameterizedTest
    @CsvSource({ "fd_window, driverfrontwindow", "rd_window, driverrearwindow", "fp_window, passengerfrontwindow",
            "rp_window, passengerrearwindow" })
    public void windowIsOpenUnlessReportedAsZero(String restId, String channelId) {
        assertEquals(channelId, TeslaChannelSelector.getValueSelectorFromRESTID(restId).getChannelID());
        assertEquals(OpenClosedType.CLOSED, state(restId, "0"));
        assertEquals(OpenClosedType.OPEN, state(restId, "1"));
        assertEquals(OpenClosedType.OPEN, state(restId, "2"));
        assertEquals(UnDefType.UNDEF, state(restId, "unknown"));
    }

    @Test
    public void tirePressureWarningHasASelector() {
        // needed when an item is linked, the handler looks up every linked channel
        assertEquals("tirepressurewarning",
                TeslaChannelSelector.getValueSelectorFromChannelID("tirepressurewarning").getChannelID());
    }

    @Test
    public void userPresentIsASwitch() {
        assertEquals("userpresent", TeslaChannelSelector.getValueSelectorFromRESTID("is_user_present").getChannelID());
        assertEquals(OnOffType.ON, state("is_user_present", "true"));
        assertEquals(OnOffType.OFF, state("is_user_present", "false"));
    }

    @Test
    public void minutesToFullChargeAreMinutes() {
        assertEquals("minutestofullcharge",
                TeslaChannelSelector.getValueSelectorFromRESTID("minutes_to_full_charge").getChannelID());
        assertEquals(new QuantityType<>(10, Units.MINUTE), state("minutes_to_full_charge", "10"));
    }

    private State state(String restId, String value) {
        return new TeslaChannelSelectorProxy().getState(value, TeslaChannelSelector.getValueSelectorFromRESTID(restId),
                new HashMap<>());
    }
}
