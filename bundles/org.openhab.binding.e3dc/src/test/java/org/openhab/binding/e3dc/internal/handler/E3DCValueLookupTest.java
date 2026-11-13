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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCInverterChannels;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
                        : E3DCBindingConstants.GROUP_METER.equals(group)
                                ? E3DCChannelSpec.powerMeter().spec(group, channel)
                                : E3DCChannelSpec.powerPlant().spec(group, channel);
        assertNotNull(spec, "the channel " + group + "#" + channel + " must exist");
        return spec;
    }

    private static double numberOf(RscpValue value) {
        Number number = value.asNumber();
        assertNotNull(number, "the value must be a number");
        return number.doubleValue();
    }

    /** A response with the shape the device answers the module request of two batteries with. */
    private static RscpValue twoBatteries(int firstVoltage, int secondVoltage) {
        return RscpValue.container(0, List.of(battery(0, firstVoltage), battery(1, secondVoltage)));
    }

    /** The data of one battery with exactly one cell module, which reports its index and its voltage. */
    private static RscpValue battery(int index, int voltage) {
        return RscpValue.container(RscpTag.BAT_DATA,
                List.of(RscpValue.uchar8(RscpTag.BAT_INDEX, index),
                        RscpValue.container(RscpTag.BAT_DCB_INFO, List.of(RscpValue.uchar8(RscpTag.BAT_DCB_INDEX, 0),
                                RscpValue.of(RscpTag.BAT_DCB_VOLTAGE, RscpType.FLOAT32, (float) voltage)))));
    }

    /**
     * The module index counts inside its battery, so two batteries report module 0 and the value of this
     * module has to come from the data container of its own battery.
     */
    @Test
    void aModuleReadsTheValueOfItsOwnBattery() {
        RscpValue response = twoBatteries(1, 2);
        E3DCChannelSpec voltage = spec(E3DCBindingConstants.GROUP_MODULE, "voltage");

        RscpValue first = Objects.requireNonNull(E3DCBatteryModuleHandler.batteryData(response, 0));
        RscpValue second = Objects.requireNonNull(E3DCBatteryModuleHandler.batteryData(response, 1));
        RscpValue firstModule = E3DCThingHandler.findValue(first, voltage, 0);
        RscpValue secondModule = E3DCThingHandler.findValue(second, voltage, 0);
        assertNotNull(firstModule, "battery 0 reports the module with the voltage 1");
        assertNotNull(secondModule, "battery 1 reports the module with the voltage 2");

        assertEquals(1.0, numberOf(firstModule), 0.001, "the module of battery 0 carries the voltage of battery 0");
        assertEquals(2.0, numberOf(secondModule), 0.001, "the module of battery 1 carries the voltage of battery 1");
        assertNull(E3DCBatteryModuleHandler.batteryData(response, 2),
                "a battery the device does not report has no data");
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
                E3DCThingHandler.valueState(text, spec(E3DCBindingConstants.GROUP_IDLE_PERIODS, "charge-periods")));
    }

    @Test
    void textTheDeviceSendsAsBytesIsDecodedAndNumbersOnTextChannelsStayText() {
        E3DCChannelSpec text = spec(E3DCBindingConstants.GROUP_IDLE_PERIODS, "charge-periods");
        RscpValue bytes = RscpValue.of(RscpTag.EMS_PERIOD_NAME, RscpType.CSTRING,
                "weekend\0".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(new StringType("weekend"), E3DCThingHandler.valueState(bytes, text, true));

        RscpValue version = RscpValue.of(RscpTag.BAT_DCB_FW_VERSION, RscpType.UINT32, 260L);
        assertEquals(new StringType("260"), E3DCThingHandler.valueState(version, text, true));
        assertTrue(E3DCThingHandler.valueState(version, text, false) instanceof DecimalType);
    }

    /** A thing property states a specification number together with its unit. */
    @Test
    void aPropertyStatesANumberWithItsUnit() {
        assertEquals("64 V", E3DCThingHandler.quantity(64.0, "V"));
        assertEquals("64.5 V", E3DCThingHandler.quantity(64.5, "V"));
        assertEquals("100 A", E3DCThingHandler.quantity(100, "A"));
        assertEquals("8", E3DCThingHandler.quantity(8, ""));
    }

    /** A meter response that holds exactly one device. */
    private static RscpValue meter(int index, double power) {
        return RscpValue.container(0,
                List.of(RscpValue.container(RscpTag.PM_DATA, List.of(RscpValue.uchar8(RscpTag.PM_INDEX, index),
                        RscpValue.of(RscpTag.PM_POWER_L1, RscpType.DOUBLE64, power)))));
    }

    /**
     * A device that is absent from the response has no value: the container of another device must not fill
     * its channels.
     */
    @Test
    void aDeviceTheResponseDoesNotHoldHasNoValue() {
        E3DCChannelSpec power = spec(E3DCBindingConstants.GROUP_METER, "power-l1");

        assertNotNull(E3DCThingHandler.findValue(meter(0, 1234.0), power, 0));
        assertNull(E3DCThingHandler.findValue(meter(0, 1234.0), power, 1),
                "the values of device 0 must not stand in for device 1");
    }

    @Test
    void aResponseWithoutAnIndexIsReadByPosition() {
        RscpValue unindexed = RscpValue.container(0, List.of(RscpValue.container(RscpTag.PM_DATA,
                List.of(RscpValue.of(RscpTag.PM_POWER_L1, RscpType.DOUBLE64, 1234.0)))));
        E3DCChannelSpec power = spec(E3DCBindingConstants.GROUP_METER, "power-l1");

        assertNotNull(E3DCThingHandler.findValue(unindexed, power, 1),
                "a device that carries no index of its own is told apart by position");
    }

    private static QuantityType<?> quantityOf(@Nullable State state) {
        assertTrue(state instanceof QuantityType<?>, "the state must be a quantity, but was " + state);
        return (QuantityType<?>) state;
    }

    @Test
    void aMeasurementIsPublishedInTheUnitOfItsDevice() {
        E3DCChannelSpec power = spec(E3DCBindingConstants.GROUP_POWER, "solar-power");
        QuantityType<?> watts = quantityOf(E3DCThingHandler.withUnit(new DecimalType(2000), "Number:Power", power));
        assertEquals(2000.0, watts.doubleValue(), 0.001);
        assertEquals(Units.WATT, watts.getUnit());

        E3DCChannelSpec voltage = spec(E3DCBindingConstants.GROUP_METER, "voltage-l1");
        assertEquals(Units.VOLT,
                quantityOf(E3DCThingHandler.withUnit(new DecimalType(237.2), "Number:ElectricPotential", voltage))
                        .getUnit());

        E3DCChannelSpec share = spec(E3DCBindingConstants.GROUP_POWER, "autarky");
        assertEquals(Units.PERCENT,
                quantityOf(E3DCThingHandler.withUnit(new DecimalType(42), "Number:Dimensionless", share)).getUnit());
    }

    /**
     * The device reports every energy counter in Wh, and the ones that are divided by a thousand are published
     * in kWh. A counter that keeps its raw value stays in Wh.
     */
    @Test
    void anEnergyCounterIsPublishedInTheUnitItIsScaledTo() {
        E3DCChannelSpec scaled = spec(E3DCBindingConstants.GROUP_METER, "energy-l1");
        QuantityType<?> kilowattHours = quantityOf(
                E3DCThingHandler.withUnit(new DecimalType(12.5), "Number:Energy", scaled));
        assertEquals(12.5, kilowattHours.doubleValue(), 0.001);
        assertEquals(Units.KILOWATT_HOUR, kilowattHours.getUnit());

        E3DCChannelSpec raw = spec(E3DCBindingConstants.GROUP_EMERGENCY, "energy");
        assertEquals(Units.WATT_HOUR,
                quantityOf(E3DCThingHandler.withUnit(new DecimalType(450), "Number:Energy", raw)).getUnit());
    }

    @Test
    void aChannelWithoutADimensionKeepsAPlainNumber() {
        // the capacities are reported in Ah on a channel whose item type is a plain number
        E3DCChannelSpec capacity = spec(E3DCBindingConstants.GROUP_MODULE, "remaining-capacity");

        State state = E3DCThingHandler.withUnit(new DecimalType(38.5), "Number", capacity);

        assertTrue(state instanceof DecimalType, "a value whose channel carries no dimension stays a plain number");
    }
}
