/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.handler;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.openhab.binding.digitalstrom.internal.lib.climate.constants.ControlModes;
import org.openhab.binding.digitalstrom.internal.lib.climate.constants.ControlStates;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.TemperatureControlManager;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneTemperatureControlHandler} is responsible for handling the configuration, to load the supported
 * channel of a
 * digitalSTROM zone, which has a temperature control configured, and handling commands, which are sent to the channel.
 * <br>
 * <br>
 * For that it uses the {@link BridgeHandler} to register itself as {@link TemperatureControlStatusListener} at the
 * {@link TemperatureControlManager} to get informed by status changes. Through the registration as
 * {@link TemperatureControlStatusListener} a {@link TemperatureControlSensorTransmitter} will be registered to this
 * {@link ZoneTemperatureControlHandler}, which is needed to set the temperature or the control value of a zone.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class ZoneTemperatureControlHandler extends BaseThingHandler implements TemperatureControlStatusListener {

    /**
     * Contains all supported thing types of this handler
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL));

    private final Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlHandler.class);

    private TemperatureControlSensorTransmitter temperatureSensorTransmitter;

    private BridgeHandler dssBridgeHandler;
    private Integer zoneID;
    private String currentChannelID;
    private Float currentValue = 0f;
    private final Float step = 1f;

    // check zoneID error codes
    public static final int ZONE_ID_NOT_EXISTS = -1;
    public static final int ZONE_ID_NOT_SET = -2;
    public static final int BRIDGE_IS_NULL = -3;

    /**
     * Creates a new {@link ZoneTemperatureControlHandler}.
     *
     * @param thing must not be null
     */
    public ZoneTemperatureControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        if (getConfig().get(DigitalSTROMBindingConstants.ZONE_ID) != null) {
            final Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeStatusChanged(bridge.getStatusInfo());
            } else {
                // Set status to OFFLINE, if no bridge is available e.g. because the bridge has been removed and the
                // Thing was reinitialized.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "zoneID is missing");
        }
    }

    /**
     * Returns the configured zoneID of the given {@link Configuration}. If the zoneID does't exist or can't be checked
     * {@link #ZONE_ID_NOT_EXISTS}, {@link #ZONE_ID_NOT_SET} or {@link #BRIDGE_IS_NULL} will be returned.
     *
     * @param config the {@link Configuration} to be checked
     * @param bridge the responsible {@link BridgeHandler}
     * @return zoneID the existing dS zoneID or a error constant
     */
    public static int getZoneID(Configuration config, BridgeHandler bridge) {
        if (config == null || config.get(DigitalSTROMBindingConstants.ZONE_ID) == null) {
            return ZONE_ID_NOT_SET;
        }
        if (bridge == null) {
            return BRIDGE_IS_NULL;
        }
        String configZoneID = config.get(DigitalSTROMBindingConstants.ZONE_ID).toString();
        int zoneID;
        StructureManager strucMan = bridge.getStructureManager();
        if (strucMan != null) {
            try {
                zoneID = Integer.parseInt(configZoneID);
                if (!strucMan.checkZoneID(zoneID)) {
                    zoneID = ZONE_ID_NOT_EXISTS;
                }
            } catch (NumberFormatException e) {
                zoneID = strucMan.getZoneId(configZoneID);
            }
            return zoneID;
        }
        return ZONE_ID_NOT_EXISTS;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (zoneID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterTemperatureControlStatusListener(this);
            }
            temperatureSensorTransmitter = null;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            int tempZoneID = getZoneID(getConfig(), getDssBridgeHandler());
            if (tempZoneID == ZONE_ID_NOT_EXISTS) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configured zone '" + getConfig().get(DigitalSTROMBindingConstants.ZONE_ID)
                                + "' does not exist, please check the configuration.");
            } else {
                this.zoneID = tempZoneID;
            }
            if (zoneID != null) {
                if (getDssBridgeHandler() != null && temperatureSensorTransmitter == null) {
                    dssBridgeHandler.registerTemperatureControlStatusListener(this);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No zoneID is set!");
            }
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        logger.debug("Set status to {}", getThing().getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.debug("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }
        if (temperatureSensorTransmitter == null && zoneID != null) {
            logger.debug(
                    "Device not known on TemperationControlManager or temperatureSensorTransreciver is not registerd. Cannot handle command.");
            return;
        }
        if (channelUID.getId().equals(currentChannelID)) {
            if (command instanceof PercentType || command instanceof DecimalType) {
                sendCommandAndUpdateChannel(((DecimalType) command).floatValue());
            } else if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MAX_TEMP);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MAX_CONTROLL_VALUE);
                    }
                } else {
                    if (isTemperature()) {
                        sendCommandAndUpdateChannel(0f);
                    } else {
                        sendCommandAndUpdateChannel(TemperatureControlSensorTransmitter.MIN_CONTROLL_VALUE);
                    }
                }
            } else if (command instanceof IncreaseDecreaseType) {
                if (IncreaseDecreaseType.INCREASE.equals(command)) {
                    sendCommandAndUpdateChannel(currentValue + step);
                } else {
                    sendCommandAndUpdateChannel(currentValue - step);
                }
            }
        } else {
            logger.debug("Command sent to an unknown channel id: {}", channelUID);
        }
    }

    private boolean isTemperature() {
        return currentChannelID.contains(DsChannelTypeProvider.TEMPERATURE_CONTROLLED);
    }

    private void sendCommandAndUpdateChannel(Float newValue) {
        if (isTemperature()) {
            if (temperatureSensorTransmitter.pushTargetTemperature(zoneID, newValue)) {
                currentValue = newValue;
                updateState(currentChannelID, new DecimalType(newValue));
            }
        } else {
            if (temperatureSensorTransmitter.pushControlValue(zoneID, newValue)) {
                currentValue = newValue;
                updateState(currentChannelID, new PercentType(newValue.intValue()));
            }
        }
    }

    private synchronized BridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bride cannot be found");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof BridgeHandler) {
                dssBridgeHandler = (BridgeHandler) handler;
            } else {
                return null;
            }
        }
        return dssBridgeHandler;
    }

    @Override
    public synchronized void configChanged(TemperatureControlStatus tempControlStatus) {
        if (tempControlStatus != null && tempControlStatus.isNotSetOff()) {
            ControlModes controlMode = ControlModes.getControlMode(tempControlStatus.getControlMode());
            ControlStates controlState = ControlStates.getControlState(tempControlStatus.getControlState());
            if (controlMode != null && controlState != null) {
                logger.debug("config changed: {}", tempControlStatus.toString());
                if (controlMode.equals(ControlModes.OFF) && currentChannelID != null) {
                    currentChannelID = null;
                    loadChannel();
                } else if (controlMode.equals(ControlModes.PID_CONTROL)
                        && (currentChannelID == null
                                || !currentChannelID.contains(DsChannelTypeProvider.TEMPERATURE_CONTROLLED))
                        && !controlState.equals(ControlStates.EMERGENCY)) {
                    currentChannelID = DsChannelTypeProvider.getOutputChannelTypeID(ApplicationGroup.Color.BLUE,
                            OutputModeEnum.TEMPRETURE_PWM, new ArrayList<OutputChannelEnum>());
                    loadChannel();
                    currentValue = tempControlStatus.getNominalValue();
                    updateState(currentChannelID, new DecimalType(currentValue.doubleValue()));
                } else if (!controlMode.equals(ControlModes.PID_CONTROL) && !controlMode.equals(ControlModes.OFF)) {
                    currentChannelID = DsChannelTypeProvider.getOutputChannelTypeID(ApplicationGroup.Color.BLUE,
                            OutputModeEnum.HEATING_PWM, new ArrayList<OutputChannelEnum>());
                    loadChannel();
                    currentValue = tempControlStatus.getControlValue();
                    updateState(currentChannelID, new PercentType(fixPercent(currentValue.intValue())));
                    if (controlState.equals(ControlStates.EMERGENCY)) {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "The communication with temperation sensor fails. Temperature control state emergency (temperature control though the control value) is active.");
                    }
                }
                Map<String, String> properties = editProperties();
                properties.put("controlDSUID", tempControlStatus.getControlDSUID());
                properties.put("controlMode", controlMode.getKey());
                properties.put("controlState", controlState.getKey());
                updateProperties(properties);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "digitalSTROM temperature control is for this zone not configured in.");
        }
    }

    private synchronized void loadChannel() {
        List<Channel> newChannelList = new ArrayList<>(1);
        if (currentChannelID != null) {
            newChannelList.add(ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), currentChannelID),
                            DsChannelTypeProvider.getItemType(currentChannelID))
                    .withType(new ChannelTypeUID(BINDING_ID, currentChannelID)).build());
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(newChannelList);
        updateThing(thingBuilder.build());
        logger.debug("load channel: {} with item: {}", currentChannelID,
                DsChannelTypeProvider.getItemType(currentChannelID));
    }

    @Override
    public synchronized void onTargetTemperatureChanged(Float newValue) {
        if (isTemperature()) {
            updateState(currentChannelID, new DecimalType(newValue));
        }
    }

    @Override
    public synchronized void onControlValueChanged(Integer newValue) {
        if (!isTemperature()) {
            updateState(currentChannelID, new PercentType(fixPercent(newValue)));
        }
    }

    private int fixPercent(int value) {
        return value < 0 ? 0 : value > 100 ? 100 : value;
    }

    @Override
    public void registerTemperatureSensorTransmitter(
            TemperatureControlSensorTransmitter temperatureSensorTransreciver) {
        updateStatus(ThingStatus.ONLINE);
        this.temperatureSensorTransmitter = temperatureSensorTransreciver;
    }

    @Override
    public Integer getTemperationControlStatusListenrID() {
        return zoneID;
    }
}
