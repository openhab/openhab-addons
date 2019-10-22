/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.CapabilityState;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;
import org.openhab.binding.innogysmarthome.internal.listener.DeviceStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyDeviceHandler} is responsible for handling the {@link Device}s and their commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class InnogyDeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = SUPPORTED_DEVICE_THING_TYPES;
    private final Logger logger = LoggerFactory.getLogger(InnogyDeviceHandler.class);

    private String deviceId;
    private InnogyBridgeHandler bridgeHandler;
    private final Object lock = new Object();

    /**
     * Constructs a new {@link InnogyDeviceHandler} for the given {@link Thing}.
     *
     * @param thing
     */
    public InnogyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel '{}' of type '{}' with command '{}'", channelUID,
                getThing().getThingTypeUID().getId(), command);
        InnogyBridgeHandler innogyBridgeHandler = getInnogyBridgeHandler();
        if (innogyBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }
        if (!ThingStatus.ONLINE.equals(innogyBridgeHandler.getThing().getStatus())) {
            logger.debug("Cannot handle command - bridge is not online. Command ignored.");
            return;
        }

        if (command instanceof RefreshType) {
            Device device = innogyBridgeHandler.getDeviceById(deviceId);
            if (device != null) {
                onDeviceStateChanged(device);
            }
            return;
        }

        // TODO: add devices
        // SWITCH
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            // DEBUGGING HELPER
            // ----------------
            Device device = innogyBridgeHandler.getDeviceById(deviceId);
            if(device.getConfig().getName().equals("DEBUG")) {
                logger.debug("DEBUG SWITCH ACTIVATED!");
                if(OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.onEvent("{\"sequenceNumber\": -1,\"type\": \"MessageCreated\",\"desc\": \"/desc/event/MessageCreated\",\"namespace\": \"core.RWE\",\"timestamp\": \"2019-07-07T18:41:47.2970000Z\",\"source\": \"/desc/device/SHC.RWE/1.0\",\"data\": {\"id\": \"6e5ce2290cd247208f95a5b53736958b\",\"type\": \"DeviceLowBattery\",\"read\": false,\"class\": \"Alert\",\"timestamp\": \"2019-07-07T18:41:47.232Z\",\"devices\": [\"/device/fe51785319854f36a621d0b4f8ea0e25\"],\"properties\": {\"deviceName\": \"Heizkörperthermostat\",\"serialNumber\": \"914110165056\",\"locationName\": \"Bad\"},\"namespace\": \"core.RWE\"}}");
                } else {
                    innogyBridgeHandler.onEvent("{\"sequenceNumber\": -1,\"type\": \"MessageDeleted\",\"desc\": \"/desc/event/MessageDeleted\",\"namespace\": \"core.RWE\",\"timestamp\": \"2019-07-07T19:15:39.2100000Z\",\"data\": { \"id\": \"6e5ce2290cd247208f95a5b53736958b\" }}");
                }
                return;
            }
            // ----------------
            if (command instanceof OnOffType) {
                innogyBridgeHandler.commandSwitchDevice(deviceId, OnOffType.ON.equals(command));
            }

            // DIMMER
        } else if (channelUID.getId().equals(CHANNEL_DIMMER)) {
            if (command instanceof DecimalType) {
                DecimalType dimLevel = (DecimalType) command;
                innogyBridgeHandler.commandSetDimmLevel(deviceId, dimLevel.intValue());
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.commandSetDimmLevel(deviceId, 100);
                } else {
                    innogyBridgeHandler.commandSetDimmLevel(deviceId, 0);
                }
            }

            // ROLLERSHUTTER
        } else if (channelUID.getId().equals(CHANNEL_ROLLERSHUTTER)) {
            if (command instanceof DecimalType) {
                DecimalType rollerShutterLevel = (DecimalType) command;
                innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                        invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, rollerShutterLevel.intValue()));
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                            invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, 100));
                } else {
                    innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                            invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, 0));
                }
            } else if (command instanceof UpDownType) {
                if (UpDownType.DOWN.equals(command)) {
                    innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                            invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, 100));
                } else {
                    innogyBridgeHandler.commandSetRollerShutterLevel(deviceId,
                            invertValueIfConfigured(CHANNEL_ROLLERSHUTTER, 0));
                }
            }

            // SET_TEMPERATURE
        } else if (channelUID.getId().equals(CHANNEL_SET_TEMPERATURE)) {
            if (command instanceof DecimalType) {
                DecimalType pointTemperature = (DecimalType) command;
                innogyBridgeHandler.commandUpdatePointTemperature(deviceId, pointTemperature.doubleValue());
            }

            // OPERATION_MODE
        } else if (channelUID.getId().equals(CHANNEL_OPERATION_MODE)) {
            if (command instanceof StringType) {
                StringType autoModeCommand = (StringType) command;

                if (autoModeCommand.toString().equals("Auto")) {
                    innogyBridgeHandler.commandSetOperationMode(deviceId, true);
                } else if (autoModeCommand.toString().equals("Manu")) {
                    innogyBridgeHandler.commandSetOperationMode(deviceId, false);
                } else {
                    logger.warn("Could not set operationmode. Invalid value '{}'! Only '{}' or '{}' allowed.",
                            autoModeCommand.toString(), "Auto", "Manu");
                }
            }

            // ALARM
        } else if (channelUID.getId().equals(CHANNEL_ALARM)) {
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
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    /**
     * Initializes the {@link Thing} corresponding to the given status of the bridge.
     *
     * @param bridgeStatus
     */
    private void initializeThing(ThingStatus bridgeStatus) {
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
            Device device = getDevice();
            if (device != null) {
                Map<String, String> properties = editProperties();
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
                // TODO: RST2
                if (Device.DEVICE_TYPE_RST.equals(device.getType())
                        || Device.DEVICE_TYPE_WRT.equals(device.getType())) {
                    properties.put(PROPERTY_DISPLAY_CURRENT_TEMPERATURE,
                            device.getConfig().getDisplayCurrentTemperature());
                }

                // Meter
                if (Device.DEVICE_TYPE_ANALOG_METER.equals(device.getType())
                        || Device.DEVICE_TYPE_GENERATION_METER.equals(device.getType())
                        || Device.DEVICE_TYPE_SMARTMETER.equals(device.getType())
                        || Device.DEVICE_TYPE_TWO_WAY_METER.equals(device.getType())) {
                    properties.put(PROPERTY_METER_ID, device.getConfig().getMeterId());
                    properties.put(PROPERTY_METER_FIRMWARE_VERSION, device.getConfig().getMeterFirmwareVersion());
                }

                if(device.getConfig().getTimeOfAcceptance() != null) {
                    properties.put(PROPERTY_TIME_OF_ACCEPTANCE, device.getConfig().getTimeOfAcceptance()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }
                if(device.getConfig().getTimeOfDiscovery() != null) {
                    properties.put(PROPERTY_TIME_OF_DISCOVERY, device.getConfig().getTimeOfDiscovery()
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }
    
                updateProperties(properties);

                // TODO: check device state first! E.g. there is no state, when device is still in configuration state.
                onDeviceStateChanged(device);
                return true;
            } else {
                logger.warn("initializeProperties: device is null");
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
    private Device getDevice() {
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
    private InnogyBridgeHandler getInnogyBridgeHandler() {
        synchronized (this.lock) {
            if (this.bridgeHandler == null) {
                Bridge bridge = getBridge();
                if (bridge == null) {
                    return null;
                }
                ThingHandler handler = bridge.getHandler();
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
    public void onDeviceStateChanged(Device device) {
        synchronized (this.lock) {
            if (!deviceId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        deviceId);
                return;
            }

            logger.debug("onDeviceStateChanged called with device {}/{}", device.getConfig().getName(), device.getId());

            // DEVICE STATES
            if (device.hasDeviceState()) {
                Boolean reachable = null;
                if (device.getDeviceState().hasIsReachableState()) {
                    reachable = device.getDeviceState().isReachable();
                }

                if (reachable != null && !reachable) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not reachable.");
                    return;
                } else if ((reachable != null && reachable)
                        || Device.DEVICE_TYPE_VARIABLE_ACTUATOR.equals(device.getType())) {
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
            for (Capability c : device.getCapabilityMap().values()) {
                logger.debug("->capability:{} ({}/{})", c.getId(), c.getType(), c.getName());

                if (c.getCapabilityState() == null) {
                    logger.debug("Capability not available for device {} ({})", device.getConfig().getName(),
                            device.getType());
                    continue;
                }
                // TODO: ADD DEVICES
                switch (c.getType()) {
                    case Capability.TYPE_VARIABLEACTUATOR:
                        Boolean variableActuatorState = c.getCapabilityState().getVariableActuatorState();
                        if (variableActuatorState != null) {
                            updateState(CHANNEL_SWITCH, variableActuatorState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_SWITCHACTUATOR:
                        Boolean switchActuatorState = c.getCapabilityState().getSwitchActuatorState();
                        if (switchActuatorState != null) {
                            updateState(CHANNEL_SWITCH, switchActuatorState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_DIMMERACTUATOR:
                        Integer dimLevel = c.getCapabilityState().getDimmerActuatorState();
                        if (dimLevel != null) {
                            logger.debug("Dimlevel state {}", dimLevel);
                            if (dimLevel > 0) {
                                updateState(CHANNEL_DIMMER, OnOffType.ON);
                            } else {
                                updateState(CHANNEL_DIMMER, OnOffType.OFF);
                            }
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
                            if (rollerShutterLevel > 0) {
                                updateState(CHANNEL_ROLLERSHUTTER, OnOffType.ON);
                            } else {
                                updateState(CHANNEL_ROLLERSHUTTER, OnOffType.OFF);
                            }
                            updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rollerShutterLevel));
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_TEMPERATURESENSOR:
                        // temperature
                        Double temperatureSensorState = c.getCapabilityState().getTemperatureSensorTemperatureState();
                        if (temperatureSensorState != null) {
                            logger.debug("-> Temperature sensor state: {}", temperatureSensorState);
                            updateState(CHANNEL_TEMPERATURE, new DecimalType(temperatureSensorState));
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // frost warning
                        Boolean temperatureSensorFrostWarningState = c.getCapabilityState()
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
                        Double thermostatActuatorPointTemperatureState = c.getCapabilityState()
                                .getThermostatActuatorPointTemperatureState();
                        if (thermostatActuatorPointTemperatureState != null) {
                            DecimalType pointTemp = new DecimalType(thermostatActuatorPointTemperatureState);
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
                        String thermostatActuatorOperationModeState = c.getCapabilityState()
                                .getThermostatActuatorOperationModeState();
                        if (thermostatActuatorOperationModeState != null) {
                            StringType operationMode = new StringType(thermostatActuatorOperationModeState);
                            updateState(CHANNEL_OPERATION_MODE, operationMode);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // window reduction active
                        Boolean thermostatActuatorWindowReductionActiveState = c.getCapabilityState()
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
                        Double humidityState = c.getCapabilityState().getHumiditySensorHumidityState();
                        if (humidityState != null) {
                            DecimalType humidity = new DecimalType(humidityState);
                            updateState(CHANNEL_HUMIDITY, humidity);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }

                        // mold warning
                        Boolean humiditySensorMoldWarningState = c.getCapabilityState()
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
                        Boolean contactState = c.getCapabilityState().getWindowDoorSensorState();
                        if (contactState != null) {
                            updateState(CHANNEL_CONTACT, contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_SMOKEDETECTORSENSOR:
                        Boolean smokeState = c.getCapabilityState().getSmokeDetectorSensorState();
                        if (smokeState != null) {
                            updateState(CHANNEL_SMOKE, smokeState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_ALARMACTUATOR:
                        Boolean alarmState = c.getCapabilityState().getAlarmActuatorState();
                        if (alarmState != null) {
                            updateState(CHANNEL_ALARM, alarmState ? OnOffType.ON : OnOffType.OFF);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_MOTIONDETECTIONSENSOR:
                        Integer motionState = c.getCapabilityState().getMotionDetectionSensorState();
                        if (motionState != null) {
                            DecimalType motionCount = new DecimalType(motionState);
                            logger.debug("Motion state {} -> count {}", motionState, motionCount);
                            updateState(CHANNEL_MOTION_COUNT, motionCount);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_LUMINANCESENSOR:
                        Double luminanceState = c.getCapabilityState().getLuminanceSensorState();
                        if (luminanceState != null) {
                            DecimalType luminance = new DecimalType(luminanceState);
                            updateState(CHANNEL_LUMINANCE, luminance);
                        } else {
                            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", c.getType(),
                                    c.getCapabilityState().getId(), c.getId());
                        }
                        break;
                    case Capability.TYPE_PUSHBUTTONSENSOR:
                        Integer pushCountState = c.getCapabilityState().getPushButtonSensorCounterState();
                        Integer buttonIndexState = c.getCapabilityState().getPushButtonSensorButtonIndexState();
                        logger.debug("Pushbutton index {} count {}", buttonIndexState, pushCountState);
                        if (pushCountState != null) {
                            DecimalType pushCount = new DecimalType(pushCountState);
                            if (buttonIndexState.equals(0)) {
                                triggerChannel(CHANNEL_BUTTON1, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON1_COUNT, pushCount);
                            } else if (buttonIndexState.equals(1)) {
                                triggerChannel(CHANNEL_BUTTON2, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON2_COUNT, pushCount);
                            } else if (buttonIndexState.equals(2)) {
                                triggerChannel(CHANNEL_BUTTON3, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON3_COUNT, pushCount);
                            } else if (buttonIndexState.equals(3)) {
                                triggerChannel(CHANNEL_BUTTON4, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON4_COUNT, pushCount);
                            } else if (buttonIndexState.equals(4)) {
                                triggerChannel(CHANNEL_BUTTON5, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON5_COUNT, pushCount);
                            } else if (buttonIndexState.equals(5)) {
                                triggerChannel(CHANNEL_BUTTON6, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON6_COUNT, pushCount);
                            } else if (buttonIndexState.equals(6)) {
                                triggerChannel(CHANNEL_BUTTON7, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON7_COUNT, pushCount);
                            } else if (buttonIndexState.equals(7)) {
                                triggerChannel(CHANNEL_BUTTON8, CommonTriggerEvents.PRESSED);
                                updateState(CHANNEL_BUTTON8_COUNT, pushCount);
                            } else {
                                logger.debug("Button index {} not supported.", buttonIndexState);
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
    private void updateStateForEnergyChannel(String channelId, Double state, Capability capability) {
        if (state != null) {
            DecimalType newValue = new DecimalType(state);
            updateState(channelId, newValue);
        } else {
            logger.debug("State for {} is STILL NULL!! cstate-id: {}, c-id: {}", capability.getType(),
                    capability.getCapabilityState().getId(), capability.getId());
        }
    }

    @Override
    public void onDeviceStateChanged(Device device, Event event) {
        synchronized (this.lock) {
            if (!deviceId.equals(device.getId())) {
                // logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                // deviceId);
                return;
            }

            logger.trace("DeviceId {} relevant for this handler.", device.getId());

            if (event.isLinkedtoCapability()) {
                boolean deviceChanged = false;
                String linkedCapabilityId = event.getSourceId();

                HashMap<String, Capability> capabilityMap = device.getCapabilityMap();
                Capability capability = capabilityMap.get(linkedCapabilityId);
                logger.trace("Loaded Capability {}, {} with id {}, device {} from device id {}", capability.getType(),
                        capability.getName(), capability.getId(), capability.getDeviceLink(), device.getId());

                CapabilityState capabilityState;
                if (capability.hasState()) {
                    capabilityState = capability.getCapabilityState();

                    // TODO: ADD DEVICES
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
                        capabilityState.setTemperatureSensorTemperatureState(event.getProperties().getTemperature());
                        capabilityState.setTemperatureSensorFrostWarningState(event.getProperties().getFrostWarning());
                        deviceChanged = true;

                        // ThermostatActuator
                    } else if (capability.isTypeThermostatActuator()) {
                        capabilityState.setThermostatActuatorPointTemperatureState(
                                event.getProperties().getPointTemperature());
                        capabilityState
                                .setThermostatActuatorOperationModeState(event.getProperties().getOperationMode());
                        capabilityState.setThermostatActuatorWindowReductionActiveState(
                                event.getProperties().getWindowReductionActive());
                        deviceChanged = true;

                        // HumiditySensor
                    } else if (capability.isTypeHumiditySensor()) {
                        capabilityState.setHumiditySensorHumidityState(event.getProperties().getHumidity());
                        capabilityState.setHumiditySensorMoldWarningState(event.getProperties().getMoldWarning());
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
                        capabilityState
                                .setPushButtonSensorButtonIndexState(event.getProperties().getLastPressedButtonIndex());
                        capabilityState.setPushButtonSensorCounterState(event.getProperties().getLastKeyPressCounter());
                        deviceChanged = true;

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
                } else { // capability.hasState()
                    logger.debug("Capability {} has no state (yet?) - refreshing device.", capability.getName());
                    device = getInnogyBridgeHandler().refreshDevice(deviceId);

                    capabilityMap = device.getCapabilityMap();
                    capability = capabilityMap.get(linkedCapabilityId);
                    if (capability.hasState()) {
                        capabilityState = capability.getCapabilityState();
                        deviceChanged = true;
                    }
                }
                if (deviceChanged) {
                    onDeviceStateChanged(device);
                }

            } else if (event.isLinkedtoDevice()) {
                if (device.hasDeviceState()) {
                    Map<String, Property> stateMap = device.getDeviceState().getStateMap();

                    // for (Property p : event.getProperties()) {
                    // logger.debug("State changed {} to {}.", p.getName(), p.getValue());

                    // stateMap.get(p.getName()).setValue(p.getValue());
                    // stateMap.get(p.getName()).setLastchanged((p.getLastchanged()));
                    // }

                    onDeviceStateChanged(device);
                } else {
                    logger.debug("Device {}/{} has no state.", device.getConfig().getName(), device.getId());
                    return;
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
    private int invertValueIfConfigured(String channelId, int value) {
        if (!CHANNEL_ROLLERSHUTTER.equals(channelId)) {
            logger.debug("Channel {} cannot be inverted.", channelId);
            return value;
        }

        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.debug("Channel {} was null! Value not inverted.", channelId);
            return value;
        }

        return 100 - value;
    }
}
