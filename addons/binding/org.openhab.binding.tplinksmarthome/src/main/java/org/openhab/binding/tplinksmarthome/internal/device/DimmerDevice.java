/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.CHANNEL_BRIGHTNESS;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;

/**
 * TP-Link Smart Home device with a dimmer.
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
        return CHANNEL_BRIGHTNESS.equals(channelId) ? handleBrightness(channelId, connection, command, configuration)
                : super.handleCommand(channelId, connection, command, configuration);
    }

    private boolean handleBrightness(String channelId, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException {
        HasErrorResponse response = null;

        if (command instanceof OnOffType) {
            response = setOnOffState(connection, (OnOffType) command);
        } else if (command instanceof DecimalType) {
            DecimalType decimalCommand = (DecimalType) command;

            response = commands.setDimmerBrightnessResponse(
                    connection.sendCommand(commands.setDimmerBrightness((decimalCommand).intValue())));
            checkErrors(response);
            response = setOnOffState(connection, (OnOffType) decimalCommand.as(OnOffType.class));
        }
        checkErrors(response);
        return response != null;
    }

    @Override
    public State updateChannel(String channelId, DeviceState deviceState) {
        if (CHANNEL_BRIGHTNESS.equals(channelId)) {
            return deviceState.getSysinfo().getRelayState() == OnOffType.OFF ? DecimalType.ZERO
                    : new DecimalType(deviceState.getSysinfo().getBrightness());
        } else {
            return super.updateChannel(channelId, deviceState);
        }
    }
}
