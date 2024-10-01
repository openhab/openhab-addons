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
 * The {@link VelbusVMBELHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 * @author Daniel Rosengarten - Add VMBELPIR support, add output support
 */
@NonNullByDefault
public class VelbusVMBELHandler extends VelbusThermostatHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBEL1, THING_TYPE_VMBEL2, THING_TYPE_VMBEL4, THING_TYPE_VMBELPIR,
                    THING_TYPE_VMBEL1_20, THING_TYPE_VMBEL2_20, THING_TYPE_VMBEL4_20, THING_TYPE_VMBEL4PIR_20));

    private final ChannelUID outputChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_OUTPUT, CHANNEL_OUTPUT);

    public VelbusVMBELHandler(Thing thing) {
        super(thing, 4, new ChannelUID(thing.getUID(), CHANNEL_GROUP_INPUT, "CH9"));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            VelbusStatusRequestPacket packet = new VelbusStatusRequestPacket(getModuleAddress().getAddress());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof OnOffType commandAsOnOffType) {
            byte commandByte = determineCommandByte(commandAsOnOffType);

            VelbusRelayPacket packet = new VelbusRelayPacket(getModuleAddress(), commandByte);

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

            if (command == COMMAND_MODULE_STATUS && packet.length >= 7) {
                boolean on = (packet[7] & (byte) 0x80) == (byte) 0x80;

                OnOffType state = on ? OnOffType.ON : OnOffType.OFF;
                updateState(outputChannel, state);
            }
        }

        return true;
    }
}
