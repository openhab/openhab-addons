/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusVMBGPOHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBGPOHandler extends VelbusMemoHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBGPO, THING_TYPE_VMBGPOD, THING_TYPE_VMBGPOD_2));

    public static final int MODULESETTINGS_MEMORY_ADDRESS = 0x02F0;
    public static final int LAST_MEMORY_LOCATION_ADDRESS = 0x1A03;

    private final ChannelUID screensaverChannel = new ChannelUID(thing.getUID(), "oledDisplay", "SCREENSAVER");

    private byte moduleSettings;

    public VelbusVMBGPOHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (channelUID.equals(screensaverChannel) && command instanceof RefreshType) {
            sendReadMemoryPacket(velbusBridgeHandler, MODULESETTINGS_MEMORY_ADDRESS);
        }

        if (channelUID.equals(screensaverChannel) && command instanceof OnOffType) {
            byte screenSaverOnOffByte = (byte) ((command == OnOffType.ON) ? 0x80 : 0x00);
            moduleSettings = (byte) (screenSaverOnOffByte | (moduleSettings & 0x7F));
            sendWriteMemoryPacket(velbusBridgeHandler, MODULESETTINGS_MEMORY_ADDRESS, moduleSettings);
            sendWriteMemoryPacket(velbusBridgeHandler, LAST_MEMORY_LOCATION_ADDRESS, (byte) 0xFF);
        }
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        super.onPacketReceived(packet);

        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if ((command == COMMAND_MEMORY_DATA_BLOCK && packet.length >= 11)
                    || (command == COMMAND_MEMORY_DATA && packet.length >= 8)) {
                byte highMemoryAddress = packet[5];
                byte lowMemoryAddress = packet[6];
                int memoryAddress = ((highMemoryAddress & 0xff) << 8) | (lowMemoryAddress & 0xff);
                byte[] data = (command == COMMAND_MEMORY_DATA_BLOCK)
                        ? new byte[] { packet[7], packet[8], packet[9], packet[10] }
                        : new byte[] { packet[7] };

                for (int i = 0; i < data.length; i++) {

                    if ((memoryAddress + i) == MODULESETTINGS_MEMORY_ADDRESS) {
                        this.moduleSettings = data[i];
                        OnOffType state = OnOffType.from((this.moduleSettings & 0x80) != 0x00);
                        updateState(screensaverChannel, state);
                    }
                }
            }
        }
    }
}
