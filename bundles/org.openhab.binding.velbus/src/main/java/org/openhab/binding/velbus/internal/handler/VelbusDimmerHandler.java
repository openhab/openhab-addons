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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.config.VelbusDimmerConfig;
import org.openhab.binding.velbus.internal.packets.VelbusDimmerPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusDimmerHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB1DM,
            THING_TYPE_VMB1LED, THING_TYPE_VMB4DC, THING_TYPE_VMBDME, THING_TYPE_VMBDMI, THING_TYPE_VMBDMIR));

    private @NonNullByDefault({}) VelbusDimmerConfig dimmerConfig;

    public VelbusDimmerHandler(Thing thing) {
        super(thing, 0);
    }

    @Override
    public void initialize() {
        this.dimmerConfig = getConfigAs(VelbusDimmerConfig.class);

        super.initialize();
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
            byte commandByte = COMMAND_SET_VALUE;

            VelbusDimmerPacket packet = new VelbusDimmerPacket(getModuleAddress().getChannelIdentifier(channelUID),
                    commandByte, ((PercentType) command).byteValue(), this.dimmerConfig.dimspeed,
                    isFirstGenerationDevice());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else if (command instanceof OnOffType) {
            byte commandByte = determineCommandByte((OnOffType) command);

            VelbusDimmerPacket packet = new VelbusDimmerPacket(getModuleAddress().getChannelIdentifier(channelUID),
                    commandByte, (byte) 0x00, this.dimmerConfig.dimspeed, isFirstGenerationDevice());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    private byte determineCommandByte(OnOffType command) {
        return (command == OnOffType.ON) ? COMMAND_RESTORE_LAST_DIMVALUE : COMMAND_SET_VALUE;
    }

    private Boolean isFirstGenerationDevice() {
        ThingTypeUID thingTypeUID = this.getThing().getThingTypeUID();
        return thingTypeUID.equals(THING_TYPE_VMB1DM) || thingTypeUID.equals(THING_TYPE_VMB1LED)
                || thingTypeUID.equals(THING_TYPE_VMBDME);
    }

    @Override
    public void onPacketReceived(byte[] packet) {
        logger.trace("onPacketReceived() was called");

        if (packet[0] == VelbusPacket.STX && packet.length >= 5) {
            byte address = packet[2];
            byte command = packet[4];

            if ((command == COMMAND_DIMMERCONTROLLER_STATUS) && packet.length >= 8) {
                byte channel = packet[5];
                byte dimValue = packet[7];

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, channel);
                PercentType state = new PercentType(dimValue);
                updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), state);
            } else if ((command == COMMAND_DIMMER_STATUS) && packet.length >= 7) {
                byte dimValue = packet[6];

                VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address, (byte) 0x01);
                PercentType state = new PercentType(dimValue);
                updateState(getModuleAddress().getChannelId(velbusChannelIdentifier), state);
            }
        }
    }
}
