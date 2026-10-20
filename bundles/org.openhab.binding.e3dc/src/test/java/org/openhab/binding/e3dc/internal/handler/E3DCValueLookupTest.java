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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCInverterChannels;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Tests where a channel finds its value in a poll response.
 *
 * <p>
 * The device wraps values in containers of their own more often than the channel model suggests: the
 * inverter reports every value as a container carrying {@code PVI_VALUE} next to the inverter index, and
 * reports it once per tracker. These tests pin those shapes, because a lookup that names the wrong level
 * leaves a channel silently empty.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCValueLookupTest {

    /** A response with the shape the device answers the inverter request with. */
    private static RscpValue inverterResponse(int trackers) {
        List<RscpValue> data = new ArrayList<>();
        data.add(RscpValue.uchar8(RscpTag.PVI_INDEX, 0));
        data.add(wrapped(RscpTag.PVI_AC_VOLTAGE, 0, 237.2));
        data.add(RscpValue.of(RscpTag.PVI_ON_GRID, RscpType.BOOL, false));
        for (int tracker = 0; tracker < trackers; tracker++) {
            data.add(wrapped(RscpTag.PVI_DC_CURRENT, tracker, tracker + 0.5));
        }
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.PVI_DATA, data)));
    }

    /** One inverter value, which arrives in a container of its own together with the index of its request. */
    private static RscpValue wrapped(int tag, int index, double value) {
        return RscpValue.container(tag, List.of(RscpValue.uchar8(RscpTag.PVI_INDEX, index),
                RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32, (float) value)));
    }

    /** The history response of one day, whose sum container holds the values. */
    private static RscpValue historyResponse() {
        return RscpValue.container(0,
                List.of(RscpValue.container(RscpTag.DB_HISTORY_DATA_DAY,
                        List.of(RscpValue.container(RscpTag.DB_SUM_CONTAINER,
                                List.of(RscpValue.of(RscpTag.DB_AUTARKY, RscpType.FLOAT32, 42.0f)))))));
    }

    private static E3DCChannelSpec spec(String group, String channel) {
        E3DCChannelSpec spec = E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL.equals(group)
                ? E3DCChannelSpec.wallbox().spec(group, channel)
                : E3DCBindingConstants.GROUP_MODULE.equals(group) ? E3DCChannelSpec.batteryModule().spec(group, channel)
                        : E3DCChannelSpec.powerPlant().spec(group, channel);
        assertNotNull(spec, "the channel " + group + "#" + channel + " must exist");
        return spec;
    }

    private static double numberOf(RscpValue value) {
        Number number = value.asNumber();
        assertNotNull(number, "the value must be a number");
        return number.doubleValue();
    }

    @Test
    void theValueOfAnInverterChannelIsReadFromItsOwnContainer() {
        RscpValue value = E3DCThingHandler.findValue(inverterResponse(1),
                spec(E3DCBindingConstants.GROUP_INVERTER, "ac-voltage-l1"), null);

        assertNotNull(value, "the value sits in the container the parameter is answered with");
        assertEquals(237.2, numberOf(value), 0.001);
    }

    @Test
    void anInverterValueWithoutAContainerIsReadFromTheContainerItself() {
        RscpValue value = E3DCThingHandler.findValue(inverterResponse(1),
                spec(E3DCBindingConstants.GROUP_INVERTER, "on-grid"), null);

        assertNotNull(value);
        assertEquals(Boolean.FALSE, value.getValue(), "the device reports this flag as a plain value");
    }

    private static E3DCChannelSpec trackerCurrent(int tracker) {
        E3DCChannelSpec spec = E3DCInverterChannels
                .specs(new E3DCInverterChannels.Found(new java.util.TreeSet<>(java.util.Set.of(tracker)),
                        new java.util.TreeSet<>()))
                .get("tracker-" + (tracker + 1) + "-current");
        assertNotNull(spec);
        return spec;
    }

    @Test
    void theTrackersSelectTheirOwnContainer() {
        RscpValue first = E3DCThingHandler.findValue(inverterResponse(2), trackerCurrent(0), null);
        RscpValue second = E3DCThingHandler.findValue(inverterResponse(2), trackerCurrent(1), null);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(0.5, numberOf(first), 0.001);
        assertEquals(1.5, numberOf(second), 0.001);
    }

    @Test
    void aTrackerWithoutItsOwnContainerHasNoValue() {
        assertNull(E3DCThingHandler.findValue(inverterResponse(1), trackerCurrent(1), null),
                "one tracker cannot fill the channel of the second one");
    }

    /** A container that carries no index at all is still told apart by its position. */
    @Test
    void containersWithoutAnIndexAreSelectedByPosition() {
        RscpValue unindexed = RscpValue
                .container(0,
                        List.of(RscpValue
                                .container(
                                        RscpTag.PVI_DATA, List.of(
                                                RscpValue.container(
                                                        RscpTag.PVI_DC_CURRENT,
                                                        List.of(RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32,
                                                                1.0f))),
                                                RscpValue.container(RscpTag.PVI_DC_CURRENT, List.of(
                                                        RscpValue.of(RscpTag.PVI_VALUE, RscpType.FLOAT32, 2.0f)))))));

        RscpValue second = E3DCThingHandler.findValue(unindexed, trackerCurrent(1), null);

        assertNotNull(second);
        assertEquals(2.0, numberOf(second), 0.001);
    }

    @Test
    void aHistoryValueIsStillReadThroughItsSumContainer() {
        RscpValue value = E3DCThingHandler.findValue(historyResponse(),
                spec(E3DCBindingConstants.GROUP_POWER, "autarky"), null);

        assertNotNull(value);
        assertEquals(42.0, numberOf(value), 0.001);
    }

    /**
     * The interface reports plain booleans as well, for example the charging mode of a wallbox. A value no
     * state is derived from leaves its channel at whatever it showed before.
     */
    @Test
    void aBooleanValueBecomesASwitchState() {
        E3DCChannelSpec sunMode = spec(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, "sun-mode");

        assertEquals(OnOffType.OFF,
                E3DCThingHandler.valueState(RscpValue.of(RscpTag.WB_SUN_MODE_ACTIVE, RscpType.BOOL, false), sunMode));
        assertEquals(OnOffType.ON,
                E3DCThingHandler.valueState(RscpValue.of(RscpTag.WB_SUN_MODE_ACTIVE, RscpType.BOOL, true), sunMode));
    }

    @Test
    void aNumberIsScaledAndTextIsPublishedAsText() {
        RscpValue number = RscpValue.of(RscpTag.PVI_AC_VOLTAGE, RscpType.FLOAT32, 237.2f);
        State voltage = E3DCThingHandler.valueState(number, spec(E3DCBindingConstants.GROUP_INVERTER, "ac-voltage-l1"));
        assertTrue(voltage instanceof DecimalType, "a measurement is published as a number");
        assertEquals(237.2, ((DecimalType) voltage).doubleValue(), 0.001);

        RscpValue text = RscpValue.of(RscpTag.INFO_SW_RELEASE, RscpType.CSTRING, "S10_2026_027");
        assertEquals(new StringType("S10_2026_027"),
                E3DCThingHandler.valueState(text, spec(E3DCBindingConstants.GROUP_SYSTEM, "software-version")));
    }

    @Test
    void textTheDeviceSendsAsBytesIsDecodedAndNumbersOnTextChannelsStayText() {
        E3DCChannelSpec name = spec(E3DCBindingConstants.GROUP_MODULE, "device-name");
        RscpValue bytes = RscpValue.of(RscpTag.BAT_DCB_DEVICE_NAME, RscpType.CSTRING,
                "BAT_2\0".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(new StringType("BAT_2"), E3DCThingHandler.valueState(bytes, name, true));

        E3DCChannelSpec firmware = spec(E3DCBindingConstants.GROUP_MODULE, "firmware-version");
        RscpValue version = RscpValue.of(RscpTag.BAT_DCB_FW_VERSION, RscpType.UINT32, 260L);
        assertEquals(new StringType("260"), E3DCThingHandler.valueState(version, firmware, true));
        assertTrue(E3DCThingHandler.valueState(version, firmware, false) instanceof DecimalType);
    }
}
