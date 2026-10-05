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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for Modbus conversion and command encoding.
 *
 * @author Michael Weger - Initial contribution
 */
class KeContactModbusHandlerTest {

    @Test
    @Timeout(20)
    void persistedModelDoesNotReplaceIdentificationOnNewConnections() throws Exception {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:persistedmodel"))
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.1")))
                .withProperties(Map.of("modbusModel", "P30", "model", "P30")).build();
        ModbusManager manager = Objects.requireNonNull(mock(ModbusManager.class));
        ModbusCommunicationInterface firstComms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        ModbusCommunicationInterface secondComms = Objects.requireNonNull(mock(ModbusCommunicationInterface.class));
        when(manager.newModbusCommunicationInterface(any(), any())).thenReturn(firstComms, secondComms);
        record Identification(ModbusReadRequestBlueprint request, ModbusReadCallback callback) {
            void respond(int product) {
                callback.handle(
                        new AsyncModbusReadResult(request, new ModbusRegisterArray(product >>> 16, product & 0xffff)));
            }
        }
        BlockingQueue<Identification> identifications = new LinkedBlockingQueue<>();
        for (ModbusCommunicationInterface comms : java.util.List.of(firstComms, secondComms)) {
            when(comms.submitOneTimePoll(any(), any(), any())).thenAnswer(invocation -> {
                ModbusReadRequestBlueprint request = invocation.getArgument(0);
                if (request.getReference() == 1016) {
                    identifications.add(new Identification(request, invocation.getArgument(1)));
                }
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            });
            when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any()))
                    .thenReturn(Objects.requireNonNull(mock(PollTask.class)));
        }
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        when(listener.isLinked(any())).thenReturn(true);
        KeContactModbusHandler handler = new KeContactModbusHandler(thing, manager, null, listener);
        try {
            handler.initialize();
            Identification first = Objects.requireNonNull(identifications.poll(5, TimeUnit.SECONDS));
            verify(firstComms, never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
            first.respond(4212311);
            assertEquals("P40", handler.getThing().getProperties().get("modbusModel"));
            verify(firstComms).registerRegularPoll(argThat(request -> request.getReference() == 1200), anyLong(),
                    anyLong(), any(), any());
            handler.refreshLinkedPolls();
            verify(firstComms, times(1)).submitOneTimePoll(argThat(request -> request.getReference() == 1016), any(),
                    any());

            handler.handleConfigurationUpdate(Map.of("ipAddress", "192.0.2.2"));
            Identification second = Objects.requireNonNull(identifications.poll(5, TimeUnit.SECONDS));
            verify(firstComms).close();
            verify(secondComms, never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
            second.respond(304111);
            assertEquals("P30", handler.getThing().getProperties().get("modbusModel"));
            verify(secondComms, never()).registerRegularPoll(argThat(request -> request.getReference() == 1200),
                    anyLong(), anyLong(), any(), any());
        } finally {
            handler.dispose();
        }
    }

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
    void convertsPhaseCountsToModbusPhaseSwitchValues() {
        assertEquals(0,
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH, new DecimalType(1)));
        assertEquals(1,
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH, new DecimalType(3)));
        assertEquals(0, KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH,
                new DecimalType("1.0")));
        for (String value : java.util.List.of("0", "2", "4", "-1", "1.5", "3.1")) {
            assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH,
                    new DecimalType(value)));
        }
        assertNull(KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH, OnOffType.ON));
        assertNull(
                KeContactModbusHandler.toRawValue(KebaModbusWriteRegister.TRIGGER_PHASE_SWITCH, new StringType("1")));
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
