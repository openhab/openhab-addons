/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.handler;

import static org.openhab.binding.velbus.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusBlindOffPacket;
import org.openhab.binding.velbus.internal.packets.VelbusBlindPositionPacket;
import org.openhab.binding.velbus.internal.packets.VelbusBlindUpDownPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;

/**
 * The {@link VelbusBlindsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBlindsHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB1BLS, THING_TYPE_VMB2BLE));

    public VelbusBlindsHandler(Thing thing) {
        super(thing, 0, "Rollershutter");
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
        } else if (command instanceof UpDownType) {
            UpDownType s = (UpDownType) command;
            if (s == UpDownType.UP) {
                byte commandByte = COMMAND_BLIND_UP;

                VelbusBlindUpDownPacket packet = new VelbusBlindUpDownPacket(
                        getModuleAddress().getChannelIdentifier(channelUID), commandByte);

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            } else {
                byte commandByte = COMMAND_BLIND_DOWN;

                VelbusBlindUpDownPacket packet = new VelbusBlindUpDownPacket(
                        getModuleAddress().getChannelIdentifier(channelUID), commandByte);

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            }
        } else if (command instanceof StopMoveType) {
            StopMoveType s = (StopMoveType) command;
            if (s == StopMoveType.STOP) {
                VelbusBlindOffPacket packet = new VelbusBlindOffPacket(
                        getModuleAddress().getChannelIdentifier(channelUID));

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            }
        } else if (command instanceof PercentType) {
            VelbusBlindPositionPacket packet = new VelbusBlindPositionPacket(
                    getModuleAddress().getChannelIdentifier(channelUID), ((PercentType) command).byteValue());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_BLIND_STATUS && packet.length >= 9) {
                byte address = packet[2];
                byte channel = packet[5];
                byte blindPosition = packet[9];

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                PercentType state = new PercentType(blindPosition);
                updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), state);
            }
        }
    }
}
