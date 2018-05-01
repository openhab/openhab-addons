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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusDimmerPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;

/**
 * The {@link VelbusDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusDimmerHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB1DM,
            THING_TYPE_VMB1LED, THING_TYPE_VMB4DC, THING_TYPE_VMBDME, THING_TYPE_VMBDMI, THING_TYPE_VMBDMIR));

    public VelbusDimmerHandler(Thing thing) {
        super(thing, 0, "Dimmer");
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
        } else if (command instanceof PercentType) {
            byte commandByte = COMMAND_SET_DIMVALUE;

            VelbusDimmerPacket packet = new VelbusDimmerPacket(getModuleAddress().getChannelIdentifier(channelUID),
                    commandByte, ((PercentType) command).byteValue());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof OnOffType) {
            byte commandByte = determineCommandByte((OnOffType) command);

            VelbusDimmerPacket packet = new VelbusDimmerPacket(getModuleAddress().getChannelIdentifier(channelUID),
                    commandByte, (byte) 0x00);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    private byte determineCommandByte(OnOffType command) {
        return (command == OnOffType.ON) ? COMMAND_RESTORE_LAST_DIMVALUE : COMMAND_SET_DIMVALUE;
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if ((command == COMMAND_DIMMERCONTROLLER_STATUS || command == COMMAND_DIMMER_STATUS)
                    && packet.length >= 7) {
                byte address = packet[2];
                byte channel = packet[5];
                byte dimvalue = packet[7];

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                PercentType state = new PercentType(dimvalue);
                updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), state);
            }
        }
    }
}
