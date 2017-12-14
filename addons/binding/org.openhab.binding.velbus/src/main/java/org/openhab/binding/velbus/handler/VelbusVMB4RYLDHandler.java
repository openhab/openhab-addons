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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusRelayPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;

/**
 * The {@link VelbusVMB4RYLDHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusVMB4RYLDHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB4RYLD));

    public VelbusVMB4RYLDHandler(Thing thing) {
        super(thing, 5, 0, "Switch");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            logger.warn("Velbus bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (command instanceof RefreshType) {
            VelbusStatusRequestPacket packet = new VelbusStatusRequestPacket(
                    getModuleAddress().getChannelIdentifier(channelUID));

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof OnOffType) {
            byte commandByte = determineCommandByte((OnOffType) command);

            VelbusRelayPacket packet = new VelbusRelayPacket(getModuleAddress().getChannelIdentifier(channelUID),
                    commandByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.info("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    private byte determineCommandByte(OnOffType command) {
        return (command == OnOffType.ON) ? COMMAND_SWITCH_RELAY_ON : COMMAND_SWITCH_RELAY_OFF;
    }

    @SuppressWarnings("null")
    @Override
    public void onPacketReceived(byte[] packet) {
        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_RELAY_STATUS && packet.length >= 7) {
                byte address = packet[2];
                byte channel = packet[5];
                boolean on = packet[7] != 0x00;

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                OnOffType state = on ? OnOffType.ON : OnOffType.OFF;
                updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), state);
            }
        }
    }
}
