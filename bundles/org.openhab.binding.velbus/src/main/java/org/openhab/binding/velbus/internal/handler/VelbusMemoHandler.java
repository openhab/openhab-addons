/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.packets.VelbusMemoTextPacket;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link VelbusMemoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public abstract class VelbusMemoHandler extends VelbusThermostatHandler {
    public static final int MEMO_TEXT_MAX_LENGTH = 63;

    private final ChannelUID memoChannel = new ChannelUID(thing.getUID(), "oledDisplay", "MEMO");

    public VelbusMemoHandler(Thing thing) {
        super(thing, 4, new ChannelUID(thing.getUID(), "input", "CH33"));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (channelUID.equals(memoChannel) && command instanceof StringType) {
            String memoText = ((StringType) command).toFullString();
            String trucatedMemoText = memoText.substring(0, Math.min(memoText.length(), MEMO_TEXT_MAX_LENGTH));
            String[] splittedMemoText = trucatedMemoText.split("(?<=\\G.....)");

            for (int i = 0; i < splittedMemoText.length; i++) {
                VelbusMemoTextPacket packet = new VelbusMemoTextPacket(getModuleAddress().getAddress(), (byte) (i * 5),
                        splittedMemoText[i].toCharArray());

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);

                // The last character must be zero
                if ((((i * 5) + 5) >= trucatedMemoText.length()) && (splittedMemoText[i].length() == 5)) {
                    packet = new VelbusMemoTextPacket(getModuleAddress().getAddress(), (byte) ((i + 1) * 5),
                            new char[0]);

                    packetBytes = packet.getBytes();
                    velbusBridgeHandler.sendPacket(packetBytes);
                }
            }
        }
    }
}
