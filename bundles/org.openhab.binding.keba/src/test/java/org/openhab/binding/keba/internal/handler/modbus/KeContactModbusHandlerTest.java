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
package org.openhab.binding.keba.internal.handler.modbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * Tests for Modbus conversion and command encoding.
 *
 * @author MikeTheTux - Initial contribution
 */
class KeContactModbusHandlerTest {

    @Test
    void convertsSwitchCommands() {
        assertEquals(1, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.ENABLE_DISABLE, OnOffType.ON));
        assertEquals(0, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.ENABLE_DISABLE, OnOffType.OFF));
    }

    @Test
    void convertsUnlockTriggerAndRejectsOff() {
        assertEquals(0, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.UNLOCK_PLUG, OnOffType.ON));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.UNLOCK_PLUG, OnOffType.OFF));
        assertEquals(1, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.FAILSAFE_PERSIST, OnOffType.ON));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.FAILSAFE_PERSIST, OnOffType.OFF));
        assertEquals(1,
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.ACTIVATE_FAST_CHARGING, OnOffType.ON));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.ACTIVATE_FAST_CHARGING, OnOffType.OFF));
    }

    @Test
    void convertsAndBoundsPlainNumbers() {
        assertEquals(4,
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_PHASE_SWITCH_SOURCE, new DecimalType(4)));
        assertNull(
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_PHASE_SWITCH_SOURCE, new DecimalType(5)));
        assertNull(
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH, new DecimalType(-1)));
    }

    @Test
    void scalesAndBoundsCurrent() {
        assertEquals(12345, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_CHARGING_CURRENT,
                new QuantityType<>(12.345, Units.AMPERE)));
        assertEquals(63000, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_CHARGING_CURRENT,
                new QuantityType<>(63, Units.AMPERE)));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_CHARGING_CURRENT,
                new QuantityType<>(63.001, Units.AMPERE)));
    }

    @Test
    void scalesAndBoundsEnergy() {
        assertEquals(123, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_ENERGY,
                new QuantityType<>(1234, Units.WATT_HOUR)));
        assertEquals(65535, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_ENERGY,
                new QuantityType<>(655350, Units.WATT_HOUR)));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_ENERGY,
                new QuantityType<>(655360, Units.WATT_HOUR)));
    }

    @Test
    void scalesAndBoundsTime() {
        assertEquals(13, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_FAILSAFE_TIMEOUT,
                new QuantityType<>(12.6, Units.SECOND)));
        assertEquals(65535, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_FAILSAFE_TIMEOUT,
                new QuantityType<>(65535, Units.SECOND)));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_FAILSAFE_TIMEOUT,
                new QuantityType<>(65536, Units.SECOND)));
    }

    @Test
    void rejectsUint16AndRegisterSpecificBoundaries() {
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_ENERGY, new DecimalType(-1)));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_ENERGY, new DecimalType(65536)));
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.SET_FAILSAFE_CURRENT,
                new DecimalType(63001)));
    }

    @Test
    void convertsReadValuesToEngineeringUnits() {
        assertEquals(new QuantityType<>(12.345, Units.AMPERE),
                KeContactModbusHandler.toState(KebaModbusReadRegister.CURRENT_L1, new DecimalType(12345)));
        assertEquals(new QuantityType<>(1234.5, Units.WATT_HOUR),
                KeContactModbusHandler.toState(KebaModbusReadRegister.SESSION_ENERGY, new DecimalType(12345)));
        assertEquals(new QuantityType<>(123.0, Units.VOLT),
                KeContactModbusHandler.toState(KebaModbusReadRegister.VOLTAGE_L1, new DecimalType(123)));
        assertEquals(new QuantityType<>(12.345, Units.WATT),
                KeContactModbusHandler.toState(KebaModbusReadRegister.ACTIVE_POWER, new DecimalType(12345)));
        assertEquals(new QuantityType<>(12.3, Units.PERCENT),
                KeContactModbusHandler.toState(KebaModbusReadRegister.POWER_FACTOR, new DecimalType(123)));
        assertEquals(new QuantityType<>(123, Units.SECOND),
                KeContactModbusHandler.toState(KebaModbusReadRegister.FAILSAFE_TIMEOUT_SETTING, new DecimalType(123)));
    }

    @Test
    void convertsReadValuesToStringAndNumberStates() {
        assertEquals(new StringType("12345"),
                KeContactModbusHandler.toState(KebaModbusReadRegister.SERIAL, new DecimalType(12345)));
        assertEquals(new StringType("00003039"),
                KeContactModbusHandler.toState(KebaModbusReadRegister.RFID_TAG, new DecimalType(12345)));
        assertEquals(new DecimalType(12345),
                KeContactModbusHandler.toState(KebaModbusReadRegister.STATE, new DecimalType(12345)));
        assertEquals(new DecimalType(304111),
                KeContactModbusHandler.toState(KebaModbusReadRegister.PRODUCT_INFO, new DecimalType(304111)));
        assertEquals(new StringType("030A0D00"),
                KeContactModbusHandler.toState(KebaModbusReadRegister.SOFTWARE_VERSION, new DecimalType(50990336)));
        assertEquals(new DecimalType(4),
                KeContactModbusHandler.toState(KebaModbusReadRegister.FAST_CHARGING_STATUS, new DecimalType(4)));
    }

    @Test
    void registersP30AndP40ModelSpecificAddresses() {
        assertEquals(1016, KebaModbusReadRegister.PRODUCT_INFO.getAddress());
        assertEquals(1018, KebaModbusReadRegister.SOFTWARE_VERSION.getAddress());
        assertEquals(1200, KebaModbusReadRegister.FAST_CHARGING_STATUS.getAddress());
        assertEquals(1700, KebaModbusReadRegister.HARDWARE_REVISION_DEVICE.getAddress());
        assertEquals(1702, KebaModbusReadRegister.HARDWARE_REVISION_KC_MS10.getAddress());
        assertEquals("hardwareRevisionDevice",
                KeContactModbusHandler.propertyName(KebaModbusReadRegister.HARDWARE_REVISION_DEVICE));
        assertEquals("hardwareRevisionKcMs10",
                KeContactModbusHandler.propertyName(KebaModbusReadRegister.HARDWARE_REVISION_KC_MS10));
        assertTrue(KebaModbusReadRegister.FAST_CHARGING_STATUS.isOptional());
        assertTrue(KebaModbusReadRegister.HARDWARE_REVISION_DEVICE.isOptional());
        assertTrue(KebaModbusReadRegister.HARDWARE_REVISION_KC_MS10.isOptional());
        assertEquals(5020, KebaModbusWriteRegister.FAILSAFE_PERSIST.getAddress());
        assertEquals(5200, KebaModbusWriteRegister.ACTIVATE_FAST_CHARGING.getAddress());
    }

    @Test
    void identifiesModelFromProductFamilyCode() {
        assertTrue(KeContactModbusHandler.isP30Product(304111));
        assertFalse(KeContactModbusHandler.isP40Product(304111));
        assertTrue(KeContactModbusHandler.isP40Product(4212311));
        assertFalse(KeContactModbusHandler.isP30Product(4212311));
        assertFalse(KeContactModbusHandler.isP30Product(123456));
        assertFalse(KeContactModbusHandler.isP40Product(123456));
    }
}
