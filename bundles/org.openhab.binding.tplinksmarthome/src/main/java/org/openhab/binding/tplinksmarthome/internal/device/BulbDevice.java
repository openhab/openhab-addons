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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tplinksmarthome.internal.Commands;
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
    public boolean handleCommand(ChannelUID channelUid, Command command) throws IOException {
        final String channelId = channelUid.getId();
        final int transitionPeriod = configuration.transitionPeriod;
        final HasErrorResponse response;

        if (command instanceof OnOffType) {
            response = handleOnOffType(channelId, (OnOffType) command, transitionPeriod);
        } else if (command instanceof HSBType) {
            response = handleHSBType(channelId, (HSBType) command, transitionPeriod);
        } else if (command instanceof DecimalType) {
            response = handleDecimalType(channelId, (DecimalType) command, transitionPeriod);
        } else {
            return false;
        }
        checkErrors(response);
        return response != null;
    }

    private @Nullable HasErrorResponse handleOnOffType(String channelID, OnOffType onOff, int transitionPeriod)
            throws IOException {
        if (CHANNELS_BULB_SWITCH.contains(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setLightState(onOff, transitionPeriod)));
        }
        return null;
    }

    private @Nullable HasErrorResponse handleDecimalType(String channelID, DecimalType command, int transitionPeriod)
            throws IOException {
        if (CHANNEL_COLOR.equals(channelID) || CHANNEL_BRIGHTNESS.equals(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setBrightness(command.intValue(), transitionPeriod)));
        } else if (CHANNEL_COLOR_TEMPERATURE.equals(channelID)) {
            return handleColorTemperature(convertPercentageToKelvin(command.intValue()), transitionPeriod);
        } else if (CHANNEL_COLOR_TEMPERATURE_ABS.equals(channelID)) {
            return handleColorTemperature(guardColorTemperature(command.intValue()), transitionPeriod);
        }
        return null;
    }

    private @Nullable TransitionLightStateResponse handleColorTemperature(int colorTemperature, int transitionPeriod)
            throws IOException {
        return commands.setTransitionLightStateResponse(
                connection.sendCommand(commands.setColorTemperature(colorTemperature, transitionPeriod)));
    }

    @Nullable
    private HasErrorResponse handleHSBType(String channelID, HSBType command, int transitionPeriod) throws IOException {
        if (CHANNEL_COLOR.equals(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setColor(command, transitionPeriod)));
        }
        return null;
    }

    @Override
    public State updateChannel(ChannelUID channelUid, DeviceState deviceState) {
        final LightState lightState = deviceState.getSysinfo().getLightState();
        final State state;

        switch (channelUid.getId()) {
            case CHANNEL_BRIGHTNESS:
                state = lightState.getBrightness();
                break;
            case CHANNEL_COLOR:
                state = new HSBType(lightState.getHue(), lightState.getSaturation(), lightState.getBrightness());
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                state = new PercentType(convertKelvinToPercentage(lightState.getColorTemp()));
                break;
            case CHANNEL_COLOR_TEMPERATURE_ABS:
                state = new DecimalType(guardColorTemperature(lightState.getColorTemp()));
                break;
            case CHANNEL_SWITCH:
                state = lightState.getOnOff();
                break;
            case CHANNEL_ENERGY_POWER:
                state = new DecimalType(deviceState.getRealtime().getPower());
                break;
            default:
                state = UnDefType.UNDEF;
                break;
        }
        return state;
    }

    private int convertPercentageToKelvin(int percentage) {
        return guardColorTemperature(colorTempMin + colorTempRangeFactor * percentage);
    }

    private int convertKelvinToPercentage(int colorTemperature) {
        return (guardColorTemperature(colorTemperature) - colorTempMin) / colorTempRangeFactor;
    }

    private int guardColorTemperature(int colorTemperature) {
        return Math.max(colorTempMin, Math.min(colorTempMax, colorTemperature));
    }
}
