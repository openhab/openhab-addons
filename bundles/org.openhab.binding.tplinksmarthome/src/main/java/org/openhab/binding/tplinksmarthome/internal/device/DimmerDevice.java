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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_BRIGHTNESS;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * TP-Link Smart Home device with a dimmer (HS220).
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DimmerDevice extends SwitchDevice {

    @Override
    protected @Nullable HasErrorResponse setOnOffState(ChannelUID channelUid, OnOffType onOff) throws IOException {
        return commands.setSwitchStateResponse(connection.sendCommand(commands.setSwitchState(onOff)));
    }

    @Override
    public boolean handleCommand(ChannelUID channelUid, Command command) throws IOException {
        return CHANNEL_BRIGHTNESS.equals(channelUid.getId()) ? handleBrightnessChannel(channelUid, command)
                : super.handleCommand(channelUid, command);
    }

    /**
     * Handle the brightness channel. Because the device has different commands for setting the device on/off and
     * setting the brightness the on/off command must be send to the device as well when the brightness.
     *
     * @param channelUid uid of the channel to handle
     * @param command command to the send
     * @return returns true if the command was handled
     * @throws IOException throws an {@link IOException} if the command handling failed
     */
    private boolean handleBrightnessChannel(ChannelUID channelUid, Command command) throws IOException {
        HasErrorResponse response = null;

        if (command instanceof OnOffType onOffCommand) {
            response = setOnOffState(channelUid, onOffCommand);
        } else if (command instanceof PercentType percentCommand) {
            // Don't send value 0 as brightness value as it will give an error from the device.
            if (percentCommand.intValue() > 0) {
                response = commands.setDimmerBrightnessResponse(
                        connection.sendCommand(commands.setDimmerBrightness(percentCommand.intValue())));
                checkErrors(response);
                if (response == null) {
                    return false;
                }
                response = setOnOffState(channelUid, OnOffType.ON);
            } else {
                response = setOnOffState(channelUid, OnOffType.OFF);
            }
        }
        checkErrors(response);
        return response != null;
    }

    @Override
    public State updateChannel(ChannelUID channelUid, DeviceState deviceState) {
        if (CHANNEL_BRIGHTNESS.equals(channelUid.getId())) {
            return deviceState.getSysinfo().getRelayState() == OnOffType.OFF ? PercentType.ZERO
                    : new PercentType(deviceState.getSysinfo().getBrightness());
        } else {
            return super.updateChannel(channelUid, deviceState);
        }
    }
}
