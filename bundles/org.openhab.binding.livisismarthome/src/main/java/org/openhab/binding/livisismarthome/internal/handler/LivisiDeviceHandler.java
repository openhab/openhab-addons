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

import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionType;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventPropertiesDTO;
import org.openhab.binding.livisismarthome.internal.listener.DeviceStatusListener;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LivisiDeviceHandler} is responsible for handling the {@link DeviceDTO}s and their commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
public class LivisiDeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    private static final int MIN_TEMPERATURE_CELSIUS = 6;
    private static final int MAX_TEMPERATURE_CELSIUS = 30;
    private static final String LONG_PRESS = "LongPress";
    private static final String SHORT_PRESS = "ShortPress";

    private final Logger logger = LoggerFactory.getLogger(LivisiDeviceHandler.class);
    private final Object lock = new Object();

    private String deviceId = "";
    private @Nullable LivisiBridgeHandler bridgeHandler;

    /**
     * Constructs a new {@link LivisiDeviceHandler} for the given {@link Thing}.
     *
     * @param thing device thing
     */
    public LivisiDeviceHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.debug("handleCommand called for channel '{}' of type '{}' with command '{}'", channelUID,
                getThing().getThingTypeUID().getId(), command);

        if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
            final Optional<LivisiBridgeHandler> bridgeHandlerOptional = getBridgeHandler();
            if (bridgeHandlerOptional.isPresent()) {
                LivisiBridgeHandler bridgeHandler = bridgeHandlerOptional.get();
                if (command instanceof RefreshType) {
                    final Optional<DeviceDTO> device = bridgeHandler.getDeviceById(deviceId);
                    device.ifPresent(this::onDeviceStateChanged);
                } else {
                    executeCommand(channelUID, command, bridgeHandler);
                }
            } else {
                logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            }
        } else {
            logger.debug("Cannot handle command - thing is not online. Command ignored.");
        }
    }

    private void executeCommand(ChannelUID channelUID, Command command, LivisiBridgeHandler bridgeHandler) {
        if (CHANNEL_SWITCH.equals(channelUID.getId())) {
            commandSwitchDevice(command, bridgeHandler);
        } else if (CHANNEL_DIMMER.equals(channelUID.getId())) {
            commandSetDimLevel(command, bridgeHandler);
        } else if (CHANNEL_ROLLERSHUTTER.equals(channelUID.getId())) {
            commandRollerShutter(command, bridgeHandler);
        } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
            commandUpdatePointTemperature(command, bridgeHandler);
        } else if (CHANNEL_OPERATION_MODE.equals(channelUID.getId())) {
            commandSetOperationMode(command, bridgeHandler);
        } else if (CHANNEL_ALARM.equals(channelUID.getId())) {
            commandSwitchAlarm(command, bridgeHandler);
        } else {
            logger.debug("UNSUPPORTED channel {} for device {}.", channelUID.getId(), deviceId);
        }
    }

    private void commandSwitchDevice(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof OnOffType) {
            bridgeHandler.commandSwitchDevice(deviceId, OnOffType.ON.equals(command));
        }
    }

    private void commandSetDimLevel(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof DecimalType dimLevel) {
            bridgeHandler.commandSetDimLevel(deviceId, dimLevel.intValue());
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                bridgeHandler.commandSetDimLevel(deviceId, 100);
            } else {
                bridgeHandler.commandSetDimLevel(deviceId, 0);
            }
        }
    }

    private void commandRollerShutter(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof DecimalType rollerShutterLevel) {
            bridgeHandler.commandSetRollerShutterLevel(deviceId,
                    invertRollerShutterValueIfConfigured(rollerShutterLevel.intValue()));
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                bridgeHandler.commandSetRollerShutterStop(deviceId, ShutterActionType.DOWN);
            } else {
                bridgeHandler.commandSetRollerShutterStop(deviceId, ShutterActionType.UP);
            }
        } else if (command instanceof UpDownType) {
            if (UpDownType.DOWN.equals(command)) {
                bridgeHandler.commandSetRollerShutterStop(deviceId, ShutterActionType.DOWN);
            } else {
                bridgeHandler.commandSetRollerShutterStop(deviceId, ShutterActionType.UP);
            }
        } else if (command instanceof StopMoveType) {
            if (StopMoveType.STOP.equals(command)) {
                bridgeHandler.commandSetRollerShutterStop(deviceId, ShutterActionType.STOP);
            }
        }
    }

    private void commandUpdatePointTemperature(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof QuantityType temperatureCommand) {
            final QuantityType<?> pointTemperatureCommand = temperatureCommand.toUnit(SIUnits.CELSIUS);
            if (pointTemperatureCommand != null) {
                commandUpdatePointTemperature(pointTemperatureCommand.doubleValue(), bridgeHandler);
            }
        } else if (command instanceof DecimalType temperatureCommand) {
            commandUpdatePointTemperature(temperatureCommand.doubleValue(), bridgeHandler);
        }
    }

    private void commandUpdatePointTemperature(double pointTemperature, LivisiBridgeHandler bridgeHandler) {
        if (pointTemperature < MIN_TEMPERATURE_CELSIUS) {
            pointTemperature = MIN_TEMPERATURE_CELSIUS;
            logger.debug(
                    "pointTemperature set to value {} (instead of value '{}'), because it is the minimal possible value!",
                    MIN_TEMPERATURE_CELSIUS, pointTemperature);
        } else if (pointTemperature > MAX_TEMPERATURE_CELSIUS) {
            pointTemperature = MAX_TEMPERATURE_CELSIUS;
            logger.debug(
                    "pointTemperature set to value {} (instead of value '{}'), because it is the maximal possible value!",
                    MAX_TEMPERATURE_CELSIUS, pointTemperature);
        }
        bridgeHandler.commandUpdatePointTemperature(deviceId, pointTemperature);
    }

    private void commandSetOperationMode(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof StringType) {
            final String autoModeCommand = command.toString();

            if (CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_AUTO.equals(autoModeCommand)) {
                bridgeHandler.commandSetOperationMode(deviceId, true);
            } else if (CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_MANUAL.equals(autoModeCommand)) {
                bridgeHandler.commandSetOperationMode(deviceId, false);
            } else {
                logger.warn("Could not set operationMode. Invalid value '{}'! Only '{}' or '{}' allowed.",
                        autoModeCommand, CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_AUTO,
                        CapabilityStateDTO.STATE_VALUE_OPERATION_MODE_MANUAL);
            }
        }
    }

    private void commandSwitchAlarm(Command command, LivisiBridgeHandler bridgeHandler) {
        if (command instanceof OnOffType) {
            bridgeHandler.commandSwitchAlarm(deviceId, OnOffType.ON.equals(command));
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing LIVISI SmartHome device handler.");
        initializeThing(isBridgeOnline());
    }

    @Override
    public void dispose() {
        unregisterListeners(bridgeHandler, deviceId);
    }

    private static void unregisterListeners(@Nullable LivisiBridgeHandler bridgeHandler, String deviceId) {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(deviceId);
        }
    }

    @Override
    public void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(ThingStatus.ONLINE == bridgeStatusInfo.getStatus());
    }

    /**
     * Initializes the {@link Thing} corresponding to the given status of the bridge.
     * 
     * @param isBridgeOnline true if the bridge thing is online, otherwise false
     */
    private void initializeThing(final boolean isBridgeOnline) {
        logger.debug("initializeThing thing {} bridge online status: {}", getThing().getUID(), isBridgeOnline);
        final String configDeviceId = (String) getConfig().get(PROPERTY_ID);
        if (configDeviceId != null) {
            deviceId = configDeviceId;

            Optional<LivisiBridgeHandler> bridgeHandler = registerAtBridgeHandler();
            if (bridgeHandler.isPresent()) {
                if (isBridgeOnline) {
                    initializeProperties();

                    Optional<DeviceDTO> deviceOptional = getDevice();
                    if (deviceOptional.isPresent()) {
                        DeviceDTO device = deviceOptional.get();
                        if (device.isReachable() != null && !device.isReachable()) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/error.notReachable");
                        } else {
                            updateStatus(ThingStatus.ONLINE);
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/error.deviceNotFound");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.bridgeHandlerMissing");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.deviceIdUnknown");
        }
    }

    /**
     * Initializes all properties of the {@link DeviceDTO}, like vendor, serialnumber etc.
     */
    private void initializeProperties() {
        synchronized (this.lock) {
            final Optional<DeviceDTO> deviceOptional = getDevice();
            if (deviceOptional.isPresent()) {
                DeviceDTO device = deviceOptional.get();

                final Map<String, String> properties = editProperties();
                properties.put(PROPERTY_ID, device.getId());
                properties.put(PROPERTY_PROTOCOL_ID, device.getConfig().getProtocolId());
                if (device.hasSerialNumber()) {
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
                }
                properties.put(Thing.PROPERTY_VENDOR, device.getManufacturer());
                properties.put(PROPERTY_VERSION, device.getVersion());
                if (device.hasLocation()) {
                    properties.put(PROPERTY_LOCATION, device.getLocation().getName());
                }
                if (device.isBatteryPowered()) {
                    properties.put(PROPERTY_BATTERY_POWERED, "yes");
                } else {
                    properties.put(PROPERTY_BATTERY_POWERED, "no");
                }
                if (device.isController()) {
                    properties.put(PROPERTY_DEVICE_TYPE, "Controller");
                } else if (device.isVirtualDevice()) {
                    properties.put(PROPERTY_DEVICE_TYPE, "Virtual");
                } else if (device.isRadioDevice()) {
                    properties.put(PROPERTY_DEVICE_TYPE, "Radio");
                }

                // Thermostat
                if (DEVICE_RST.equals(device.getType()) || DEVICE_RST2.equals(device.getType())
                        || DEVICE_WRT.equals(device.getType())) {
                    properties.put(PROPERTY_DISPLAY_CURRENT_TEMPERATURE,
                            device.getConfig().getDisplayCurrentTemperature());
                }

                // Meter
                if (DEVICE_ANALOG_METER.equals(device.getType()) || DEVICE_GENERATION_METER.equals(device.getType())
                        || DEVICE_SMART_METER.equals(device.getType())
                        || DEVICE_TWO_WAY_METER.equals(device.getType())) {
                    properties.put(PROPERTY_METER_ID, device.getConfig().getMeterId());
                    properties.put(PROPERTY_METER_FIRMWARE_VERSION, device.getConfig().getMeterFirmwareVersion());
                }

                if (device.getConfig().getTimeOfAcceptance() != null) {
                    properties.put(PROPERTY_TIME_OF_ACCEPTANCE, device.getConfig().getTimeOfAcceptance()
                            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }
                if (device.getConfig().getTimeOfDiscovery() != null) {
                    properties.put(PROPERTY_TIME_OF_DISCOVERY, device.getConfig().getTimeOfDiscovery()
                            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }

                updateProperties(properties);

                onDeviceStateChanged(device);
            } else {
                logger.debug("initializeProperties: The device with id {} isn't found", deviceId);
            }
        }
    }

    @Override
    public void onDeviceStateChanged(final DeviceDTO device) {
        synchronized (this.lock) {
            updateChannels(device, false);
        }
    }

    @Override
    public void onDeviceStateChanged(final DeviceDTO device, final EventDTO event) {
        synchronized (this.lock) {
            if (event.isLinkedtoCapability()) {
                final String linkedCapabilityId = event.getSourceId();

                CapabilityDTO capability = device.getCapabilityMap().get(linkedCapabilityId);
                if (capability != null) {
                    logger.trace("Loaded Capability {}, {} with id {}, device {} from device id {}",
                            capability.getType(), capability.getName(), capability.getId(), capability.getDeviceLink(),
                            device.getId());

                    if (capability.hasState()) {
                        boolean deviceChanged = updateDevice(event, capability);
                        if (deviceChanged) {
                            updateChannels(device, true);
                        }
                    } else {
                        logger.debug("Capability {} has no state (yet?) - refreshing device.", capability.getName());

                        Optional<DeviceDTO> deviceOptional = refreshDevice(linkedCapabilityId);
                        deviceOptional.ifPresent((d) -> updateChannels(d, true));
                    }
                }
            } else if (event.isLinkedtoDevice()) {
                if (device.hasDeviceState()) {
                    updateChannels(device, true);
                } else {
                    logger.debug("Device {}/{} has no state.", device.getConfig().getName(), device.getId());
                }
            }
        }
    }

    private Optional<DeviceDTO> refreshDevice(String linkedCapabilityId) {
        Optional<LivisiBridgeHandler> bridgeHandler = registerAtBridgeHandler();
        Optional<DeviceDTO> deviceOptional = bridgeHandler.flatMap(bh -> bh.refreshDevice(deviceId));
        if (deviceOptional.isPresent()) {
            DeviceDTO device = deviceOptional.get();
            CapabilityDTO capability = device.getCapabilityMap().get(linkedCapabilityId);
            if (capability != null && capability.hasState()) {
                return Optional.of(device);
            }
        }
        return Optional.empty();
    }

    private boolean updateDevice(EventDTO event, CapabilityDTO capability) {
        CapabilityStateDTO capabilityState = capability.getCapabilityState();

        // VariableActuator
        if (capability.isTypeVariableActuator()) {
            capabilityState.setVariableActuatorState(event.getProperties().getValue());

            // SwitchActuator
        } else if (capability.isTypeSwitchActuator()) {
            capabilityState.setSwitchActuatorState(event.getProperties().getOnState());

            // DimmerActuator
        } else if (capability.isTypeDimmerActuator()) {
            capabilityState.setDimmerActuatorState(event.getProperties().getDimLevel());

            // RollerShutterActuator
        } else if (capability.isTypeRollerShutterActuator()) {
            capabilityState.setRollerShutterActuatorState(event.getProperties().getShutterLevel());

            // TemperatureSensor
        } else if (capability.isTypeTemperatureSensor()) {
            // when values are changed, they come with separate events
            // values should only updated when they are not null
            final Double temperature = event.getProperties().getTemperature();
            final Boolean frostWarning = event.getProperties().getFrostWarning();
            if (temperature != null) {
                capabilityState.setTemperatureSensorTemperatureState(temperature);
            }
            if (frostWarning != null) {
                capabilityState.setTemperatureSensorFrostWarningState(frostWarning);
            }

            // ThermostatActuator
        } else if (capability.isTypeThermostatActuator()) {
            // when values are changed, they come with separate events
            // values should only updated when they are not null

            final Double pointTemperature = event.getProperties().getPointTemperature();
            final String operationMode = event.getProperties().getOperationMode();
            final Boolean windowReductionActive = event.getProperties().getWindowReductionActive();

            if (pointTemperature != null) {
                capabilityState.setThermostatActuatorPointTemperatureState(pointTemperature);
            }
            if (operationMode != null) {
                capabilityState.setThermostatActuatorOperationModeState(operationMode);
            }
            if (windowReductionActive != null) {
                capabilityState.setThermostatActuatorWindowReductionActiveState(windowReductionActive);
            }

            // HumiditySensor
        } else if (capability.isTypeHumiditySensor()) {
            // when values are changed, they come with separate events
            // values should only updated when they are not null
            final Double humidity = event.getProperties().getHumidity();
            final Boolean moldWarning = event.getProperties().getMoldWarning();
            if (humidity != null) {
                capabilityState.setHumiditySensorHumidityState(humidity);
            }
            if (moldWarning != null) {
                capabilityState.setHumiditySensorMoldWarningState(moldWarning);
            }

            // WindowDoorSensor
        } else if (capability.isTypeWindowDoorSensor()) {
            capabilityState.setWindowDoorSensorState(event.getProperties().getIsOpen());

            // SmokeDetectorSensor
        } else if (capability.isTypeSmokeDetectorSensor()) {
            capabilityState.setSmokeDetectorSensorState(event.getProperties().getIsSmokeAlarm());

            // AlarmActuator
        } else if (capability.isTypeAlarmActuator()) {
            capabilityState.setAlarmActuatorState(event.getProperties().getOnState());

            // MotionDetectionSensor
        } else if (capability.isTypeMotionDetectionSensor()) {
            capabilityState.setMotionDetectionSensorState(event.getProperties().getMotionDetectedCount());

            // LuminanceSensor
        } else if (capability.isTypeLuminanceSensor()) {
            capabilityState.setLuminanceSensorState(event.getProperties().getLuminance());

            // PushButtonSensor
        } else if (capability.isTypePushButtonSensor()) {
            if (event.isButtonPressedEvent()) {
                // Some devices send both StateChanged and ButtonPressed. But only the ButtonPressed should be handled,
                // therefore it is checked for button pressed event (button index is set).
                EventPropertiesDTO properties = event.getProperties();
                capabilityState.setPushButtonSensorButtonIndexState(properties.getKeyPressButtonIndex());
                capabilityState.setPushButtonSensorButtonIndexType(properties.getKeyPressType());
                capabilityState.setPushButtonSensorCounterState(properties.getKeyPressCounter());
            }

            // EnergyConsumptionSensor
        } else if (capability.isTypeEnergyConsumptionSensor()) {
            capabilityState.setEnergyConsumptionSensorEnergyConsumptionMonthKWhState(
                    event.getProperties().getEnergyConsumptionMonthKWh());
            capabilityState.setEnergyConsumptionSensorAbsoluteEnergyConsumptionState(
                    event.getProperties().getAbsoluteEnergyConsumption());
            capabilityState.setEnergyConsumptionSensorEnergyConsumptionMonthEuroState(
                    event.getProperties().getEnergyConsumptionMonthEuro());
            capabilityState.setEnergyConsumptionSensorEnergyConsumptionDayEuroState(
                    event.getProperties().getEnergyConsumptionDayEuro());
            capabilityState.setEnergyConsumptionSensorEnergyConsumptionDayKWhState(
                    event.getProperties().getEnergyConsumptionDayKWh());

            // PowerConsumptionSensor
        } else if (capability.isTypePowerConsumptionSensor()) {
            capabilityState.setPowerConsumptionSensorPowerConsumptionWattState(
                    event.getProperties().getPowerConsumptionWatt());

            // GenerationMeterEnergySensor
        } else if (capability.isTypeGenerationMeterEnergySensor()) {
            capabilityState.setGenerationMeterEnergySensorEnergyPerMonthInKWhState(
                    event.getProperties().getEnergyPerMonthInKWh());
            capabilityState.setGenerationMeterEnergySensorTotalEnergyState(event.getProperties().getTotalEnergy());
            capabilityState.setGenerationMeterEnergySensorEnergyPerMonthInEuroState(
                    event.getProperties().getEnergyPerMonthInEuro());
            capabilityState.setGenerationMeterEnergySensorEnergyPerDayInEuroState(
                    event.getProperties().getEnergyPerDayInEuro());
            capabilityState
                    .setGenerationMeterEnergySensorEnergyPerDayInKWhState(event.getProperties().getEnergyPerDayInKWh());

            // GenerationMeterPowerConsumptionSensor
        } else if (capability.isTypeGenerationMeterPowerConsumptionSensor()) {
            capabilityState
                    .setGenerationMeterPowerConsumptionSensorPowerInWattState(event.getProperties().getPowerInWatt());

            // TwoWayMeterEnergyConsumptionSensor
        } else if (capability.isTypeTwoWayMeterEnergyConsumptionSensor()) {
            capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(
                    event.getProperties().getEnergyPerMonthInKWh());
            capabilityState
                    .setTwoWayMeterEnergyConsumptionSensorTotalEnergyState(event.getProperties().getTotalEnergy());
            capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(
                    event.getProperties().getEnergyPerMonthInEuro());
            capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(
                    event.getProperties().getEnergyPerDayInEuro());
            capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(
                    event.getProperties().getEnergyPerDayInKWh());

            // TwoWayMeterEnergyFeedSensor
        } else if (capability.isTypeTwoWayMeterEnergyFeedSensor()) {
            capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(
                    event.getProperties().getEnergyPerMonthInKWh());
            capabilityState.setTwoWayMeterEnergyFeedSensorTotalEnergyState(event.getProperties().getTotalEnergy());
            capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(
                    event.getProperties().getEnergyPerMonthInEuro());
            capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(
                    event.getProperties().getEnergyPerDayInEuro());
            capabilityState
                    .setTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(event.getProperties().getEnergyPerDayInKWh());

            // TwoWayMeterPowerConsumptionSensor
        } else if (capability.isTypeTwoWayMeterPowerConsumptionSensor()) {
            capabilityState
                    .setTwoWayMeterPowerConsumptionSensorPowerInWattState(event.getProperties().getPowerInWatt());

        } else {
            logger.debug("Unsupported capability type {}.", capability.getType());
            return false;
        }
        return true;
    }

    private void updateChannels(DeviceDTO device, boolean isChangedByEvent) {
        // DEVICE STATES
        final boolean isReachable = updateStatus(device);

        if (isReachable) {
            updateDeviceChannels(device);

            // CAPABILITY STATES
            for (final Entry<String, CapabilityDTO> entry : device.getCapabilityMap().entrySet()) {
                final CapabilityDTO capability = entry.getValue();

                logger.debug("->capability:{} ({}/{})", capability.getId(), capability.getType(), capability.getName());

                if (capability.hasState()) {
                    updateCapabilityChannels(device, capability, isChangedByEvent);
                } else {
                    logger.debug("Capability not available for device {} ({})", device.getConfig().getName(),
                            device.getType());
                }
            }
        }
    }

    private boolean updateStatus(DeviceDTO device) {
        Boolean reachable = device.isReachable();
        if (reachable != null) {
            if (reachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.notReachable");
                return false;
            }
        }
        return true;
    }

    private void updateDeviceChannels(DeviceDTO device) {
        if (device.isBatteryPowered()) {
            updateState(CHANNEL_BATTERY_LOW, OnOffType.from(device.hasLowBattery()));
        }
    }

    private void updateCapabilityChannels(DeviceDTO device, CapabilityDTO capability, boolean isChangedByEvent) {
        switch (capability.getType()) {
            case CapabilityDTO.TYPE_VARIABLEACTUATOR:
                updateVariableActuatorChannels(capability);
                break;
            case CapabilityDTO.TYPE_SWITCHACTUATOR:
                updateSwitchActuatorChannels(capability);
                break;
            case CapabilityDTO.TYPE_DIMMERACTUATOR:
                updateDimmerActuatorChannels(capability);
                break;
            case CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR:
                updateRollerShutterActuatorChannels(capability);
                break;
            case CapabilityDTO.TYPE_TEMPERATURESENSOR:
                updateTemperatureSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_THERMOSTATACTUATOR:
                updateThermostatActuatorChannels(device, capability);
                break;
            case CapabilityDTO.TYPE_HUMIDITYSENSOR:
                updateHumiditySensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_WINDOWDOORSENSOR:
                updateWindowDoorSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_SMOKEDETECTORSENSOR:
                updateSmokeDetectorChannels(capability);
                break;
            case CapabilityDTO.TYPE_ALARMACTUATOR:
                updateAlarmActuatorChannels(capability);
                break;
            case CapabilityDTO.TYPE_MOTIONDETECTIONSENSOR:
                updateMotionDetectionSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_LUMINANCESENSOR:
                updateLuminanceSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_PUSHBUTTONSENSOR:
                updatePushButtonSensorChannels(capability, isChangedByEvent);
                break;
            case CapabilityDTO.TYPE_ENERGYCONSUMPTIONSENSOR:
                updateEnergyConsumptionSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_POWERCONSUMPTIONSENSOR:
                updateStateForEnergyChannelWatt(CHANNEL_POWER_CONSUMPTION_WATT,
                        capability.getCapabilityState().getPowerConsumptionSensorPowerConsumptionWattState(),
                        capability);
                break;
            case CapabilityDTO.TYPE_GENERATIONMETERENERGYSENSOR:
                updateGenerationMeterEnergySensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR:
                updateStateForEnergyChannelWatt(CHANNEL_POWER_GENERATION_WATT,
                        capability.getCapabilityState().getGenerationMeterPowerConsumptionSensorPowerInWattState(),
                        capability);
                break;
            case CapabilityDTO.TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR:
                updateTwoWayMeterEnergyConsumptionSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_TWOWAYMETERENERGYFEEDSENSOR:
                updateTwoWayMeterEnergyFeedSensorChannels(capability);
                break;
            case CapabilityDTO.TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR:
                updateStateForEnergyChannelWatt(CHANNEL_POWER_WATT,
                        capability.getCapabilityState().getTwoWayMeterPowerConsumptionSensorPowerInWattState(),
                        capability);
                break;
            default:
                logger.debug("Unsupported capability type {}.", capability.getType());
                break;
        }
    }

    private void updateVariableActuatorChannels(CapabilityDTO capability) {
        final Boolean variableActuatorState = capability.getCapabilityState().getVariableActuatorState();
        if (variableActuatorState != null) {
            updateState(CHANNEL_SWITCH, OnOffType.from(variableActuatorState));
        } else {
            logStateNull(capability);
        }
    }

    private void updateSwitchActuatorChannels(CapabilityDTO capability) {
        final Boolean switchActuatorState = capability.getCapabilityState().getSwitchActuatorState();
        if (switchActuatorState != null) {
            updateState(CHANNEL_SWITCH, OnOffType.from(switchActuatorState));
        } else {
            logStateNull(capability);
        }
    }

    private void updateDimmerActuatorChannels(CapabilityDTO capability) {
        final Integer dimLevel = capability.getCapabilityState().getDimmerActuatorState();
        if (dimLevel != null) {
            logger.debug("Dimlevel state {}", dimLevel);
            updateState(CHANNEL_DIMMER, new PercentType(dimLevel));
        } else {
            logStateNull(capability);
        }
    }

    private void updateRollerShutterActuatorChannels(CapabilityDTO capability) {
        Integer rollerShutterLevel = capability.getCapabilityState().getRollerShutterActuatorState();
        if (rollerShutterLevel != null) {
            rollerShutterLevel = invertRollerShutterValueIfConfigured(rollerShutterLevel);
            logger.debug("RollerShutterlevel state {}", rollerShutterLevel);
            updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rollerShutterLevel));
        } else {
            logStateNull(capability);
        }
    }

    private void updateTemperatureSensorChannels(CapabilityDTO capability) {
        // temperature
        final Double temperature = capability.getCapabilityState().getTemperatureSensorTemperatureState();
        if (temperature != null) {
            logger.debug("-> Temperature sensor state: {}", temperature);
            updateState(CHANNEL_CURRENT_TEMPERATURE, QuantityType.valueOf(temperature, SIUnits.CELSIUS));
        } else {
            logStateNull(capability);
        }

        // frost warning
        final Boolean frostWarning = capability.getCapabilityState().getTemperatureSensorFrostWarningState();
        if (frostWarning != null) {
            updateState(CHANNEL_FROST_WARNING, OnOffType.from(frostWarning));
        } else {
            logStateNull(capability);
        }
    }

    private void updateThermostatActuatorChannels(DeviceDTO device, CapabilityDTO capability) {
        // point temperature
        final Double pointTemperature = capability.getCapabilityState().getThermostatActuatorPointTemperatureState();
        if (pointTemperature != null) {
            logger.debug("Update CHANNEL_SET_TEMPERATURE: state:{} (DeviceName {}, Capab-ID:{})", pointTemperature,
                    device.getConfig().getName(), capability.getId());
            updateState(CHANNEL_TARGET_TEMPERATURE, QuantityType.valueOf(pointTemperature, SIUnits.CELSIUS));
        } else {
            logStateNull(capability);
        }

        // operation mode
        final String operationMode = capability.getCapabilityState().getThermostatActuatorOperationModeState();
        if (operationMode != null) {
            updateState(CHANNEL_OPERATION_MODE, new StringType(operationMode));
        } else {
            logStateNull(capability);
        }

        // window reduction active
        final Boolean windowReductionActive = capability.getCapabilityState()
                .getThermostatActuatorWindowReductionActiveState();
        if (windowReductionActive != null) {
            updateState(CHANNEL_WINDOW_REDUCTION_ACTIVE, OnOffType.from(windowReductionActive));
        } else {
            logStateNull(capability);
        }
    }

    private void updateHumiditySensorChannels(CapabilityDTO capability) {
        // humidity
        final Double humidity = capability.getCapabilityState().getHumiditySensorHumidityState();
        if (humidity != null) {
            updateState(CHANNEL_HUMIDITY, QuantityType.valueOf(humidity, Units.PERCENT));
        } else {
            logStateNull(capability);
        }

        // mold warning
        final Boolean moldWarning = capability.getCapabilityState().getHumiditySensorMoldWarningState();
        if (moldWarning != null) {
            updateState(CHANNEL_MOLD_WARNING, OnOffType.from(moldWarning));
        } else {
            logStateNull(capability);
        }
    }

    private void updateWindowDoorSensorChannels(CapabilityDTO capability) {
        final Boolean contactState = capability.getCapabilityState().getWindowDoorSensorState();
        if (contactState != null) {
            updateState(CHANNEL_CONTACT, toOpenClosedType(contactState));
        } else {
            logStateNull(capability);
        }
    }

    private void updateSmokeDetectorChannels(CapabilityDTO capability) {
        final Boolean smokeState = capability.getCapabilityState().getSmokeDetectorSensorState();
        if (smokeState != null) {
            updateState(CHANNEL_SMOKE, OnOffType.from(smokeState));
        } else {
            logStateNull(capability);
        }
    }

    private void updateAlarmActuatorChannels(CapabilityDTO capability) {
        final Boolean alarmState = capability.getCapabilityState().getAlarmActuatorState();
        if (alarmState != null) {
            updateState(CHANNEL_ALARM, OnOffType.from(alarmState));
        } else {
            logStateNull(capability);
        }
    }

    private void updateMotionDetectionSensorChannels(CapabilityDTO capability) {
        final Integer motionCount = capability.getCapabilityState().getMotionDetectionSensorState();
        if (motionCount != null) {
            logger.debug("Motion state {} -> count {}", motionCount, motionCount);
            updateState(CHANNEL_MOTION_COUNT, new DecimalType(motionCount));
        } else {
            logStateNull(capability);
        }
    }

    private void updateLuminanceSensorChannels(CapabilityDTO capability) {
        final Double luminance = capability.getCapabilityState().getLuminanceSensorState();
        if (luminance != null) {
            updateState(CHANNEL_LUMINANCE, QuantityType.valueOf(luminance, Units.PERCENT));
        } else {
            logStateNull(capability);
        }
    }

    private void updatePushButtonSensorChannels(CapabilityDTO capability, boolean isChangedByEvent) {
        final Integer pushCount = capability.getCapabilityState().getPushButtonSensorCounterState();
        final Integer buttonIndex = capability.getCapabilityState().getPushButtonSensorButtonIndexState();
        final String type = capability.getCapabilityState().getPushButtonSensorButtonIndexType();
        logger.debug("Pushbutton index {}, count {}, type {}", buttonIndex, pushCount, type);
        if (buttonIndex != null && pushCount != null) {
            if (buttonIndex >= 0 && buttonIndex <= 7) {
                final int channelIndex = buttonIndex + 1;
                updateState(String.format(CHANNEL_BUTTON_COUNT, channelIndex), new DecimalType(pushCount));

                if (isChangedByEvent) {
                    triggerButtonChannels(type, channelIndex);
                }

                // Button handled so remove state to avoid re-trigger.
                capability.getCapabilityState().setPushButtonSensorButtonIndexState(null);
                capability.getCapabilityState().setPushButtonSensorButtonIndexType(null);
            }
        } else {
            logStateNull(capability);
        }
    }

    private void triggerButtonChannels(@Nullable String type, int channelIndex) {
        if (type != null) {
            if (SHORT_PRESS.equals(type)) {
                triggerChannel(CHANNEL_BUTTON + channelIndex, CommonTriggerEvents.SHORT_PRESSED);
            } else if (LONG_PRESS.equals(type)) {
                triggerChannel(CHANNEL_BUTTON + channelIndex, CommonTriggerEvents.LONG_PRESSED);
            }
        }
        triggerChannel(CHANNEL_BUTTON + channelIndex, CommonTriggerEvents.PRESSED);
    }

    private void updateEnergyConsumptionSensorChannels(CapabilityDTO capability) {
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH,
                capability.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionMonthKWhState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ABOLUTE_ENERGY_CONSUMPTION,
                capability.getCapabilityState().getEnergyConsumptionSensorAbsoluteEnergyConsumptionState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO,
                capability.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionMonthEuroState(),
                capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_CONSUMPTION_DAY_EURO,
                capability.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionDayEuroState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_CONSUMPTION_DAY_KWH,
                capability.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionDayKWhState(), capability);
    }

    private void updateGenerationMeterEnergySensorChannels(CapabilityDTO capability) {
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_GENERATION_MONTH_KWH,
                capability.getCapabilityState().getGenerationMeterEnergySensorEnergyPerMonthInKWhState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_TOTAL_ENERGY_GENERATION,
                capability.getCapabilityState().getGenerationMeterEnergySensorTotalEnergyState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_GENERATION_MONTH_EURO,
                capability.getCapabilityState().getGenerationMeterEnergySensorEnergyPerMonthInEuroState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_GENERATION_DAY_EURO,
                capability.getCapabilityState().getGenerationMeterEnergySensorEnergyPerDayInEuroState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_GENERATION_DAY_KWH,
                capability.getCapabilityState().getGenerationMeterEnergySensorEnergyPerDayInKWhState(), capability);
    }

    private void updateTwoWayMeterEnergyConsumptionSensorChannels(CapabilityDTO capability) {
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_MONTH_KWH,
                capability.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(),
                capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_TOTAL_ENERGY,
                capability.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorTotalEnergyState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_MONTH_EURO,
                capability.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(),
                capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_DAY_EURO,
                capability.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(),
                capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_DAY_KWH,
                capability.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(),
                capability);
    }

    private void updateTwoWayMeterEnergyFeedSensorChannels(CapabilityDTO capability) {
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_FEED_MONTH_KWH,
                capability.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_TOTAL_ENERGY_FED,
                capability.getCapabilityState().getTwoWayMeterEnergyFeedSensorTotalEnergyState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_FEED_MONTH_EURO,
                capability.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(), capability);
        updateStateForEnergyChannelEuro(CHANNEL_ENERGY_FEED_DAY_EURO,
                capability.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(), capability);
        updateStateForEnergyChannelKiloWattHour(CHANNEL_ENERGY_FEED_DAY_KWH,
                capability.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(), capability);
    }

    private void updateStateForEnergyChannelEuro(final String channelId, @Nullable final Double state,
            final CapabilityDTO capability) {
        if (state != null) {
            updateState(channelId, new DecimalType(state));
        } else {
            logStateNull(capability);
        }
    }

    private void updateStateForEnergyChannelWatt(final String channelId, @Nullable final Double state,
            final CapabilityDTO capability) {
        if (state != null) {
            updateState(channelId, QuantityType.valueOf(state, Units.WATT));
        } else {
            logStateNull(capability);
        }
    }

    private void updateStateForEnergyChannelKiloWattHour(final String channelId, @Nullable final Double state,
            final CapabilityDTO capability) {
        if (state != null) {
            updateState(channelId, QuantityType.valueOf(state, Units.KILOWATT_HOUR));
        } else {
            logStateNull(capability);
        }
    }

    /**
     * Returns the inverted value. Currently only rollershutter channels are supported.
     *
     * @param value value to become inverted
     * @return the value or the inverted value
     */
    private int invertRollerShutterValueIfConfigured(final int value) {
        @Nullable
        final Channel channel = getThing().getChannel(CHANNEL_ROLLERSHUTTER);
        if (channel == null) {
            logger.debug("Channel {} was null! Value not inverted.", CHANNEL_ROLLERSHUTTER);
            return value;
        }
        final Boolean invert = (Boolean) channel.getConfiguration().get(INVERT_CHANNEL_PARAMETER);
        if (invert != null && invert) {
            return value;
        }
        return 100 - value;
    }

    /**
     * Returns the {@link DeviceDTO} associated with this {@link LivisiDeviceHandler} (referenced by the
     * {@link LivisiDeviceHandler#deviceId}).
     *
     * @return the {@link DeviceDTO} or null, if not found or no {@link LivisiBridgeHandler} is available
     */
    private Optional<DeviceDTO> getDevice() {
        return getBridgeHandler().flatMap(bridgeHandler -> bridgeHandler.getDeviceById(deviceId));
    }

    private Optional<LivisiBridgeHandler> registerAtBridgeHandler() {
        synchronized (this.lock) {
            if (this.bridgeHandler == null) {
                @Nullable
                final Bridge bridge = getBridge();
                if (bridge == null) {
                    return Optional.empty();
                }
                @Nullable
                final ThingHandler handler = bridge.getHandler();
                if (handler instanceof LivisiBridgeHandler bridgeHandler) {
                    bridgeHandler.registerDeviceStatusListener(deviceId, this);
                    this.bridgeHandler = bridgeHandler;
                } else {
                    return Optional.empty(); // also called when the handler is NULL
                }
            }
            return getBridgeHandler();
        }
    }

    /**
     * Returns the LIVISI bridge handler.
     *
     * @return the {@link LivisiBridgeHandler} or null
     */
    private Optional<LivisiBridgeHandler> getBridgeHandler() {
        return Optional.ofNullable(this.bridgeHandler);
    }

    private boolean isBridgeOnline() {
        @Nullable
        Bridge bridge = getBridge();
        if (bridge != null) {
            return ThingStatus.ONLINE == bridge.getStatus();
        }
        return false;
    }

    private void logStateNull(CapabilityDTO capability) {
        logger.debug("State for {} is STILL NULL!! cstate-id: {}, capability-id: {}", capability.getType(),
                capability.getCapabilityState().getId(), capability.getId());
    }

    private static OpenClosedType toOpenClosedType(boolean isOpen) {
        if (isOpen) {
            return OpenClosedType.OPEN;
        }
        return OpenClosedType.CLOSED;
    }
}
