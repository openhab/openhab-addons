/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.LightState;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightStateResponse;

/**
 * TP-Link Smart Home Light Bulb.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class BulbDevice extends SmartHomeDevice {

    protected Commands commands = new Commands();

    private final int colorTempMin;
    private final int colorTempMax;
    private final int colorTempRangeFactor;

    public BulbDevice(ThingTypeUID thingTypeUID) {
        this(thingTypeUID, 0, 0);
    }

    public BulbDevice(ThingTypeUID thingTypeUID, int colorTempMin, int colorTempMax) {
        this.colorTempMin = colorTempMin;
        this.colorTempMax = colorTempMax;
        colorTempRangeFactor = (colorTempMax - colorTempMin) / 100;
    }

    @Override
    public String getUpdateCommand() {
        return Commands.getRealtimeBulbAndSysinfo();
    }

    @Override
    public boolean handleCommand(String channelID, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException {
        int transitionPeriod = configuration.transitionPeriod;
        HasErrorResponse response;

        if (command instanceof OnOffType) {
            response = handleOnOffType(channelID, connection, (OnOffType) command, transitionPeriod);
        } else if (command instanceof HSBType) {
            response = handleHSBType(channelID, connection, (HSBType) command, transitionPeriod);
        } else if (command instanceof DecimalType) {
            response = handleDecimalType(channelID, connection, (DecimalType) command, transitionPeriod);
        } else {
            return false;
        }
        checkErrors(response);
        return response != null;
    }

    @Nullable
    private HasErrorResponse handleOnOffType(String channelID, Connection connection, OnOffType onOff,
            int transitionPeriod) throws IOException {
        if (CHANNELS_BULB_SWITCH.contains(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setLightState(onOff, transitionPeriod)));
        }
        return null;
    }

    @Nullable
    private HasErrorResponse handleDecimalType(String channelID, Connection connection, DecimalType command,
            int transitionPeriod) throws IOException {
        if (CHANNEL_COLOR.equals(channelID) || CHANNEL_BRIGHTNESS.equals(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setBrightness(command.intValue(), transitionPeriod)));
        } else if (CHANNEL_COLOR_TEMPERATURE.equals(channelID)) {
            return handleColorTemperature(connection, convertPercentageToKelvin(command.intValue()), transitionPeriod);
        }
        return null;
    }

    private int convertPercentageToKelvin(int percentage) {
        return Math.max(colorTempMin, Math.min(colorTempMax, colorTempMin + colorTempRangeFactor * percentage));
    }

    private TransitionLightStateResponse handleColorTemperature(Connection connection, int colorTemperature,
            int transitionPeriod) throws IOException {
        return commands.setTransitionLightStateResponse(
                connection.sendCommand(commands.setColorTemperature(colorTemperature, transitionPeriod)));
    }

    @Nullable
    private HasErrorResponse handleHSBType(String channelID, Connection connection, HSBType command,
            int transitionPeriod) throws IOException {
        if (CHANNEL_COLOR.equals(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setColor(command, transitionPeriod)));
        }
        return null;
    }

    @Override
    public State updateChannel(String channelId, DeviceState deviceState) {
        LightState lightState = deviceState.getSysinfo().getLightState();
        final State state;

        switch (channelId) {
            case CHANNEL_BRIGHTNESS:
                state = lightState.getBrightness();
                break;
            case CHANNEL_COLOR:
                state = new HSBType(lightState.getHue(), lightState.getSaturation(), lightState.getBrightness());
                break;
            case CHANNEL_SWITCH:
                state = lightState.getOnOff();
                break;
            default:
                state = UnDefType.UNDEF;
                break;
        }
        return state;
    }
}
