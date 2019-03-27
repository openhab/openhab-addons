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

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.CHANNEL_BRIGHTNESS;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;

/**
 * TP-Link Smart Home device with a dimmer (HS220).
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DimmerDevice extends SwitchDevice {

    @Override
    protected @Nullable HasErrorResponse setOnOffState(Connection connection, OnOffType onOff) throws IOException {
        return commands.setSwitchStateResponse(connection.sendCommand(commands.setSwitchState(onOff)));
    }

    @Override
    public boolean handleCommand(String channelId, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException {
        return CHANNEL_BRIGHTNESS.equals(channelId)
                ? handleBrightnessChannel(channelId, connection, command, configuration)
                : super.handleCommand(channelId, connection, command, configuration);
    }

    /**
     * Handle the brightness channel. Because the device has different commands for setting the device on/off and
     * setting the brightness the on/off command must be send to the device as well when the brightness.
     *
     * @param connection Connection to use
     * @param command command to the send
     * @return returns true if the command was handled
     * @throws IOException throws an {@link IOException} if the command handling failed
     */
    private boolean handleBrightnessChannel(String channelId, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException {
        HasErrorResponse response = null;

        if (command instanceof OnOffType) {
            response = setOnOffState(connection, (OnOffType) command);
        } else if (command instanceof PercentType) {
            PercentType percentCommand = (PercentType) command;

            // Don't send value 0 as brightness value as it will give an error from the device.
            if (percentCommand.intValue() > 0) {
                response = commands.setDimmerBrightnessResponse(
                        connection.sendCommand(commands.setDimmerBrightness(percentCommand.intValue())));
            } else {
                response = setOnOffState(connection, OnOffType.OFF);
            }
        }
        checkErrors(response);
        return response != null;
    }

    @Override
    public State updateChannel(String channelId, DeviceState deviceState) {
        if (CHANNEL_BRIGHTNESS.equals(channelId)) {
            return deviceState.getSysinfo().getRelayState() == OnOffType.OFF ? PercentType.ZERO
                    : new PercentType(deviceState.getSysinfo().getBrightness());
        } else {
            return super.updateChannel(channelId, deviceState);
        }
    }
}
