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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;

/**
 * The {@link VelbusSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusSensorHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB2PBN, THING_TYPE_VMB6IN, THING_TYPE_VMB6PBN, THING_TYPE_VMB7IN,
                    THING_TYPE_VMB8IR, THING_TYPE_VMB8PB, THING_TYPE_VMB8PBU, THING_TYPE_VMBPIRC, THING_TYPE_VMBPIRM));

    public VelbusSensorHandler(Thing thing) {
        this(thing, 0);
    }

    public VelbusSensorHandler(Thing thing, int numberOfSubAddresses) {
        super(thing, numberOfSubAddresses, null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte address = packet[2];
            byte command = packet[4];

            if (command == COMMAND_PUSH_BUTTON_STATUS && packet.length >= 6) {
                byte channelJustPressed = packet[5];
                if (channelJustPressed != 0) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address,
                            channelJustPressed);
                    triggerChannel(getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.PRESSED);
                }

                byte channelJustReleased = packet[6];
                if (channelJustReleased != 0) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address,
                            channelJustReleased);
                    triggerChannel(getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.RELEASED);
                }

                byte channelLongPressed = packet[7];
                if (channelLongPressed != 0) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address,
                            channelLongPressed);
                    triggerChannel(getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.LONG_PRESSED);
                }
            }
        }
    }
}
