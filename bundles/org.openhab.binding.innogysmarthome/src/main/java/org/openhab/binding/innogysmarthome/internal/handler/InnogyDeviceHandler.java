/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.handler;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.*;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.ShutterAction;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.CapabilityState;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;
import org.openhab.binding.innogysmarthome.internal.listener.DeviceStatusListener;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyDeviceHandler} is responsible for handling the {@link Device}s and their commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public class InnogyDeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = SUPPORTED_DEVICE_THING_TYPES;

    private static final String DEBUG = "DEBUG";
    private static final String LONG_PRESS = "LongPress";
    private static final String SHORT_PRESS = "ShortPress";

    private final Logger logger = LoggerFactory.getLogger(InnogyDeviceHandler.class);
    private final Object lock = new Object();

    private String deviceId = "";
    private @Nullable InnogyBridgeHandler bridgeHandler;

    /**
     * Constructs a new {@link InnogyDeviceHandler} for the given {@link Thing}.
     *
     * @param thing
     */
    public InnogyDeviceHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.debug("handleCommand called for channel '{}' of type '{}' with command '{}'", channelUID,
                getThing().getThingTypeUID().getId(), command);
        @Nullable
        final InnogyBridgeHandler innogyBridgeHandler = getInnogyBridgeHandler();
        if (innogyBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }
        if (!ThingStatus.ONLINE.equals(innogyBridgeHandler.getThing().getStatus())) {
            logger.debug("Cannot handle command - bridge is not online. Command ignored.");
            return;
        }

        if (command instanceof RefreshType) {
            @Nullable
            final Device device = innogyBridgeHandler.getDeviceById(deviceId);
            if (device != null) {
                onDeviceStateChanged(device);
            }
            return;
        }

        // SWITCH
        if (CHANNEL_SWITCH.equals(channelUID.getId())) {
            // DEBUGGING HELPER
            // ----------------
            @Nullable
            final Device device = innogyBridgeHandler.getDeviceById(deviceId);
            if (device != null && DEBUG.equals(device.getConfig().getName())) {
                logger.debug("DEBUG SWITCH ACTIVATED!");
                if (OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.onEvent(
                            "{\"sequenceNumber\": -1,\"type\": \"MessageCreated\",\"desc\": \"/desc/event/MessageCreated\",\"namespace\": \"core.RWE\",\"timestamp\": \"2019-07-07T18:41:47.2970000Z\",\"source\": \"/desc/device/SHC.RWE/1.0\",\"data\": {\"id\": \"6e5ce2290cd247208f95a5b53736958b\",\"type\": \"DeviceLowBattery\",\"read\": false,\"class\": \"Alert\",\"timestamp\": \"2019-07-07T18:41:47.232Z\",\"devices\": [\"/device/fe51785319854f36a621d0b4f8ea0e25\"],\"properties\": {\"deviceName\": \"Heizkörperthermostat\",\"serialNumber\": \"914110165056\",\"locationName\": \"Bad\"},\"namespace\": \"core.RWE\"}}");
                } else {
                    innogyBridgeHandler.onEvent(
                            "{\"sequenceNumber\": -1,\"type\": \"MessageDeleted\",\"desc\": \"/desc/event/MessageDeleted\",\"namespace\": \"core.RWE\",\"timestamp\": \"2019-07-07T19:15:39.2100000Z\",\"data\": { \"id\": \"6e5ce2290cd247208f95a5b53736958b\" }}");
                }
                return;
            }
            // ----------------
            if (command instanceof OnOffType) {
                innogyBridgeHandler.commandSwitchDevice(deviceId, OnOffType.ON.equals(command));
            }

            // DIMMER
        } else if (CHANNEL_DIMMER.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                final DecimalType dimLevel = (DecimalType) command;
                innogyBridgeHandler.commandSetDimmLevel(deviceId, dimLevel.intValue());
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.commandSetDimmLevel(deviceId, 100);
                } else {
                    innogyBridgeHandler.commandSetDimmLevel(deviceId, 0);
                }
            }

            // ROLLERSHUTTER
        } else if (CHANNEL_ROLLERSHUTTER.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                final DecimalType rollerShutterLevel = (DecimalType) command;
                innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                        invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, rollerShutterLevel.intValue()));
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.commandSetRollerShutterStop(deviceId, ShutterAction.ShutterActions.DOWN);
                } else {
                    innogyBridgeHandler.commandSetRollerShutterStop(deviceId, ShutterAction.ShutterActions.UP);
                }
            } else if (command instanceof UpDownType) {
                if (UpDownType.DOWN.equals(command)) {
                    innogyBridgeHandler.commandSetRollerShutterStop(deviceId, ShutterAction.ShutterActions.DOWN);
                } else {
                    innogyBridgeHandler.commandSetRollerShutterStop(deviceId, ShutterAction.ShutterActions.UP);
                }
            } else if (command instanceof StopMoveType) {
                if (StopMoveType.STOP.equals(command)) {
                    innogyBridgeHandler.commandSetRollerShutterStop(deviceId, ShutterAction.ShutterActions.STOP);
                }
            }

            // SET_TEMPERATURE
        } else if (CHANNEL_SET_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                final DecimalType pointTemperature = (DecimalType) command;
                innogyBridgeHandler.commandUpdatePointTemperature(deviceId, pointTemperature.doubleValue());
            }

            // OPERATION_MODE
        } else if (CHANNEL_OPERATION_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                final String autoModeCommand = command.toString();

                if (CapabilityState.STATE_VALUE_OPERATION_MODE_AUTO.equals(autoModeCommand)) {
                    innogyBridgeHandler.commandSetOperationMode(deviceId, true);
                } else if (CapabilityState.STATE_VALUE_OPERATION_MODE_MANUAL.equals(autoModeCommand)) {
                    innogyBridgeHandler.commandSetOperationMode(deviceId, false);
                } else {
                    logger.warn("Could not set operationmode. Invalid value '{}'! Only '{}' or '{}' allowed.",
                            autoModeCommand, CapabilityState.STATE_VALUE_OPERATION_MODE_AUTO,
                            CapabilityState.STATE_VALUE_OPERATION_MODE_MANUAL);
                }
            }

            // ALARM
        } else if (CHANNEL_ALARM.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                innogyBridgeHandler.commandSwitchAlarm(deviceId, OnOffType.ON.equals(command));
            }
        } else {
            logger.debug("UNSUPPORTED channel {} for device {}.", channelUID.getId(), deviceId);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing innogy SmartHome device handler.");
        initializeThing(getBridge() == null ? null : getBridge().getStatus());
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(this);
        }
    }

    @Override
    public void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    /**
     * Initializes the {@link Thing} corresponding to the given status of the bridge.
     *
     * @param bridgeStatus
     */
    private void initializeThing(@Nullable final ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configDeviceId = (String) getConfig().get(PROPERTY_ID);
        if (configDeviceId != null) {
            deviceId = configDeviceId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getInnogyBridgeHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    if (initializeProperties()) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                                "Device not found in innogy config. Was it removed?");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "device id unknown");
        }
    }

    /**
     * Initializes all properties of the {@link Device}, like vendor, serialnumber etc.
     */
    private boolean initializeProperties() {
        synchronized (this.lock) {
            @Nullable
            final Device device = getDevice();
            if (device != null) {
                final Map<String, String> properties = editProperties();
                properties.put(PROPERTY_ID, device.getId());
                properties.put(PROPERTY_PROTOCOL_ID, device.getConfig().getProtocolId());
                if (device.hasSerialNumber()) {
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialnumber());
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
                return true;
            } else {
                logger.warn("initializeProperties: The device with id {} isn't found", deviceId);
                return false;
            }
        }
    }

    /**
     * Returns the {@link Device} associated with this {@link InnogyDeviceHandler} (referenced by the
     * {@link InnogyDeviceHandler#deviceId}).
     *
     * @return the {@link Device} or null, if not found or no {@link InnogyBridgeHandler} is available
     */
    private @Nullable Device getDevice() {
        if (getInnogyBridgeHandler() != null) {
            return getInnogyBridgeHandler().getDeviceById(deviceId);
        }
        return null;
    }

    /**
     * Returns the innogy bridge handler.
     *
     * @return the {@link InnogyBridgeHandler} or null
     */
    private @Nullable InnogyBridgeHandler getInnogyBridgeHandler() {
        synchronized (this.lock) {
            if (this.bridgeHandler == null) {
                @Nullable
                final Bridge bridge = getBridge();
                if (bridge == null) {
                    return null;
                }
                @Nullable
                final ThingHandler handler = bridge.getHandler();
                if (handler instanceof InnogyBridgeHandler) {
                    this.bridgeHandler = (InnogyBridgeHandler) handler;
                    this.bridgeHandler.registerDeviceStatusListener(this);
                } else {
                    return null;
                }
            }
            return this.bridgeHandler;
        }
    }

    @Override
    public void onDeviceStateChanged(final Device device) {
        synchronized (this.lock) {
            if (!deviceId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        deviceId);
                return;
            }

            logger.debug("onDeviceStateChanged called with device {}/{}", device.getConfig().getName(), device.getId());

            // DEVICE STATES
            if (device.hasDeviceState()) {
                @Nullable
                Boolean reachable = null;
                if (device.getDeviceState().hasIsReachableState()) {
                    reachable = device.getDeviceState().isReachable();
                }

                if (reachable != null && !reachable) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not reachable.");
                    return;
                } else if ((reachable != null && reachable) || DEVICE_VARIABLE_ACTUATOR.equals(device.getType())) {
                    if (device.getDeviceState().deviceIsIncluded()) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "State is " + device.getDeviceState().getDeviceInclusionState());
                    }
                }
            }

            if (device.isBatteryPowered()) {
                if (device.hasLowBattery()) {
                    updateState(CHANNEL_BATTERY_LOW, OnOffType.ON);
                } else {
                    updateState(CHANNEL_BATTERY_LOW, OnOffType.OFF);
                }
            }

            // CAPABILITY STATES
            for (final Entry<String, Capability> entry : device.getCapabilityMap().entrySet()) {
                final Capability c = entry.getValue();

                logger.debug("->capability:{} ({}/{})", c.getId(), c.getType(), c.getName());

                if (c.getCapabilityState() == null) {
                    logger.debug("Capability not available for device {} ({})", device.getConfig().getName(),
                            device.getType());
                    continue;
                }
                switch (c.getType()) {
                    case Capability.TYPE_VARIABLEACTUATOR:
                        final Boolean variableActuatorState = c.getCapabilityState().getVariableActuatorState();
                        if (variableActuatorState != null) {
                            updateState(CHANNEL_SWITCH, variableActuatorState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_SWITCHACTUATOR:
                        final Boolean switchActuatorState = c.getCapabilityState().getSwitchActuatorState();
                        if (switchActuatorState != null) {
                            updateState(CHANNEL_SWITCH, switchActuatorState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_DIMMERACTUATOR:
                        final Integer dimLevel = c.getCapabilityState().getDimmerActuatorState();
                        if (dimLevel != null) {
                            logger.debug("Dimlevel state {}", dimLevel);
                            updateState(CHANNEL_DIMMER, new PercentType(dimLevel));
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_ROLLERSHUTTERACTUATOR:
                        Integer rollerShutterLevel = c.getCapabilityState().getRollerShutterActuatorState();
                        if (rollerShutterLevel != null) {
                            rollerShutterLevel = invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, rollerShutterLevel);
                            logger.debug("RollerShutterlevel state {}", rollerShutterLevel);
                            updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rollerShutterLevel));
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_TEMPERATURESENSOR:
                        // temperature
                        final Double temperatureSensorState = c.getCapabilityState()
                                .getTemperatureSensorTemperatureState();
                        if (temperatureSensorState != null) {
                            logger.debug("-> Temperature sensor state: {}", temperatureSensorState);
                            updateState(CHANNEL_TEMPERATURE, new DecimalType(temperatureSensorState));
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // frost warning
                        final Boolean temperatureSensorFrostWarningState = c.getCapabilityState()
                                .getTemperatureSensorFrostWarningState();
                        if (temperatureSensorFrostWarningState != null) {
                            updateState(CHANNEL_FROST_WARNING,
                                    temperatureSensorFrostWarningState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        break;
                    case Capability.TYPE_THERMOSTATACTUATOR:
                        // point temperature
                        final Double thermostatActuatorPointTemperatureState = c.getCapabilityState()
                                .getThermostatActuatorPointTemperatureState();
                        if (thermostatActuatorPointTemperatureState != null) {
                            final DecimalType pointTemp = new DecimalType(thermostatActuatorPointTemperatureState);
                            logger.debug(
                                    "Update CHANNEL_SET_TEMPERATURE: state:{}->decType:{} (DeviceName {}, Capab-ID:{})",
                                    thermostatActuatorPointTemperatureState, pointTemp, device.getConfig().getName(),
                                    c.getId());
                            updateState(CHANNEL_SET_TEMPERATURE, pointTemp);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // operation mode
                        final String thermostatActuatorOperationModeState = c.getCapabilityState()
                                .getThermostatActuatorOperationModeState();
                        if (thermostatActuatorOperationModeState != null) {
                            final StringType operationMode = new StringType(thermostatActuatorOperationModeState);
                            updateState(CHANNEL_OPERATION_MODE, operationMode);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // window reduction active
                        final Boolean thermostatActuatorWindowReductionActiveState = c.getCapabilityState()
                                .getThermostatActuatorWindowReductionActiveState();
                        if (thermostatActuatorWindowReductionActiveState != null) {
                            updateState(CHANNEL_WINDOW_REDUCTION_ACTIVE,
                                    thermostatActuatorWindowReductionActiveState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_HUMIDITYSENSOR:
                        // humidity
                        final Double humidityState = c.getCapabilityState().getHumiditySensorHumidityState();
                        if (humidityState != null) {
                            final DecimalType humidity = new DecimalType(humidityState);
                            updateState(CHANNEL_HUMIDITY, humidity);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // mold warning
                        final Boolean humiditySensorMoldWarningState = c.getCapabilityState()
                                .getHumiditySensorMoldWarningState();
                        if (humiditySensorMoldWarningState != null) {
                            updateState(CHANNEL_MOLD_WARNING,
                                    humiditySensorMoldWarningState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_WINDOWDOORSENSOR:
                        final Boolean contactState = c.getCapabilityState().getWindowDoorSensorState();
                        if (contactState != null) {
                            updateState(CHANNEL_CONTACT, contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_SMOKEDETECTORSENSOR:
                        final Boolean smokeState = c.getCapabilityState().getSmokeDetectorSensorState();
                        if (smokeState != null) {
                            updateState(CHANNEL_SMOKE, smokeState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_ALARMACTUATOR:
                        final Boolean alarmState = c.getCapabilityState().getAlarmActuatorState();
                        if (alarmState != null) {
                            updateState(CHANNEL_ALARM, alarmState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_MOTIONDETECTIONSENSOR:
                        final Integer motionState = c.getCapabilityState().getMotionDetectionSensorState();
                        if (motionState != null) {
                            final DecimalType motionCount = new DecimalType(motionState);
                            logger.debug("Motion state {} -> count {}", motionState, motionCount);
                            updateState(CHANNEL_MOTION_COUNT, motionCount);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_LUMINANCESENSOR:
                        final Double luminanceState = c.getCapabilityState().getLuminanceSensorState();
                        if (luminanceState != null) {
                            final DecimalType luminance = new DecimalType(luminanceState);
                            updateState(CHANNEL_LUMINANCE, luminance);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_PUSHBUTTONSENSOR:
                        final Integer pushCountState = c.getCapabilityState().getPushButtonSensorCounterState();
                        final Integer buttonIndexState = c.getCapabilityState().getPushButtonSensorButtonIndexState();
                        logger.debug("Pushbutton index {} count {}", buttonIndexState, pushCountState);
                        if (pushCountState != null) {
                            final DecimalType pushCount = new DecimalType(pushCountState);
                            // prevent error when buttonIndexState is null
                            if (buttonIndexState != null) {
                                if (buttonIndexState >= 0 && buttonIndexState <= 7) {
                                    final int channelIndex = buttonIndexState + 1;
                                    final String type = c.getCapabilityState().getPushButtonSensorButtonIndexType();
                                    final String triggerEvent = SHORT_PRESS.equals(type)
                                            ? CommonTriggerEvents.SHORT_PRESSED
                                            : (LONG_PRESS.equals(type) ? CommonTriggerEvents.LONG_PRESSED
                                                    : CommonTriggerEvents.PRESSED);

                                    triggerChannel(CHANNEL_BUTTON + channelIndex, triggerEvent);
                                    updateState(String.format(CHANNEL_BUTTON_COUNT, channelIndex), pushCount);
                                } else {
                                    logger.debug("Button index {} not supported.", buttonIndexState);
                                }
                                // Button handled so remove state to avoid re-trigger.
                                c.getCapabilityState().setPushButtonSensorButtonIndexState(null);
                                c.getCapabilityState().setPushButtonSensorButtonIndexType(null);
                            } else {
                                logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                        c.getCapabilityState().getId(), c.getId());
                            }
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_ENERGYCONSUMPTIONSENSOR:
                        updateStateForEnergyChannel(CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH,
                                c.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionMonthKWhState(), c);
                        updateStateForEnergyChannel(CHANNEL_ABOLUTE_ENERGY_CONSUMPTION,
                                c.getCapabilityState().getEnergyConsumptionSensorAbsoluteEnergyConsumptionState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO,
                                c.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionMonthEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_CONSUMPTION_DAY_EURO,
                                c.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionDayEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_CONSUMPTION_DAY_KWH,
                                c.getCapabilityState().getEnergyConsumptionSensorEnergyConsumptionDayKWhState(), c);
                        break;
                    case Capability.TYPE_POWERCONSUMPTIONSENSOR:
                        updateStateForEnergyChannel(CHANNEL_POWER_CONSUMPTION_WATT,
                                c.getCapabilityState().getPowerConsumptionSensorPowerConsumptionWattState(), c);
                        break;
                    case Capability.TYPE_GENERATIONMETERENERGYSENSOR:
                        updateStateForEnergyChannel(CHANNEL_ENERGY_GENERATION_MONTH_KWH,
                                c.getCapabilityState().getGenerationMeterEnergySensorEnergyPerMonthInKWhState(), c);
                        updateStateForEnergyChannel(CHANNEL_TOTAL_ENERGY_GENERATION,
                                c.getCapabilityState().getGenerationMeterEnergySensorTotalEnergyState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_GENERATION_MONTH_EURO,
                                c.getCapabilityState().getGenerationMeterEnergySensorEnergyPerMonthInEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_GENERATION_DAY_EURO,
                                c.getCapabilityState().getGenerationMeterEnergySensorEnergyPerDayInEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_GENERATION_DAY_KWH,
                                c.getCapabilityState().getGenerationMeterEnergySensorEnergyPerDayInKWhState(), c);
                        break;
                    case Capability.TYPE_GENERATIONMETERPOWERCONSUMPTIONSENSOR:
                        updateStateForEnergyChannel(CHANNEL_POWER_GENERATION_WATT,
                                c.getCapabilityState().getGenerationMeterPowerConsumptionSensorPowerInWattState(), c);
                        break;
                    case Capability.TYPE_TWOWAYMETERENERGYCONSUMPTIONSENSOR:
                        updateStateForEnergyChannel(CHANNEL_ENERGY_MONTH_KWH,
                                c.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(),
                                c);
                        updateStateForEnergyChannel(CHANNEL_TOTAL_ENERGY,
                                c.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorTotalEnergyState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_MONTH_EURO,
                                c.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(),
                                c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_DAY_EURO,
                                c.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(),
                                c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_DAY_KWH,
                                c.getCapabilityState().getTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(),
                                c);
                        break;
                    case Capability.TYPE_TWOWAYMETERENERGYFEEDSENSOR:
                        updateStateForEnergyChannel(CHANNEL_ENERGY_FEED_MONTH_KWH,
                                c.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(), c);
                        updateStateForEnergyChannel(CHANNEL_TOTAL_ENERGY_FED,
                                c.getCapabilityState().getTwoWayMeterEnergyFeedSensorTotalEnergyState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_FEED_MONTH_EURO,
                                c.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_FEED_DAY_EURO,
                                c.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(), c);
                        updateStateForEnergyChannel(CHANNEL_ENERGY_FEED_DAY_KWH,
                                c.getCapabilityState().getTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(), c);
                        break;
                    case Capability.TYPE_TWOWAYMETERPOWERCONSUMPTIONSENSOR:
                        updateStateForEnergyChannel(CHANNEL_POWER_WATT,
                                c.getCapabilityState().getTwoWayMeterPowerConsumptionSensorPowerInWattState(), c);
                        break;
                    default:
                        logger.debug("Unsupported capability type {}.", c.getType());
                        break;
                }
            }
        }
    }

    /**
     * Updates the state for the {@link Channel} of an energy {@link Device}.
     *
     * @param channelId
     * @param state
     * @param capability
     */
    private void updateStateForEnergyChannel(final String channelId, @Nullable final Double state,
            final Capability capability) {
        if (state != null) {
            final DecimalType newValue = new DecimalType(state);
            updateState(channelId, newValue);
        } else {
            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", capability.getType(),
                    capability.getCapabilityState().getId(), capability.getId());
        }
    }

    @Override
    public void onDeviceStateChanged(final Device changedDevice, final Event event) {
        synchronized (this.lock) {
            Device device = changedDevice;
            if (!deviceId.equals(device.getId())) {
                return;
            }

            logger.trace("DeviceId {} relevant for this handler.", device.getId());

            if (event.isLinkedtoCapability()) {
                boolean deviceChanged = false;
                final String linkedCapabilityId = event.getSourceId();

                Map<String, Capability> capabilityMap = device.getCapabilityMap();
                Capability capability = capabilityMap.get(linkedCapabilityId);
                logger.trace("Loaded Capability {}, {} with id {}, device {} from device id {}", capability.getType(),
                        capability.getName(), capability.getId(), capability.getDeviceLink(), device.getId());

                CapabilityState capabilityState;
                if (capability.hasState()) {
                    capabilityState = capability.getCapabilityState();

                    // VariableActuator
                    if (capability.isTypeVariableActuator()) {
                        capabilityState.setVariableActuatorState(event.getProperties().getValue());
                        deviceChanged = true;

                        // SwitchActuator
                    } else if (capability.isTypeSwitchActuator()) {
                        capabilityState.setSwitchActuatorState(event.getProperties().getOnState());
                        deviceChanged = true;

                        // DimmerActuator
                    } else if (capability.isTypeDimmerActuator()) {
                        capabilityState.setDimmerActuatorState(event.getProperties().getDimLevel());
                        deviceChanged = true;

                        // RollerShutterActuator
                    } else if (capability.isTypeRollerShutterActuator()) {
                        capabilityState.setRollerShutterActuatorState(event.getProperties().getShutterLevel());
                        deviceChanged = true;

                        // TemperatureSensor
                    } else if (capability.isTypeTemperatureSensor()) {
                        // when values are changed, they come with separate events
                        // values should only updated when they are not null
                        final Double tmpTemperatureState = event.getProperties().getTemperature();
                        final Boolean tmpFrostWarningState = event.getProperties().getFrostWarning();
                        if (tmpTemperatureState != null) {
                            capabilityState.setTemperatureSensorTemperatureState(tmpTemperatureState);
                        }
                        if (tmpFrostWarningState != null) {
                            capabilityState.setTemperatureSensorFrostWarningState(tmpFrostWarningState);
                        }
                        deviceChanged = true;

                        // ThermostatActuator
                    } else if (capability.isTypeThermostatActuator()) {
                        // when values are changed, they come with separate events
                        // values should only updated when they are not null

                        final Double tmpPointTemperatureState = event.getProperties().getPointTemperature();
                        final String tmpOperationModeState = event.getProperties().getOperationMode();
                        final Boolean tmpWindowReductionActiveState = event.getProperties().getWindowReductionActive();

                        if (tmpPointTemperatureState != null) {
                            capabilityState.setThermostatActuatorPointTemperatureState(tmpPointTemperatureState);
                        }
                        if (tmpOperationModeState != null) {
                            capabilityState.setThermostatActuatorOperationModeState(tmpOperationModeState);
                        }
                        if (tmpWindowReductionActiveState != null) {
                            capabilityState
                                    .setThermostatActuatorWindowReductionActiveState(tmpWindowReductionActiveState);
                        }
                        deviceChanged = true;

                        // HumiditySensor
                    } else if (capability.isTypeHumiditySensor()) {
                        // when values are changed, they come with separate events
                        // values should only updated when they are not null
                        final Double tmpHumidityState = event.getProperties().getHumidity();
                        final Boolean tmpMoldWarningState = event.getProperties().getMoldWarning();
                        if (tmpHumidityState != null) {
                            capabilityState.setHumiditySensorHumidityState(tmpHumidityState);
                        }
                        if (tmpMoldWarningState != null) {
                            capabilityState.setHumiditySensorMoldWarningState(tmpMoldWarningState);
                        }
                        deviceChanged = true;

                        // WindowDoorSensor
                    } else if (capability.isTypeWindowDoorSensor()) {
                        capabilityState.setWindowDoorSensorState(event.getProperties().getIsOpen());
                        deviceChanged = true;

                        // SmokeDetectorSensor
                    } else if (capability.isTypeSmokeDetectorSensor()) {
                        capabilityState.setSmokeDetectorSensorState(event.getProperties().getIsSmokeAlarm());
                        deviceChanged = true;

                        // AlarmActuator
                    } else if (capability.isTypeAlarmActuator()) {
                        capabilityState.setAlarmActuatorState(event.getProperties().getOnState());
                        deviceChanged = true;

                        // MotionDetectionSensor
                    } else if (capability.isTypeMotionDetectionSensor()) {
                        capabilityState.setMotionDetectionSensorState(event.getProperties().getMotionDetectedCount());
                        deviceChanged = true;

                        // LuminanceSensor
                    } else if (capability.isTypeLuminanceSensor()) {
                        capabilityState.setLuminanceSensorState(event.getProperties().getLuminance());
                        deviceChanged = true;

                        // PushButtonSensor
                    } else if (capability.isTypePushButtonSensor()) {
                        // Some devices send both StateChanged and ButtonPressed. But only one should be handled.
                        // If ButtonPressed is send lastPressedButtonIndex is not set in StateChanged so ignore
                        // StateChanged.
                        // type is also not always present if null will be interpreted as a normal key press.
                        final Integer tmpButtonIndex = event.getProperties().getLastPressedButtonIndex();

                        if (tmpButtonIndex != null) {
                            capabilityState.setPushButtonSensorButtonIndexState(tmpButtonIndex);
                            capabilityState
                                    .setPushButtonSensorButtonIndexType(event.getProperties().getLastKeyPressType());

                            final Integer tmpLastKeyPressCounter = event.getProperties().getLastKeyPressCounter();

                            if (tmpLastKeyPressCounter != null) {
                                capabilityState.setPushButtonSensorCounterState(tmpLastKeyPressCounter);
                            }
                            deviceChanged = true;
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
                        deviceChanged = true;

                        // PowerConsumptionSensor
                    } else if (capability.isTypePowerConsumptionSensor()) {
                        capabilityState.setPowerConsumptionSensorPowerConsumptionWattState(
                                event.getProperties().getPowerConsumptionWatt());
                        deviceChanged = true;

                        // GenerationMeterEnergySensor
                    } else if (capability.isTypeGenerationMeterEnergySensor()) {
                        capabilityState.setGenerationMeterEnergySensorEnergyPerMonthInKWhState(
                                event.getProperties().getEnergyPerMonthInKWh());
                        capabilityState
                                .setGenerationMeterEnergySensorTotalEnergyState(event.getProperties().getTotalEnergy());
                        capabilityState.setGenerationMeterEnergySensorEnergyPerMonthInEuroState(
                                event.getProperties().getEnergyPerMonthInEuro());
                        capabilityState.setGenerationMeterEnergySensorEnergyPerDayInEuroState(
                                event.getProperties().getEnergyPerDayInEuro());
                        capabilityState.setGenerationMeterEnergySensorEnergyPerDayInKWhState(
                                event.getProperties().getEnergyPerDayInKWh());
                        deviceChanged = true;

                        // GenerationMeterPowerConsumptionSensor
                    } else if (capability.isTypeGenerationMeterPowerConsumptionSensor()) {
                        capabilityState.setGenerationMeterPowerConsumptionSensorPowerInWattState(
                                event.getProperties().getPowerInWatt());
                        deviceChanged = true;

                        // TwoWayMeterEnergyConsumptionSensor
                    } else if (capability.isTypeTwoWayMeterEnergyConsumptionSensor()) {
                        capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInKWhState(
                                event.getProperties().getEnergyPerMonthInKWh());
                        capabilityState.setTwoWayMeterEnergyConsumptionSensorTotalEnergyState(
                                event.getProperties().getTotalEnergy());
                        capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerMonthInEuroState(
                                event.getProperties().getEnergyPerMonthInEuro());
                        capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInEuroState(
                                event.getProperties().getEnergyPerDayInEuro());
                        capabilityState.setTwoWayMeterEnergyConsumptionSensorEnergyPerDayInKWhState(
                                event.getProperties().getEnergyPerDayInKWh());
                        deviceChanged = true;

                        // TwoWayMeterEnergyFeedSensor
                    } else if (capability.isTypeTwoWayMeterEnergyFeedSensor()) {
                        capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInKWhState(
                                event.getProperties().getEnergyPerMonthInKWh());
                        capabilityState
                                .setTwoWayMeterEnergyFeedSensorTotalEnergyState(event.getProperties().getTotalEnergy());
                        capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerMonthInEuroState(
                                event.getProperties().getEnergyPerMonthInEuro());
                        capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerDayInEuroState(
                                event.getProperties().getEnergyPerDayInEuro());
                        capabilityState.setTwoWayMeterEnergyFeedSensorEnergyPerDayInKWhState(
                                event.getProperties().getEnergyPerDayInKWh());
                        deviceChanged = true;

                        // TwoWayMeterPowerConsumptionSensor
                    } else if (capability.isTypeTwoWayMeterPowerConsumptionSensor()) {
                        capabilityState.setTwoWayMeterPowerConsumptionSensorPowerInWattState(
                                event.getProperties().getPowerInWatt());
                        deviceChanged = true;

                    } else {
                        logger.debug("Unsupported capability type {}.", capability.getType());
                    }
                } else {
                    logger.debug("Capability {} has no state (yet?) - refreshing device.", capability.getName());

                    @Nullable
                    final InnogyBridgeHandler innogyBridgeHandler = getInnogyBridgeHandler();
                    if (innogyBridgeHandler != null) {
                        device = innogyBridgeHandler.refreshDevice(deviceId);
                    }
                    if (device != null) {
                        capabilityMap = device.getCapabilityMap();
                        capability = capabilityMap.get(linkedCapabilityId);
                        if (capability.hasState()) {
                            deviceChanged = true;
                        }
                    }
                }
                if (deviceChanged && device != null) {
                    onDeviceStateChanged(device);
                }

            } else if (event.isLinkedtoDevice()) {
                if (device.hasDeviceState()) {
                    onDeviceStateChanged(device);
                } else {
                    logger.debug("Device {}/{} has no state.", device.getConfig().getName(), device.getId());
                }
            }
        }
    }

    /**
     * Returns the inverted value. Currently only rollershutter channels are supported.
     *
     * @param value
     * @return the value or the inverted value
     */
    private int invertValueIfConfigured(final String channelId, final int value) {
        if (!CHANNEL_ROLLERSHUTTER.equals(channelId)) {
            logger.debug("Channel {} cannot be inverted.", channelId);
            return value;
        }

        @Nullable
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.debug("Channel {} was null! Value not inverted.", channelId);
            return value;
        }
        final Boolean invert = (Boolean) channel.getConfiguration().get("invert");
        return invert != null && invert ? value : (100 - value);
    }
}
