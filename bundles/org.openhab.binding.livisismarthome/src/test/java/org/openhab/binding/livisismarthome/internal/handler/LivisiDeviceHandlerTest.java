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
package org.openhab.binding.livisismarthome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;
import static org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_AUTO;
import static org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_MANUAL;
import static org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO.LINK_TYPE_CAPABILITY;
import static org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO.LINK_TYPE_DEVICE;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionType;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityConfigDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceConfigDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventPropertiesDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.State;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LivisiDeviceHandlerTest {

    private @NonNullByDefault({}) LivisiBridgeHandler bridgeHandlerMock;
    private @NonNullByDefault({}) ThingStatusInfo thingStatusInfo;
    private @NonNullByDefault({}) Map<String, State> updatedChannels;
    private @NonNullByDefault({}) Set<TriggeredEvent> triggeredChannels;

    private @NonNullByDefault({}) Level previousLoggingLevel;

    @BeforeEach
    public void before() {
        bridgeHandlerMock = mock(LivisiBridgeHandler.class);
        thingStatusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build();
        updatedChannels = new LinkedHashMap<>();
        triggeredChannels = new LinkedHashSet<>();

        Logger logger = (Logger) LoggerFactory.getLogger(LivisiDeviceHandler.class);
        previousLoggingLevel = logger.getLevel();
        logger.setLevel(Level.OFF); // avoid log messages (which would be logged to the console)
    }

    @AfterEach
    public void after() {
        Logger logger = (Logger) LoggerFactory.getLogger(LivisiDeviceHandler.class);
        logger.setLevel(previousLoggingLevel);
    }

    @Test
    public void testInitialize() {
        LivisiDeviceHandler deviceHandler = createDeviceHandler(createDevice());
        assertEquals(ThingStatus.ONLINE, deviceHandler.getThing().getStatus());
    }

    @Test
    public void testOnDeviceStateChanged() {
        DeviceDTO device = createDevice();
        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
    }

    @Test
    public void testOnDeviceStateChangedIsReachable() {
        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setReachable(true);

        DeviceDTO device = createDevice();
        device.setDeviceState(deviceState);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertEquals(ThingStatus.ONLINE, deviceHandler.getThing().getStatus());
        assertEquals(ThingStatusDetail.NONE, deviceHandler.getThing().getStatusInfo().getStatusDetail());
    }

    @Test
    public void testOnDeviceStateChangedIsNotReachable() {
        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setReachable(false);

        DeviceDTO device = createDevice();
        device.setDeviceState(deviceState);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertEquals(ThingStatus.OFFLINE, deviceHandler.getThing().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, deviceHandler.getThing().getStatusInfo().getStatusDetail());
    }

    @Test
    public void testOnDeviceStateChangedIsReachableVariableActuator() {
        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setReachable(true);

        DeviceDTO device = createDevice();
        device.setType(DEVICE_VARIABLE_ACTUATOR);
        device.setDeviceState(deviceState);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertEquals(ThingStatus.ONLINE, deviceHandler.getThing().getStatus());
        assertEquals(ThingStatusDetail.NONE, deviceHandler.getThing().getStatusInfo().getStatusDetail());
    }

    @Test
    public void testOnDeviceStateChangedLowBattery() {
        DeviceDTO device = createDevice();
        device.setIsBatteryPowered(true);
        device.setLowBattery(true);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_BATTERY_LOW, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedNoLowBattery() {
        DeviceDTO device = createDevice();
        device.setIsBatteryPowered(true);
        device.setLowBattery(false);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_BATTERY_LOW, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedNotBatteryPowered() {
        DeviceDTO device = createDevice();
        device.setIsBatteryPowered(false);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_BATTERY_LOW));
    }

    @Test
    public void testOnDeviceStateChangedVariableActuatorOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_VARIABLEACTUATOR, c -> c.setVariableActuatorState(true), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedVariableActuatorOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_VARIABLEACTUATOR, c -> c.setVariableActuatorState(false), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedVariableActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_VARIABLEACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_SWITCH));
    }

    @Test
    public void testOnDeviceStateChangedTemperatureSensorFrostWarningOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TEMPERATURESENSOR, c -> {
            c.setTemperatureSensorTemperatureState(21.5);
            c.setTemperatureSensorFrostWarningState(true);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_CURRENT_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_FROST_WARNING, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedTemperatureSensorFrostWarningOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TEMPERATURESENSOR, c -> {
            c.setTemperatureSensorTemperatureState(21.5);
            c.setTemperatureSensorFrostWarningState(false);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_CURRENT_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_FROST_WARNING, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedTemperatureSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TEMPERATURESENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_CURRENT_TEMPERATURE));
        assertFalse(isChannelUpdated(CHANNEL_FROST_WARNING));
    }

    @Test
    public void testOnDeviceStateChangedThermostatActuatorWindowReductionOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_THERMOSTATACTUATOR, c -> {
            c.setThermostatActuatorPointTemperatureState(21.5);
            c.setThermostatActuatorOperationModeState(STATE_VALUE_OPERATION_MODE_AUTO);
            c.setThermostatActuatorWindowReductionActiveState(true);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_TARGET_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_MODE, new StringType(STATE_VALUE_OPERATION_MODE_AUTO)));
        assertTrue(isChannelUpdated(CHANNEL_WINDOW_REDUCTION_ACTIVE, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedThermostatActuatorWindowReductionOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_THERMOSTATACTUATOR, c -> {
            c.setThermostatActuatorPointTemperatureState(21.5);
            c.setThermostatActuatorOperationModeState(STATE_VALUE_OPERATION_MODE_MANUAL);
            c.setThermostatActuatorWindowReductionActiveState(false);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_TARGET_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_MODE, new StringType(STATE_VALUE_OPERATION_MODE_MANUAL)));
        assertTrue(isChannelUpdated(CHANNEL_WINDOW_REDUCTION_ACTIVE, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedThermostatActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_THERMOSTATACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_TARGET_TEMPERATURE));
        assertFalse(isChannelUpdated(CHANNEL_OPERATION_MODE));
        assertFalse(isChannelUpdated(CHANNEL_WINDOW_REDUCTION_ACTIVE));
    }

    @Test
    public void testOnDeviceStateChangedHumiditySensorMoldWarningOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_HUMIDITYSENSOR, c -> {
            c.setHumiditySensorHumidityState(35.5);
            c.setHumiditySensorMoldWarningState(true);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_HUMIDITY, QuantityType.valueOf(35.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MOLD_WARNING, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedHumiditySensorMoldWarningOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_HUMIDITYSENSOR, c -> {
            c.setHumiditySensorHumidityState(35.5);
            c.setHumiditySensorMoldWarningState(false);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_HUMIDITY, QuantityType.valueOf(35.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MOLD_WARNING, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedHumiditySensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_HUMIDITYSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_HUMIDITY));
        assertFalse(isChannelUpdated(CHANNEL_MOLD_WARNING));
    }

    @Test
    public void testOnDeviceStateChangedWindowDoorSensorOpen() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_WINDOWDOORSENSOR, c -> c.setWindowDoorSensorState(true), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_CONTACT, OpenClosedType.OPEN));
    }

    @Test
    public void testOnDeviceStateChangedWindowDoorSensorClosed() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_WINDOWDOORSENSOR, c -> c.setWindowDoorSensorState(false), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_CONTACT, OpenClosedType.CLOSED));
    }

    @Test
    public void testOnDeviceStateChangedWindowDoorSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_WINDOWDOORSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_CONTACT));
    }

    @Test
    public void testOnDeviceStateChangedSmokeDetectorSensorOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SMOKEDETECTORSENSOR, c -> c.setSmokeDetectorSensorState(true), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SMOKE, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedSmokeDetectorSensorOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SMOKEDETECTORSENSOR, c -> c.setSmokeDetectorSensorState(false),
                device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SMOKE, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedSmokeDetectorSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SMOKEDETECTORSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_SMOKE));
    }

    @Test
    public void testOnDeviceStateChangedAlarmActuatorOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ALARMACTUATOR, c -> c.setAlarmActuatorState(true), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ALARM, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedAlarmActuatorOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ALARMACTUATOR, c -> c.setAlarmActuatorState(false), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ALARM, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedAlarmActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ALARMACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ALARM));
    }

    @Test
    public void testOnDeviceStateChangedSwitchActuatorOn() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SWITCHACTUATOR, c -> c.setSwitchActuatorState(true), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedSwitchActuatorOff() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SWITCHACTUATOR, c -> c.setSwitchActuatorState(false), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.OFF));
    }

    @Test
    public void testOnDeviceStateChangedSwitchActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SWITCHACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_SWITCH));
    }

    @Test
    public void testOnDeviceStateChangedDimmerActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_DIMMERACTUATOR, c -> c.setDimmerActuatorState(50), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_DIMMER, new DecimalType(50)));
    }

    @Test
    public void testOnDeviceStateChangedDimmerActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_DIMMERACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_DIMMER));
    }

    @Test
    public void testOnDeviceStateChangedRollerShutterActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, c -> c.setRollerShutterActuatorState(40),
                device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(40)));
    }

    @Test
    public void testOnDeviceStateChangedRollerShutterActuatorInvertTrue() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, c -> c.setRollerShutterActuatorState(40),
                device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        Channel rollerShutterChannelMock = createRollerShutterChannelMock(true);

        Thing thingMock = deviceHandler.getThing();
        when(thingMock.getChannel(CHANNEL_ROLLERSHUTTER)).thenReturn(rollerShutterChannelMock);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(40)));
        assertFalse(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(60)));
    }

    @Test
    public void testOnDeviceStateChangedRollerShutterActuatorInvertFalse() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, c -> c.setRollerShutterActuatorState(40),
                device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        Channel rollerShutterChannelMock = createRollerShutterChannelMock(false);

        Thing thingMock = deviceHandler.getThing();
        when(thingMock.getChannel(CHANNEL_ROLLERSHUTTER)).thenReturn(rollerShutterChannelMock);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(40)));
        assertTrue(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(60)));
    }

    @Test
    public void testOnDeviceStateChangedRollerShutterActuatorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ROLLERSHUTTER));
    }

    @Test
    public void testOnDeviceStateChangedMotionDetectionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_MOTIONDETECTIONSENSOR, c -> c.setMotionDetectionSensorState(50),
                device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_MOTION_COUNT, new DecimalType(50)));
    }

    @Test
    public void testOnDeviceStateChangedMotionDetectionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_MOTIONDETECTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_MOTION_COUNT));
    }

    @Test
    public void testOnDeviceStateChangedLuminanceSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_LUMINANCESENSOR, c -> c.setLuminanceSensorState(50.1), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_LUMINANCE, QuantityType.valueOf(50.1, Units.PERCENT)));
    }

    @Test
    public void testOnDeviceStateChangedLuminanceSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_LUMINANCESENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_LUMINANCE));
    }

    @Test
    public void testOnDeviceStateChangedEnergyConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ENERGYCONSUMPTIONSENSOR, c -> {
            c.setEnergyConsumptionSensorEnergyConsumptionMonthKWhState(201.51);
            c.setEnergyConsumptionSensorAbsoluteEnergyConsumptionState(5500.51);
            c.setEnergyConsumptionSensorEnergyConsumptionMonthEuroState(80.32);
            c.setEnergyConsumptionSensorEnergyConsumptionDayEuroState(3.72);
            c.setEnergyConsumptionSensorEnergyConsumptionDayKWhState(8.71);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH,
                QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ABOLUTE_ENERGY_CONSUMPTION,
                QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_EURO, new DecimalType(3.72)));
        assertTrue(
                isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedEnergyConsumptionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ENERGYCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH));
        assertFalse(isChannelUpdated(CHANNEL_ABOLUTE_ENERGY_CONSUMPTION));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_KWH));
    }

    @Test
    public void testOnDeviceStateChangedPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_POWERCONSUMPTIONSENSOR,
                c -> c.setPowerConsumptionSensorPowerConsumptionWattState(350.5), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_POWER_CONSUMPTION_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testOnDeviceStateChangedPowerConsumptionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_POWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_POWER_CONSUMPTION_WATT));
    }

    @Test
    public void testOnDeviceStateChangedGenerationMeterEnergySensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERENERGYSENSOR, c -> {
            c.setGenerationMeterEnergySensorEnergyPerMonthInKWhState(201.51);
            c.setGenerationMeterEnergySensorTotalEnergyState(5500.51);
            c.setGenerationMeterEnergySensorEnergyPerMonthInEuroState(80.32);
            c.setGenerationMeterEnergySensorEnergyPerDayInEuroState(3.72);
            c.setGenerationMeterEnergySensorEnergyPerDayInKWhState(8.71);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_KWH,
                QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(
                isChannelUpdated(CHANNEL_TOTAL_ENERGY_GENERATION, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_EURO, new DecimalType(3.72)));
        assertTrue(
                isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedGenerationMeterEnergySensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERENERGYSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_KWH));
        assertFalse(isChannelUpdated(CHANNEL_TOTAL_ENERGY_GENERATION));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_KWH));
    }

    @Test
    public void testOnDeviceStateChangedGenerationMeterPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR,
                c -> c.setGenerationMeterPowerConsumptionSensorPowerInWattState(350.5), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_POWER_GENERATION_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testOnDeviceStateChangedGenerationMeterPowerConsumptionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_POWER_GENERATION_WATT));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterEnergyConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR, c -> {
            c.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(201.51);
            c.setTwoWayMeterEnergyConsumptionSensorTotalEnergyState(5500.51);
            c.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(80.32);
            c.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(3.72);
            c.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(8.71);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_MONTH_KWH, QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_TOTAL_ENERGY, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_DAY_EURO, new DecimalType(3.72)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterEnergyConsumptionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_MONTH_KWH));
        assertFalse(isChannelUpdated(CHANNEL_TOTAL_ENERGY));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_MONTH_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_DAY_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_DAY_KWH));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterEnergyFeedSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYFEEDSENSOR, c -> {
            c.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(201.51);
            c.setTwoWayMeterEnergyFeedSensorTotalEnergyState(5500.51);
            c.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(80.32);
            c.setTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(3.72);
            c.setTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(8.71);
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_KWH, QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_TOTAL_ENERGY_FED, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_EURO, new DecimalType(3.72)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterEnergyFeedSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYFEEDSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_KWH));
        assertFalse(isChannelUpdated(CHANNEL_TOTAL_ENERGY_FED));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_EURO));
        assertFalse(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_KWH));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR,
                c -> c.setTwoWayMeterPowerConsumptionSensorPowerInWattState(350.5), device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated(CHANNEL_POWER_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testOnDeviceStateChangedTwoWayMeterPowerConsumptionSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_POWER_WATT));
    }

    @Test
    public void testOnDeviceStateChangedPushButtonSensorButton1ShortPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, c -> {
            c.setPushButtonSensorCounterState(10);
            c.setPushButtonSensorButtonIndexState(0);
            c.setPushButtonSensorButtonIndexType("ShortPress");
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated("button1Count", new DecimalType(10)));
        // Trigger channels should only get triggered by events (onDeviceStateChanged(device, event), not
        // onDeviceStateChanged(device)).
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedPushButtonSensorButton1LongPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, c -> {
            c.setPushButtonSensorCounterState(10);
            c.setPushButtonSensorButtonIndexState(0);
            c.setPushButtonSensorButtonIndexType("LongPress");
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated("button1Count", new DecimalType(10)));
        // Trigger channels should only get triggered by events (onDeviceStateChanged(device, event), not
        // onDeviceStateChanged(device)).
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedPushButtonSensorButton2ShortPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, c -> {
            c.setPushButtonSensorCounterState(10);
            c.setPushButtonSensorButtonIndexState(1);
            c.setPushButtonSensorButtonIndexType("ShortPress");
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated("button2Count", new DecimalType(10)));
        // Trigger channels should only get triggered by events (onDeviceStateChanged(device, event), not
        // onDeviceStateChanged(device)).
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedPushButtonSensorButton2LongPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, c -> {
            c.setPushButtonSensorCounterState(10);
            c.setPushButtonSensorButtonIndexState(1);
            c.setPushButtonSensorButtonIndexType("LongPress");
        }, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertTrue(isChannelUpdated("button2Count", new DecimalType(10)));
        // Trigger channels should only get triggered by events (onDeviceStateChanged(device, event), not
        // onDeviceStateChanged(device)).
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedPushButtonSensorEmptyState() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.onDeviceStateChanged(device);
        assertFalse(isChannelUpdated(CHANNEL_BUTTON_COUNT));
        assertFalse(isChannelUpdated("button1Count"));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedEventLinkedToDevice() {
        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setReachable(true);

        DeviceDTO device = createDevice();
        device.setDeviceState(deviceState);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = new EventDTO();
        event.setSource(LINK_TYPE_DEVICE);

        deviceHandler.onDeviceStateChanged(device, event);
        assertEquals(ThingStatus.ONLINE, deviceHandler.getThing().getStatus());
        assertEquals(ThingStatusDetail.NONE, deviceHandler.getThing().getStatusInfo().getStatusDetail());
    }

    @Test
    public void testOnDeviceStateChangedEventLinkedToCapabilityWithoutState() {
        DeviceDTO device = createDevice();

        CapabilityConfigDTO capabilityConfig = new CapabilityConfigDTO();
        capabilityConfig.setName("capabilityName");

        final String capabilityId = "capabilityId";
        CapabilityDTO capability = new CapabilityDTO();
        capability.setId(capabilityId);
        capability.setType(CapabilityDTO.TYPE_SWITCHACTUATOR);
        capability.setConfig(capabilityConfig);
        device.getCapabilityMap().put(capabilityId, capability);

        DeviceDTO refreshedDevice = createDevice();

        CapabilityStateDTO capabilityState = new CapabilityStateDTO();
        capabilityState.setSwitchActuatorState(true);

        CapabilityDTO refreshedCapability = new CapabilityDTO();
        refreshedCapability.setId(capabilityId);
        refreshedCapability.setType(CapabilityDTO.TYPE_SWITCHACTUATOR);
        refreshedCapability.setConfig(capabilityConfig);
        refreshedCapability.setCapabilityState(capabilityState);
        refreshedDevice.getCapabilityMap().put(capabilityId, refreshedCapability);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        when(bridgeHandlerMock.refreshDevice(any())).thenReturn(Optional.of(refreshedDevice));

        EventDTO event = createCapabilityEvent(c -> c.setOnState(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventLinkedToCapabilityWithoutStateAlsoAfterRefresh() {
        DeviceDTO device = createDevice();

        CapabilityConfigDTO capabilityConfig = new CapabilityConfigDTO();
        capabilityConfig.setName("capabilityName");

        final String capabilityId = "capabilityId";
        CapabilityDTO capability = new CapabilityDTO();
        capability.setId(capabilityId);
        capability.setType(CapabilityDTO.TYPE_SWITCHACTUATOR);
        capability.setConfig(capabilityConfig);
        device.getCapabilityMap().put(capabilityId, capability);

        DeviceDTO refreshedDevice = createDevice();

        CapabilityDTO refreshedCapability = new CapabilityDTO();
        refreshedCapability.setId(capabilityId);
        refreshedCapability.setType(CapabilityDTO.TYPE_SWITCHACTUATOR);
        refreshedCapability.setConfig(capabilityConfig);
        refreshedDevice.getCapabilityMap().put(capabilityId, refreshedCapability);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        when(bridgeHandlerMock.refreshDevice(any())).thenReturn(Optional.of(refreshedDevice));

        EventDTO event = createCapabilityEvent(c -> c.setOnState(true));

        deviceHandler.onDeviceStateChanged(device, event);
        // Channels should only get updated when the device have a state.
        assertFalse(isChannelUpdated(CHANNEL_SWITCH));
    }

    @Test
    public void testOnDeviceStateChangedEventVariableActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_VARIABLEACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setValue(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventSwitchActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SWITCHACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setOnState(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_SWITCH, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventDimmerActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_DIMMERACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setDimLevel(50));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_DIMMER, new DecimalType(50)));
    }

    @Test
    public void testOnDeviceStateChangedEventRollerShutterActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setShutterLevel(50));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ROLLERSHUTTER, new DecimalType(50)));
    }

    @Test
    public void testOnDeviceStateChangedEventRollerShutterPushButtonSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, null, device);
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setKeyPressCounter(10);
            c.setKeyPressButtonIndex(0);
            c.setKeyPressType("ShortPress");
        });
        event.setType("ButtonPressed");
        event.setNamespace("CosipDevices.RWE");

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated("button1Count", new DecimalType(10)));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedEventTemperatureSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TEMPERATURESENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setTemperature(21.5);
            c.setFrostWarning(true);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_CURRENT_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_FROST_WARNING, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventThermostatSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_THERMOSTATACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setPointTemperature(21.5);
            c.setOperationMode(STATE_VALUE_OPERATION_MODE_AUTO);
            c.setWindowReductionActive(true);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_TARGET_TEMPERATURE, QuantityType.valueOf(21.5, SIUnits.CELSIUS)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_MODE, new StringType(STATE_VALUE_OPERATION_MODE_AUTO)));
        assertTrue(isChannelUpdated(CHANNEL_WINDOW_REDUCTION_ACTIVE, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventHumiditySensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_HUMIDITYSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setHumidity(35.5);
            c.setMoldWarning(true);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_HUMIDITY, QuantityType.valueOf(35.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MOLD_WARNING, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventWindowDoorSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_WINDOWDOORSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setIsOpen(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_CONTACT, OpenClosedType.OPEN));
    }

    @Test
    public void testOnDeviceStateChangedEventSmokeDetectorSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_SMOKEDETECTORSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setIsSmokeAlarm(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_SMOKE, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventAlarmActuator() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ALARMACTUATOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setOnState(true));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ALARM, OnOffType.ON));
    }

    @Test
    public void testOnDeviceStateChangedEventMotionDetectionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_MOTIONDETECTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setMotionDetectedCount(50));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_MOTION_COUNT, new DecimalType(50)));
    }

    @Test
    public void testOnDeviceStateChangedEventLuminanceSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_LUMINANCESENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setLuminance(50.1));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_LUMINANCE, QuantityType.valueOf(50.1, Units.PERCENT)));
    }

    @Test
    public void testOnDeviceStateChangedEventPushButtonSensorButton1ShortPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setKeyPressCounter(10);
            c.setKeyPressButtonIndex(0);
            c.setKeyPressType("ShortPress");
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated("button1Count", new DecimalType(10)));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedEventPushButtonSensorButton1LongPress() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setKeyPressCounter(10);
            c.setKeyPressButtonIndex(0);
            c.setKeyPressType("LongPress");
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated("button1Count", new DecimalType(10)));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertTrue(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedStateChangedEventPushButtonSensorSHCClassic() {
        when(bridgeHandlerMock.isSHCClassic()).thenReturn(true);

        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        // SHC Classic sends only StateChanged events with this information
        EventDTO event = createCapabilityEvent(c -> {
            c.setLastKeyPressButtonIndex(0);
            c.setLastKeyPressCounter(10);
        });

        // Nothing should get processed, because it should only react on the more detailed ButtonPressed events
        deviceHandler.onDeviceStateChanged(device, event);
        assertFalse(isChannelUpdated("button1Count", new DecimalType(10)));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED)); // not available for SHC Classic
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED)); // not available for SHC Classic
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedStateChangedEventPushButtonSensorSHCA() {
        when(bridgeHandlerMock.isSHCClassic()).thenReturn(false);

        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_PUSHBUTTONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        // StateChanged events only have with this information
        EventDTO event = createCapabilityEvent(c -> {
            c.setLastKeyPressButtonIndex(0);
            c.setLastKeyPressCounter(10);
        });

        // Nothing should get processed, because it should only react on the more detailed ButtonPressed events
        deviceHandler.onDeviceStateChanged(device, event);
        assertFalse(isChannelUpdated("button1Count", new DecimalType(10)));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.SHORT_PRESSED));
        assertFalse(isChannelTriggered("button1", CommonTriggerEvents.LONG_PRESSED));
        assertFalse(isChannelTriggered("button2", CommonTriggerEvents.PRESSED));
    }

    @Test
    public void testOnDeviceStateChangedEventEnergyConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_ENERGYCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setEnergyConsumptionMonthKWh(201.51);
            c.setAbsoluteEnergyConsumption(5500.51);
            c.setEnergyConsumptionMonthEuro(80.32);
            c.setEnergyConsumptionDayEuro(3.72);
            c.setEnergyConsumptionDayKWh(8.71);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH,
                QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ABOLUTE_ENERGY_CONSUMPTION,
                QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_EURO, new DecimalType(3.72)));
        assertTrue(
                isChannelUpdated(CHANNEL_ENERGY_CONSUMPTION_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedEventPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_POWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setPowerConsumptionWatt(350.5));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_POWER_CONSUMPTION_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testOnDeviceStateChangedEventGenerationMeterEnergySensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERENERGYSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setEnergyPerMonthInKWh(201.51);
            c.setTotalEnergy(5500.51);
            c.setEnergyPerMonthInEuro(80.32);
            c.setEnergyPerDayInEuro(3.72);
            c.setEnergyPerDayInKWh(8.71);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_KWH,
                QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(
                isChannelUpdated(CHANNEL_TOTAL_ENERGY_GENERATION, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_EURO, new DecimalType(3.72)));
        assertTrue(
                isChannelUpdated(CHANNEL_ENERGY_GENERATION_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedEventGenerationMeterPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setPowerInWatt(350.5));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_POWER_GENERATION_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testOnDeviceStateChangedEventTwoWayMeterEnergyConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setEnergyPerMonthInKWh(201.51);
            c.setTotalEnergy(5500.51);
            c.setEnergyPerMonthInEuro(80.32);
            c.setEnergyPerDayInEuro(3.72);
            c.setEnergyPerDayInKWh(8.71);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_MONTH_KWH, QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_TOTAL_ENERGY, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_DAY_EURO, new DecimalType(3.72)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedEventTwoWayMeterEnergyFeedSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERENERGYFEEDSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> {
            c.setEnergyPerMonthInKWh(201.51);
            c.setTotalEnergy(5500.51);
            c.setEnergyPerMonthInEuro(80.32);
            c.setEnergyPerDayInEuro(3.72);
            c.setEnergyPerDayInKWh(8.71);
        });

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_KWH, QuantityType.valueOf(201.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_TOTAL_ENERGY_FED, QuantityType.valueOf(5500.51, Units.KILOWATT_HOUR)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_MONTH_EURO, new DecimalType(80.32)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_EURO, new DecimalType(3.72)));
        assertTrue(isChannelUpdated(CHANNEL_ENERGY_FEED_DAY_KWH, QuantityType.valueOf(8.71, Units.KILOWATT_HOUR)));
    }

    @Test
    public void testOnDeviceStateChangedEventTwoWayMeterPowerConsumptionSensor() {
        DeviceDTO device = createDevice();
        addCapabilityToDevice(CapabilityDTO.TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR, null, device);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        EventDTO event = createCapabilityEvent(c -> c.setPowerInWatt(350.5));

        deviceHandler.onDeviceStateChanged(device, event);
        assertTrue(isChannelUpdated(CHANNEL_POWER_WATT, QuantityType.valueOf(350.5, Units.WATT)));
    }

    @Test
    public void testHandleCommandUnsupportedChannel() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_CONTACT);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        deviceHandler.handleCommand(channelMock, OpenClosedType.OPEN);

        verify(bridgeHandlerMock, never()).commandSetRollerShutterLevel(any(), anyInt());
        verify(bridgeHandlerMock, never()).commandSwitchDevice(any(), anyBoolean());
        verify(bridgeHandlerMock, never()).commandUpdatePointTemperature(any(), anyDouble());
        verify(bridgeHandlerMock, never()).commandSwitchAlarm(any(), anyBoolean());
        verify(bridgeHandlerMock, never()).commandSetOperationMode(any(), anyBoolean());
        verify(bridgeHandlerMock, never()).commandSetDimLevel(any(), anyInt());
        verify(bridgeHandlerMock, never()).commandSetRollerShutterLevel(any(), anyInt());
        verify(bridgeHandlerMock, never()).commandSetRollerShutterStop(any(), any());
    }

    @Test
    public void testHandleCommandCommandSwitchDeviceOn() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_SWITCH);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.ON);

        verify(bridgeHandlerMock).commandSwitchDevice(device.getId(), true);
    }

    @Test
    public void testHandleCommandCommandSwitchDeviceOff() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_SWITCH);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.OFF);

        verify(bridgeHandlerMock).commandSwitchDevice(device.getId(), false);
    }

    @Test
    public void testHandleCommandCommandSwitchAlarmOn() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ALARM);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.ON);

        verify(bridgeHandlerMock).commandSwitchAlarm(device.getId(), true);
    }

    @Test
    public void testHandleCommandCommandSwitchAlarmOff() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ALARM);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.OFF);

        verify(bridgeHandlerMock).commandSwitchAlarm(device.getId(), false);
    }

    @Test
    public void testHandleCommandCommandSetDimLevelOn() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_DIMMER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.ON);

        verify(bridgeHandlerMock).commandSetDimLevel(device.getId(), 100);
    }

    @Test
    public void testHandleCommandCommandSetDimLevelOff() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_DIMMER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.OFF);

        verify(bridgeHandlerMock).commandSetDimLevel(device.getId(), 0);
    }

    @Test
    public void testHandleCommandCommandSetDimLevelDecimalType() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_DIMMER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("30"));

        verify(bridgeHandlerMock).commandSetDimLevel(device.getId(), 30);
    }

    @Test
    public void testHandleCommandCommandSetOperationModeAuto() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_OPERATION_MODE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, StringType.valueOf(STATE_VALUE_OPERATION_MODE_AUTO));

        verify(bridgeHandlerMock).commandSetOperationMode(device.getId(), true);
    }

    @Test
    public void testHandleCommandCommandSetOperationModeManual() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_OPERATION_MODE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, StringType.valueOf(STATE_VALUE_OPERATION_MODE_MANUAL));

        verify(bridgeHandlerMock).commandSetOperationMode(device.getId(), false);
    }

    @Test
    public void testHandleCommandCommandSetOperationModeUnknown() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_OPERATION_MODE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, StringType.valueOf("Unknown"));

        verify(bridgeHandlerMock, never()).commandSetOperationMode(any(), anyBoolean());
    }

    @Test
    public void testHandleCommandCommandUpdatePointTemperatureQuantityType() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_TARGET_TEMPERATURE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, QuantityType.valueOf(20.0, SIUnits.CELSIUS));

        verify(bridgeHandlerMock).commandUpdatePointTemperature(device.getId(), 20.0);
    }

    @Test
    public void testHandleCommandCommandUpdatePointTemperatureDecimalType() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_TARGET_TEMPERATURE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("20"));

        verify(bridgeHandlerMock).commandUpdatePointTemperature(device.getId(), 20.0);
    }

    @Test
    public void testHandleCommandCommandUpdatePointTemperatureMinTemperature() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_TARGET_TEMPERATURE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("0"));

        // 0 (and everything below the min temperature 6.0 should be set to the min temperature 6.0)
        verify(bridgeHandlerMock).commandUpdatePointTemperature(device.getId(), 6.0);
    }

    @Test
    public void testHandleCommandCommandUpdatePointTemperatureMaxTemperature() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_TARGET_TEMPERATURE);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("40"));

        // 0 (and everything below the min temperature 30.0 should be set to the min temperature 30.0)
        verify(bridgeHandlerMock).commandUpdatePointTemperature(device.getId(), 30.0);
    }

    @Test
    public void testHandleCommandCommandRollerShutterUp() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, UpDownType.UP);

        verify(bridgeHandlerMock).commandSetRollerShutterStop(device.getId(), ShutterActionType.UP);
    }

    @Test
    public void testHandleCommandCommandRollerShutterDown() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, UpDownType.DOWN);

        verify(bridgeHandlerMock).commandSetRollerShutterStop(device.getId(), ShutterActionType.DOWN);
    }

    @Test
    public void testHandleCommandCommandRollerShutterOn() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.ON);

        verify(bridgeHandlerMock).commandSetRollerShutterStop(device.getId(), ShutterActionType.DOWN);
    }

    @Test
    public void testHandleCommandCommandRollerShutterOff() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, OnOffType.OFF);

        verify(bridgeHandlerMock).commandSetRollerShutterStop(device.getId(), ShutterActionType.UP);
    }

    @Test
    public void testHandleCommandCommandRollerShutterStop() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, StopMoveType.STOP);

        verify(bridgeHandlerMock).commandSetRollerShutterStop(device.getId(), ShutterActionType.STOP);
    }

    @Test
    public void testHandleCommandCommandRollerShutterDecimalType() {
        DeviceDTO device = createDevice();

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);
        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("30"));

        verify(bridgeHandlerMock).commandSetRollerShutterLevel(device.getId(), 30);
    }

    @Test
    public void testHandleCommandCommandRollerShutterDecimalTypeInverted() {
        DeviceDTO device = createDevice();

        Channel rollerShutterChannelMock = createRollerShutterChannelMock(true);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        Thing thingMock = deviceHandler.getThing();
        when(thingMock.getChannel(CHANNEL_ROLLERSHUTTER)).thenReturn(rollerShutterChannelMock);

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("30"));

        verify(bridgeHandlerMock).commandSetRollerShutterLevel(device.getId(), 30);
    }

    @Test
    public void testHandleCommandCommandRollerShutterDecimalTypeNotInverted() {
        DeviceDTO device = createDevice();

        Channel rollerShutterChannelMock = createRollerShutterChannelMock(false);

        LivisiDeviceHandler deviceHandler = createDeviceHandler(device);

        Thing thingMock = deviceHandler.getThing();
        when(thingMock.getChannel(CHANNEL_ROLLERSHUTTER)).thenReturn(rollerShutterChannelMock);

        ChannelUID channelMock = createChannel(CHANNEL_ROLLERSHUTTER);

        deviceHandler.handleCommand(channelMock, DecimalType.valueOf("30"));

        verify(bridgeHandlerMock).commandSetRollerShutterLevel(device.getId(), 70);
    }

    private LivisiDeviceHandler createDeviceHandler(DeviceDTO device) {
        when(bridgeHandlerMock.getDeviceById(any())).thenReturn(Optional.of(device));

        ThingUID bridgeThingUID = new ThingUID(THING_TYPE_BRIDGE, "bridgeId");
        Bridge bridgeMock = mock(Bridge.class);
        when(bridgeMock.getHandler()).thenReturn(bridgeHandlerMock);
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridgeMock.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        ThingTypeUID thingTypeUID = THING_TYPE_RST2;
        ThingUID thingUID = new ThingUID(thingTypeUID, device.getId());

        Configuration thingConfiguration = new Configuration();
        thingConfiguration.setProperties(Map.of(PROPERTY_ID, device.getId()));

        Thing thingMock = mock(Thing.class);
        when(thingMock.getBridgeUID()).thenReturn(bridgeThingUID);
        when(thingMock.getConfiguration()).thenReturn(thingConfiguration);
        when(thingMock.getUID()).thenReturn(thingUID);
        when(thingMock.getThingTypeUID()).thenReturn(thingTypeUID);
        doAnswer(invocation -> {
            thingStatusInfo = invocation.getArgument(0, ThingStatusInfo.class);
            return null;
        }).when(thingMock).setStatusInfo(any());
        when(thingMock.getStatusInfo()).thenAnswer(invocation -> thingStatusInfo);
        when(thingMock.getStatus()).thenAnswer(invocation -> thingStatusInfo.getStatus());

        LivisiDeviceHandler deviceHandler = new LivisiDeviceHandler(thingMock);
        ThingHandlerCallback callbackMock = createCallbackMock(bridgeMock);
        deviceHandler.setCallback(callbackMock);

        deviceHandler.initialize();

        return deviceHandler;
    }

    private static DeviceDTO createDevice() {
        DeviceDTO device = new DeviceDTO();
        device.setId("id");
        device.setConfig(new DeviceConfigDTO());
        device.setCapabilityMap(new HashMap<>());
        return device;
    }

    private static ChannelUID createChannel(String channelId) {
        ChannelUID channelMock = mock(ChannelUID.class);
        when(channelMock.getId()).thenReturn(channelId);
        return channelMock;
    }

    private static void addCapabilityToDevice(String capabilityType,
            @Nullable Consumer<CapabilityStateDTO> capabilityStateConsumer, DeviceDTO device) {
        CapabilityConfigDTO capabilityConfig = new CapabilityConfigDTO();
        capabilityConfig.setName("capabilityName");

        CapabilityStateDTO capabilityState = new CapabilityStateDTO();
        if (capabilityStateConsumer != null) {
            capabilityStateConsumer.accept(capabilityState);
        }

        final String capabilityId = "capabilityId";
        CapabilityDTO capability = new CapabilityDTO();
        capability.setId(capabilityId);
        capability.setType(capabilityType);
        capability.setConfig(capabilityConfig);
        capability.setCapabilityState(capabilityState);
        device.getCapabilityMap().put(capabilityId, capability);
    }

    private static EventDTO createCapabilityEvent(Consumer<EventPropertiesDTO> eventPropertiesConsumer) {
        EventPropertiesDTO eventProperties = new EventPropertiesDTO();
        eventPropertiesConsumer.accept(eventProperties);

        EventDTO event = new EventDTO();
        event.setSource(LINK_TYPE_CAPABILITY + "capabilityId");
        event.setProperties(eventProperties);
        return event;
    }

    private ThingHandlerCallback createCallbackMock(Bridge bridge) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(any())).thenReturn(bridge);
        when(callback.createChannelBuilders(any(), any())).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0, ChannelUID.class);
            State state = invocation.getArgument(1, State.class);
            updatedChannels.put(channelUID.getId(), state);
            return null;
        }).when(callback).stateUpdated(any(), any());

        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0, Thing.class);
            ThingStatusInfo thingStatusInfo = invocation.getArgument(1, ThingStatusInfo.class);
            thing.setStatusInfo(thingStatusInfo);
            return null;
        }).when(callback).statusUpdated(any(), any());

        doAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(1, ChannelUID.class);
            String value = invocation.getArgument(2, String.class);
            triggeredChannels.add(new TriggeredEvent(channelUID.getId(), value));
            return null;
        }).when(callback).channelTriggered(any(), any(), any());

        doAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0, ChannelUID.class);
            return ChannelBuilder.create(channelUID);
        }).when(callback).createChannelBuilder(any(), any());

        doAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(1, ChannelUID.class);
            return ChannelBuilder.create(channelUID);
        }).when(callback).editChannel(any(), any());

        return callback;
    }

    private boolean isChannelUpdated(String channelUID) {
        return updatedChannels.containsKey(channelUID);
    }

    private boolean isChannelUpdated(String channelUID, State expectedState) {
        State state = updatedChannels.get(channelUID);
        return expectedState.equals(state);
    }

    private boolean isChannelTriggered(String channelUID, String expectedTriggerValue) {
        return triggeredChannels.contains(new TriggeredEvent(channelUID, expectedTriggerValue));
    }

    private static Channel createRollerShutterChannelMock(boolean isInvert) {
        Map<String, Object> rollerShutterChannelProperties = new HashMap<>();
        rollerShutterChannelProperties.put(INVERT_CHANNEL_PARAMETER, isInvert);
        Configuration rollerShutterChannelConfiguration = new Configuration(rollerShutterChannelProperties);

        Channel rollerShutterChannelMock = mock(Channel.class);
        when(rollerShutterChannelMock.getConfiguration()).thenReturn(rollerShutterChannelConfiguration);
        return rollerShutterChannelMock;
    }

    private static class TriggeredEvent {

        private final String channelUID;
        private final String triggerValue;

        public TriggeredEvent(String channelUID, String triggerValue) {
            this.channelUID = channelUID;
            this.triggerValue = triggerValue;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TriggeredEvent that = (TriggeredEvent) o;
            return channelUID.equals(that.channelUID) && triggerValue.equals(that.triggerValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelUID, triggerValue);
        }
    }
}
