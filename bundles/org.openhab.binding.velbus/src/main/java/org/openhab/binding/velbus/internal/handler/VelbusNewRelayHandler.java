/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusRelayPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusNewRelayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusNewRelayHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB4RYLD_20, THING_TYPE_VMB4RYNO_20));

    private boolean[] relayState = { false, false, false, false, false, false, false, false };
    private boolean firstRun = true;

    public VelbusNewRelayHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            VelbusStatusRequestPacket packet = new VelbusStatusRequestPacket(
                    getModuleAddress().getChannelIdentifier(channelUID));

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof OnOffType onOffCommand) {
            byte channel = Integer.valueOf(getModuleAddress().getChannelNumber(channelUID)).byteValue();
            byte commandByte = determineCommandByte(onOffCommand);

            VelbusRelayPacket packet = new VelbusRelayPacket(getModuleAddress(), channel, commandByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    private byte determineCommandByte(OnOffType command) {
        return (command == OnOffType.ON) ? COMMAND_SWITCH_RELAY_ON : COMMAND_SWITCH_RELAY_OFF;
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_RELAY_STATUS && packet.length >= 7) {
                byte address = packet[2];
                byte channelStatus = packet[5];
                byte channel = 0x01;

                for (int i = 0; i < 8; i++) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                    boolean on = (channelStatus & channel) != 0x00;
                    if (relayState[i] != on || firstRun) {
                        relayState[i] = on;
                        updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), OnOffType.from(on));
                    }
                    channel = (byte) (channel << 1);
                }
                firstRun = false;
            }
        }

        return true;
    }
}
