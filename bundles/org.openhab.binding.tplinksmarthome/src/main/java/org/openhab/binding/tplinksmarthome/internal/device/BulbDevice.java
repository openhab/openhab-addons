/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNELS_BULB_SWITCH;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_BRIGHTNESS;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_COLOR_TEMPERATURE;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_COLOR_TEMPERATURE_ABS;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_ENERGY_POWER;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_SWITCH;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.binding.tplinksmarthome.internal.model.LightState;
import org.openhab.binding.tplinksmarthome.internal.model.TransitionLightStateResponse;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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

    public BulbDevice(final TPLinkSmartHomeThingType type) {
        this.colorTempMin = type.getColorScales().getWarm();
        this.colorTempMax = type.getColorScales().getCool();
        colorTempRangeFactor = (colorTempMax - colorTempMin) / 100;
    }

    @Override
    public String getUpdateCommand() {
        return Commands.getRealtimeBulbAndSysinfo();
    }

    @Override
    public boolean handleCommand(final ChannelUID channelUid, final Command command) throws IOException {
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

    private @Nullable HasErrorResponse handleOnOffType(final String channelID, final OnOffType onOff,
            final int transitionPeriod) throws IOException {
        if (CHANNELS_BULB_SWITCH.contains(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setLightState(onOff, transitionPeriod)));
        }
        return null;
    }

    private @Nullable HasErrorResponse handleDecimalType(final String channelID, final DecimalType command,
            final int transitionPeriod) throws IOException {
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

    private @Nullable TransitionLightStateResponse handleColorTemperature(final int colorTemperature,
            final int transitionPeriod) throws IOException {
        return commands.setTransitionLightStateResponse(
                connection.sendCommand(commands.setColorTemperature(colorTemperature, transitionPeriod)));
    }

    @Nullable
    private HasErrorResponse handleHSBType(final String channelID, final HSBType command, final int transitionPeriod)
            throws IOException {
        if (CHANNEL_COLOR.equals(channelID)) {
            return commands.setTransitionLightStateResponse(
                    connection.sendCommand(commands.setColor(command, transitionPeriod)));
        }
        return null;
    }

    @Override
    public State updateChannel(final ChannelUID channelUid, final DeviceState deviceState) {
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

    private int convertPercentageToKelvin(final int percentage) {
        return guardColorTemperature(colorTempMin + colorTempRangeFactor * percentage);
    }

    private int convertKelvinToPercentage(final int colorTemperature) {
        return (guardColorTemperature(colorTemperature) - colorTempMin) / colorTempRangeFactor;
    }

    private int guardColorTemperature(final int colorTemperature) {
        return Math.max(colorTempMin, Math.min(colorTempMax, colorTemperature));
    }
}
