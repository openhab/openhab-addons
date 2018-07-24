/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;

/**
 * TP-Link Smart Home device with a switch, like Smart Plugs and Switches.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class SwitchDevice extends SmartHomeDevice {

    @Override
    public String getUpdateCommand() {
        return Commands.getSysinfo();
    }

    @Override
    public boolean handleCommand(String channelID, Connection connection, Command command,
            TPLinkSmartHomeConfiguration configuration) throws IOException {
        return command instanceof OnOffType && handleOnOffType(channelID, connection, (OnOffType) command);
    }

    /**
     * Returns the switch state.
     *
     * @param deviceState data object containing the state
     * @return the switch state
     */
    protected State getOnOffState(DeviceState deviceState) {
        return deviceState.getSysinfo().getRelayState();
    }

    private boolean handleOnOffType(String channelID, Connection connection, OnOffType onOff) throws IOException {
        HasErrorResponse response = null;

        if (CHANNEL_SWITCH.equals(channelID)) {
            response = setOnOffState(connection, onOff);
        } else if (CHANNEL_LED.equals(channelID)) {
            response = commands.setLedOnResponse(connection.sendCommand(commands.setLedOn(onOff)));
        }
        checkErrors(response);
        return response != null;
    }

    /**
     * Sends the {@link OnOffType} command to the device and returns the returned answer.
     *
     * @param connection Connection to use
     * @param onOff command to the send
     * @return state returned by the device
     */
    protected @Nullable HasErrorResponse setOnOffState(Connection connection, OnOffType onOff) throws IOException {
        return commands.setRelayStateResponse(connection.sendCommand(commands.setRelayState(onOff)));
    }

    @Override
    public State updateChannel(String channelId, DeviceState deviceState) {
        if (CHANNEL_SWITCH.equals(channelId)) {
            return getOnOffState(deviceState);
        } else if (CHANNEL_LED.equals(channelId)) {
            return deviceState.getSysinfo().getLedOff();
        }
        return UnDefType.UNDEF;
    }
}
