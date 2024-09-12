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
package org.openhab.binding.tapocontrol.internal.devices.wifi;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.wifi.lightstrip.TapoLightStripData;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
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
 * TAPO Universal-Device
 * universal device for testing pruposes
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoUniversalDeviceHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoUniversalDeviceHandler.class);
    private TapoLightStripData deviceData = new TapoLightStripData();
    private TapoRequest manualRequest = new TapoRequest(DEVICE_CMD_GETINFO);
    private boolean useSecurePassthrough = true;

    // Channel List for "Test- and Debug-Device"
    public static final String CHANNEL_GROUP_RESPONSE = "response";
    public static final String CHANNEL_GROUP_COMMAND = "devicecommand";
    public static final String CHANNEL_RESPONSE = "deviceResponse";
    public static final String CHANNEL_COMMAND_METHOD = "method";
    public static final String CHANNEL_COMMAND_PARAMS = "params";
    public static final String CHANNEL_COMMAND_SECURE = "secure";
    public static final String CHANNEL_COMMAND_SEND = "sendCommand";

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoUniversalDeviceHandler(Thing thing) {
        super(thing);
    }

    /**
     * Function called by {@link org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector} if new data were
     * received
     * 
     * @param queryCommand command where new data belong to
     */
    @Override
    public void newDataResult(String queryCommand) {
        super.newDataResult(queryCommand);
        if (DEVICE_CMD_GETINFO.equals(queryCommand)) {
            deviceData = connector.getResponseData(TapoLightStripData.class);
            updateChannels(deviceData);
        }
    }

    /**
     * query device Properties
     */
    @Override
    public void queryDeviceData() {
        deviceError.reset();
        if (isLoggedIn(LOGIN_RETRIES)) {
            connector.sendQueryCommand(DEVICE_CMD_GETINFO, true);
            connector.sendQueryCommand(DEVICE_CMD_GETENERGY, true);
        }
    }

    /*****************************
     * HANDLE COMMANDS
     *****************************/

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("({}) handleCommand '{}' for channelUID {}", uid, command, channelUID.getId());

        String group = channelUID.getGroupId();
        if (command instanceof RefreshType) {
            queryDeviceData();
        } else {
            if (CHANNEL_GROUP_RESPONSE.equals(group) || CHANNEL_GROUP_COMMAND.equals(group)) {
                handleSpecialCommands(channelUID, command);
            } else {
                handleStandardCommands(channelUID, command);
            }
        }
    }

    /**
     * Handle standard commands for debug-, test-devices
     */
    private void handleStandardCommands(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();

        switch (channel) {
            case CHANNEL_OUTPUT:
                handleOnOffCommand(command);
                break;
            case CHANNEL_BRIGHTNESS:
                handleBrightnessCommand(command);
                break;
            case CHANNEL_COLOR_TEMP:
                handleColorTempCommand(command);
                break;
            case CHANNEL_COLOR:
                handleColorCommand(command);
                break;
            default:
                logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channelUID.getId());
        }
    }

    /**
     * Handle special commands for debug-, test-devices
     */
    private void handleSpecialCommands(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();

        if (CHANNEL_GROUP_COMMAND.equals(channelUID.getGroupId())) {
            switch (channel) {
                case CHANNEL_COMMAND_METHOD:
                    manualRequest = new TapoRequest(command.toString(), manualRequest.params());
                    break;
                case CHANNEL_COMMAND_PARAMS:
                    manualRequest = new TapoRequest(manualRequest.method(), command.toString());
                    break;
                case CHANNEL_COMMAND_SECURE:
                    useSecurePassthrough = !useSecurePassthrough;
                    break;
                case CHANNEL_COMMAND_SEND:
                    /* send manual request */
                    if (useSecurePassthrough) {
                        logger.debug("({}) sendSecurePasstrough '{}' ", uid, manualRequest);
                        connector.sendAsyncRequest(manualRequest);
                    } else {
                        logger.debug("({}) sendRawCommand '{}' ", uid, manualRequest);
                        connector.sendRawCommand(manualRequest);
                    }
                    break;
                default:
                    logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command,
                            channelUID.getId());
            }

        } else {
            if (CHANNEL_RESPONSE.equals(channel)) {
                logger.debug("({}) NOT IMPLEMENTED COMMAND {} ", uid, command);
            }
        }
    }

    private void handleOnOffCommand(Command command) {
        switchOnOff(command == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE);
    }

    private void handleBrightnessCommand(Command command) {
        if (command instanceof PercentType percentCommand) {
            Float percent = percentCommand.floatValue();
            setBrightness(percent.intValue()); // 0..100% = 0..100
        } else if (command instanceof DecimalType decimalCommand) {
            setBrightness(decimalCommand.intValue());
        }
    }

    private void handleColorCommand(Command command) {
        if (command instanceof HSBType hsbCommand) {
            setColor(hsbCommand);
        }
    }

    private void handleColorTempCommand(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            setColorTemp(decimalCommand.intValue());
        }
    }

    /*****************************
     * SEND COMMANDS
     *****************************/

    /**
     * Switch device On or Off
     * 
     * @param on if true device will switch on. Otherwise switch off
     */
    protected void switchOnOff(boolean on) {
        deviceData.switchOnOff(on);
        connector.sendCommandAndQuery(deviceData, false);
    }

    /**
     * SET BRIGHTNESS
     * 
     * @param newBrightness percentage 0-100 of new brightness
     */
    protected void setBrightness(Integer newBrightness) {
        /* switch off if 0 */
        if (newBrightness == 0) {
            deviceData.switchOff();
        } else {
            deviceData.switchOn();
            deviceData.setBrightness(newBrightness);
            connector.sendCommandAndQuery(deviceData, false);
        }
    }

    /**
     * SET COLOR
     * 
     * @param command HSBType
     */
    protected void setColor(HSBType command) {
        deviceData.switchOn();
        deviceData.setHue(command.getHue().intValue());
        deviceData.setSaturation(command.getSaturation().intValue());
        deviceData.setBrightness(command.getBrightness().intValue());
        connector.sendCommandAndQuery(deviceData, false);
    }

    /**
     * SET COLORTEMP
     * 
     * @param colorTemp (Integer) in Kelvin
     */
    protected void setColorTemp(Integer colorTemp) {
        deviceData.switchOn();
        deviceData.setColorTemp(colorTemp);
        connector.sendCommandAndQuery(deviceData, false);
    }

    /**
     * Handle full responsebody received from connector
     * 
     * @param fullResponse TapoResponse received
     */
    @Override
    public void responsePasstrough(TapoResponse fullResponse) {
        String response = fullResponse.result().getAsString();
        logger.debug("({}) received response {}", uid, response);
        updateState(getChannelID(CHANNEL_GROUP_RESPONSE, CHANNEL_RESPONSE), getStringType(response));
    }

    /**
     * UPDATE PROPERTIES
     * 
     * @param deviceInfo TapoLightStripData
     */
    protected void updateChannels(TapoLightStripData deviceInfo) {
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceInfo.isOn()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceInfo.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR_TEMP),
                getDecimalType(deviceInfo.getColorTemp()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR), deviceInfo.getHSB());

        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceInfo.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceInfo.getOnTime(), Units.SECOND));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT),
                getDecimalType(deviceInfo.isOverheated() ? 1 : 0));
    }
}
