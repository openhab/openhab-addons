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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.VelbusFirstGenerationDeviceModuleAddress;
import org.openhab.binding.velbus.internal.VelbusModuleAddress;
import org.openhab.binding.velbus.internal.config.VelbusThingConfig;
import org.openhab.binding.velbus.internal.packets.VelbusBlindOffPacket;
import org.openhab.binding.velbus.internal.packets.VelbusBlindPositionPacket;
import org.openhab.binding.velbus.internal.packets.VelbusBlindUpDownPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.binding.velbus.internal.packets.VelbusStatusRequestPacket;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VelbusBlindsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusBlindsHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB1BL, THING_TYPE_VMB1BLS, THING_TYPE_VMB2BL, THING_TYPE_VMB2BLE));

    public VelbusBlindsHandler(Thing thing) {
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
        } else if (command instanceof UpDownType upDownCommand) {
            if (upDownCommand == UpDownType.UP) {
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
        } else if (command instanceof StopMoveType stopMoveCommand) {
            if (stopMoveCommand == StopMoveType.STOP) {
                VelbusBlindOffPacket packet = new VelbusBlindOffPacket(
                        getModuleAddress().getChannelIdentifier(channelUID));

                byte[] packetBytes = packet.getBytes();
                velbusBridgeHandler.sendPacket(packetBytes);
            }
        } else if (command instanceof PercentType percentCommand) {
            VelbusBlindPositionPacket packet = new VelbusBlindPositionPacket(
                    getModuleAddress().getChannelIdentifier(channelUID), percentCommand.byteValue());

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        } else {
            logger.debug("The command '{}' is not supported by this handler.", command.getClass());
        }
    }

    @Override
    protected @Nullable VelbusModuleAddress createVelbusModuleAddress(int numberOfSubAddresses) {
        final VelbusThingConfig velbusThingConfig = this.velbusThingConfig;
        if (velbusThingConfig != null) {
            byte address = hexToByte(velbusThingConfig.address);

            if (isFirstGenerationDevice()) {
                return new VelbusFirstGenerationDeviceModuleAddress(address);
            }

            return new VelbusModuleAddress(address, numberOfSubAddresses);
        }

        return null;
    }

    private Boolean isFirstGenerationDevice() {
        ThingTypeUID thingTypeUID = this.getThing().getThingTypeUID();
        return thingTypeUID.equals(THING_TYPE_VMB1BL) || thingTypeUID.equals(THING_TYPE_VMB2BL);
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
