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

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.model.HasErrorResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
    public boolean handleCommand(ChannelUID channelUid, Command command) throws IOException {
        return command instanceof OnOffType onOffCommand && handleOnOffType(channelUid, onOffCommand);
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

    private boolean handleOnOffType(ChannelUID channelUid, OnOffType onOff) throws IOException {
        final HasErrorResponse response;
        final String baseChannelId = getBaseChannel(channelUid);

        if (CHANNEL_SWITCH.contentEquals(baseChannelId)) {
            response = setOnOffState(channelUid, onOff);
        } else if (CHANNEL_LED.contentEquals(baseChannelId)) {
            response = commands
                    .setLedOnResponse(connection.sendCommand(commands.setLedOn(onOff, getChildId(channelUid))));
        } else {
            response = null;
        }
        checkErrors(response);
        return response != null;
    }

    /**
     * Sends the {@link OnOffType} command to the device and returns the returned answer.
     *
     * @param channelUid channel Id to use to determine child id
     * @param onOff command to the send
     * @return state returned by the device
     * @throws IOException exception in case device not reachable
     */
    protected @Nullable HasErrorResponse setOnOffState(ChannelUID channelUid, OnOffType onOff) throws IOException {
        return commands
                .setRelayStateResponse(connection.sendCommand(commands.setRelayState(onOff, getChildId(channelUid))));
    }

    @Override
    public State updateChannel(ChannelUID channelUid, DeviceState deviceState) {
        final String baseChannelId = getBaseChannel(channelUid);

        if (CHANNEL_SWITCH.equals(baseChannelId)) {
            return getOnOffState(deviceState);
        } else if (CHANNEL_LED.equals(baseChannelId)) {
            return deviceState.getSysinfo().getLedOff();
        }
        return UnDefType.UNDEF;
    }

    /**
     * Returns the child Id for the given channel if the device supports children and it's a channel for a specific
     * child.
     *
     * @param channelUid channel Id to get the child id for
     * @return null or child id
     */
    protected @Nullable String getChildId(ChannelUID channelUid) {
        return null;
    }

    private String getBaseChannel(ChannelUID channelUid) {
        return channelUid.isInGroup() ? channelUid.getIdWithoutGroup() : channelUid.getId();
    }
}
