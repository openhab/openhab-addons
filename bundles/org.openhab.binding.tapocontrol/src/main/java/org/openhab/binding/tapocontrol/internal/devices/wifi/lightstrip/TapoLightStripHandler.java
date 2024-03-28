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
package org.openhab.binding.tapocontrol.internal.devices.wifi.lightstrip;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoLightEffect;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
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
 * TAPO Smart-Plug-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightStripHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoLightStripHandler.class);
    private TapoLightStripData lightStripData = new TapoLightStripData();

    /**
     * Constructor
     * 
     * @param thing Thing object representing device
     */
    public TapoLightStripHandler(Thing thing) {
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
            lightStripData = connector.getResponseData(TapoLightStripData.class);
            updateChannels(lightStripData);
        }
    }

    /*****************************
     * HANDLE COMMANDS
     *****************************/

    /**
     * handle command sent to device
     * 
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        String group = channelUID.getGroupId();
        if (command instanceof RefreshType) {
            queryDeviceData();
        } else if (CHANNEL_GROUP_EFFECTS.equals(group)) {
            handleLightFx(channel, command);
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    handleOnOffCommand(command);
                    break;
                case CHANNEL_BRIGHTNESS:
                    if (lightStripData.getLightEffect().isEnabled()) {
                        handleLightFx(channel, command);
                    } else {
                        handleBrightnessCommand(command);
                    }
                    break;
                case CHANNEL_COLOR_TEMP:
                    handleColorTempCommand(command);
                    break;
                case CHANNEL_COLOR:
                    handleColorCommand(command);
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

    private void handleLightFx(String channel, Command command) {
        TapoLightEffect lightEffect = lightStripData.getLightEffect();
        switch (channel) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType percentCommand) {
                    Float percent = percentCommand.floatValue();
                    lightEffect.setBrightness(percent.intValue()); // 0..100% = 0..100
                } else if (command instanceof DecimalType decimalCommand) {
                    lightEffect.setBrightness(decimalCommand.intValue());
                }
                break;
            case CHANNEL_FX_NAME:
                try {
                    lightEffect = lightEffect.setEffect(command.toString());
                } catch (Exception e) {
                    logger.warn("({}) could not load effect '{}'", uid, command);
                }

                break;
            default:
                logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command, channel);
        }
        setLightEffect(lightEffect);
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
        lightStripData.switchOnOff(on);
        connector.sendCommandAndQuery(lightStripData, true);
    }

    /**
     * Set Britghtness of device
     * 
     * @param newBrightness percentage 0-100 of new brightness
     */
    protected void setBrightness(Integer newBrightness) {
        /* switch off if 0 */
        if (newBrightness == 0) {
            lightStripData.switchOff();
        } else {
            lightStripData.switchOn();
            lightStripData.setBrightness(newBrightness);
        }
        connector.sendCommandAndQuery(lightStripData, true);
    }

    /**
     * Set Color of Device
     * 
     * @param command HSBType
     */
    protected void setColor(HSBType command) {
        lightStripData.switchOn();
        lightStripData.setHue(command.getHue().intValue());
        lightStripData.setSaturation(command.getSaturation().intValue());
        lightStripData.setBrightness(command.getBrightness().intValue());
        connector.sendCommandAndQuery(lightStripData, true);
    }

    /**
     * Set ColorTemp
     * 
     * @param colorTemp (Integer) in Kelvin
     */
    protected void setColorTemp(Integer colorTemp) {
        lightStripData.switchOn();
        lightStripData.setColorTemp(colorTemp);
        connector.sendCommandAndQuery(lightStripData, true);
    }

    /**
     * Set light effect
     * 
     * @param lightEffect TapoLightEffect
     */
    protected void setLightEffect(TapoLightEffect lightEffect) {
        connector.sendDeviceCommand(DEVICE_CMD_SET_LIGHT_FX, lightEffect);
    }

    /*****************************
     * UPDATE CHANNELS
     *****************************/

    protected void updateChannels(TapoLightStripData deviceData) {
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceData.isOn()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceData.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR_TEMP),
                getDecimalType(deviceData.getColorTemp()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR), deviceData.getHSB());
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceData.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceData.getOnTime(), Units.SECOND));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceData.isOverheated()));

        updateLightEffectChannels(deviceData);
    }

    /**
     * Update light effect channels
     */
    protected void updateLightEffectChannels(TapoLightStripData deviceData) {
        TapoLightEffect lightEffect = deviceData.getLightEffect();
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceData.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_NAME), getStringType(lightEffect.getName()));
    }
}
