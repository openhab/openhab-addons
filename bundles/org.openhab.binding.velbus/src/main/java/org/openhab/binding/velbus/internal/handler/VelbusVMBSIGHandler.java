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
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link VelbusVMBSIGHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVMBSIGHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBSIG, THING_TYPE_VMBSIG_20, THING_TYPE_VMBSIG_21));

    private final ChannelUID statusChannel = new ChannelUID(thing.getUID(), CHANNEL_STATUS);

    private static final StringType UNKNOWN = new StringType("UNKNOWN");
    private static final StringType NORMAL = new StringType("NORMAL");
    private static final StringType BUS_ERROR = new StringType("BUS_ERROR");
    private static final StringType LOW_POWER = new StringType("LOW_POWER");
    private static final StringType HIGH_POWER = new StringType("HIGH_POWER");

    public VelbusVMBSIGHandler(Thing thing) {
        this(thing, 0);
    }

    public VelbusVMBSIGHandler(Thing thing, int numberOfSubAddresses) {
        super(thing, numberOfSubAddresses);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
    }

    @Override
    public boolean onPacketReceived(byte[] packet) {
        if (!super.onPacketReceived(packet)) {
            return false;
        }

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte command = packet[4];

            if (command == COMMAND_MODULE_STATUS && packet.length >= 7) {
                byte status = packet[6];

                switch (status) {
                    case 0x00:
                        updateState(statusChannel, NORMAL);
                        break;
                    case 0x01:
                        updateState(statusChannel, BUS_ERROR);
                        break;
                    case 0x02:
                        updateState(statusChannel, LOW_POWER);
                        break;
                    case 0x03:
                        updateState(statusChannel, HIGH_POWER);
                        break;
                    default:
                        updateState(statusChannel, UNKNOWN);
                }
            }
        }

        return true;
    }
}
