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
package org.openhab.binding.tapocontrol.internal.devices.wifi.lightswitch;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tapo Light Switch Handler
 *
 * @author Simmon Yau - Initial contribution
 * @author Lee Ballard - Added HS220 dimmer support
 */
@NonNullByDefault
public class TapoLightSwitchHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoLightSwitchHandler.class);
    private final boolean dimmer;
    private TapoLightSwitchData lightSwitchData;

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoLightSwitchHandler(Thing thing) {
        super(thing);
        dimmer = SUPPORTED_DIMMER_SWITCH_UIDS.contains(thing.getThingTypeUID());
        lightSwitchData = dimmer ? new TapoDimmerSwitchData() : new TapoLightSwitchData();
    }

    @Override
    public void newDataResult(String queryCommand) {
        super.newDataResult(queryCommand);
        if (DEVICE_CMD_GETINFO.equals(queryCommand)) {
            lightSwitchData = dimmer ? connector.getResponseData(TapoDimmerSwitchData.class)
                    : connector.getResponseData(TapoLightSwitchData.class);
            updateChannels(lightSwitchData);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            queryDeviceData();
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    handleOnOffCommand(command);
                    break;
                case CHANNEL_BRIGHTNESS:
                    handleBrightnessCommand(command);
                    break;
                default:
                    logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command,
                            channelUID.getId());
            }
        }
    }

    private void handleOnOffCommand(Command command) {
        switchOnOff(command == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE);
    }

    private void handleBrightnessCommand(Command command) {
        if (command instanceof PercentType percentCommand) {
            setBrightness(percentCommand.intValue());
        } else if (command instanceof DecimalType decimalCommand) {
            setBrightness(decimalCommand.intValue());
        } else {
            logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, CHANNEL_BRIGHTNESS);
        }
    }

    protected void switchOnOff(boolean on) {
        TapoLightSwitchData commandData = dimmer ? new TapoDimmerSwitchData() : lightSwitchData;
        commandData.switchOnOff(on);
        connector.sendCommandAndQuery(commandData, true);
    }

    protected void setBrightness(int brightness) {
        if (dimmer) {
            TapoDimmerSwitchData commandData = new TapoDimmerSwitchData();
            commandData.setBrightness(brightness);
            connector.sendCommandAndQuery(commandData, true);
        }
    }

    protected void updateChannels(TapoLightSwitchData deviceData) {
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceData.isOn()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceData.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceData.getOnTime(), Units.SECOND));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceData.isOverheated()));
        if (deviceData instanceof TapoDimmerSwitchData dimmerData) {
            updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                    getPercentType(dimmerData.isOn() ? dimmerData.getBrightness() : 0));
        }
    }
}
