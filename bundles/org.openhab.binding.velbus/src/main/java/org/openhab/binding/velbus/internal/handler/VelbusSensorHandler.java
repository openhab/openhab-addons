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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;
import org.openhab.binding.velbus.internal.packets.VelbusButtonPacket;
import org.openhab.binding.velbus.internal.packets.VelbusFeedbackLEDPacket;
import org.openhab.binding.velbus.internal.packets.VelbusPacket;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link VelbusSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 * @author Daniel Rosengarten - Add button simulation
 */
@NonNullByDefault
public class VelbusSensorHandler extends VelbusThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMB6IN, THING_TYPE_VMB8IR, THING_TYPE_VMB8PB));

    private static final StringType SET_LED = new StringType("SET_LED");
    private static final StringType SLOW_BLINK_LED = new StringType("SLOW_BLINK_LED");
    private static final StringType FAST_BLINK_LED = new StringType("FAST_BLINK_LED");
    private static final StringType VERY_FAST_BLINK_LED = new StringType("VERY_FAST_BLINK_LED");
    private static final StringType CLEAR_LED = new StringType("CLEAR_LED");

    private static final StringType PRESSED = new StringType("PRESSED");
    private static final StringType LONG_PRESSED = new StringType("LONG_PRESSED");

    public VelbusSensorHandler(Thing thing) {
        this(thing, 0);
    }

    public VelbusSensorHandler(Thing thing, int numberOfSubAddresses) {
        super(thing, numberOfSubAddresses);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        VelbusBridgeHandler velbusBridgeHandler = getVelbusBridgeHandler();
        if (velbusBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (isFeedbackChannel(channelUID) && command instanceof StringType) {
            byte commandByte;

            StringType stringTypeCommand = (StringType) command;
            if (stringTypeCommand.equals(SET_LED)) {
                commandByte = COMMAND_SET_LED;
            } else if (stringTypeCommand.equals(SLOW_BLINK_LED)) {
                commandByte = COMMAND_SLOW_BLINK_LED;
            } else if (stringTypeCommand.equals(FAST_BLINK_LED)) {
                commandByte = COMMAND_FAST_BLINK_LED;
            } else if (stringTypeCommand.equals(VERY_FAST_BLINK_LED)) {
                commandByte = COMMAND_VERY_FAST_BLINK_LED;
            } else if (stringTypeCommand.equals(CLEAR_LED)) {
                commandByte = COMMAND_CLEAR_LED;
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
            }

            VelbusFeedbackLEDPacket packet = new VelbusFeedbackLEDPacket(
                    getModuleAddress().getChannelIdentifier(channelUID), commandByte);

            byte[] packetBytes = packet.getBytes();
            velbusBridgeHandler.sendPacket(packetBytes);
        }

        if (isButtonChannel(channelUID) && command instanceof StringType) {
            StringType stringTypeCommand = (StringType) command;

            if (stringTypeCommand.equals(PRESSED) || stringTypeCommand.equals(LONG_PRESSED)) {
                VelbusButtonPacket packet = new VelbusButtonPacket(getModuleAddress().getChannelIdentifier(channelUID));

                packet.Pressed();
                velbusBridgeHandler.sendPacket(packet.getBytes());
                triggerChannel("input#CH" + getModuleAddress().getChannelNumber(channelUID),
                        CommonTriggerEvents.PRESSED);

                if (stringTypeCommand.equals(LONG_PRESSED)) {
                    packet.LongPressed();
                    velbusBridgeHandler.sendPacket(packet.getBytes());
                    triggerChannel("input#CH" + getModuleAddress().getChannelNumber(channelUID),
                            CommonTriggerEvents.LONG_PRESSED);
                }

                packet.Released();
                velbusBridgeHandler.sendPacket(packet.getBytes());
                triggerChannel("input#CH" + getModuleAddress().getChannelNumber(channelUID),
                        CommonTriggerEvents.RELEASED);
            } else {
                throw new UnsupportedOperationException(
                        "The command '" + command + "' is not supported on channel '" + channelUID + "'.");
            }
        }
    }

    private boolean isFeedbackChannel(ChannelUID channelUID) {
        return "feedback".equals(channelUID.getGroupId());
    }

    private boolean isButtonChannel(ChannelUID channelUID) {
        return "button".equals(channelUID.getGroupId());
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
                    triggerChannel("input#" + getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.PRESSED);
                }

                byte channelJustReleased = packet[6];
                if (channelJustReleased != 0) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address,
                            channelJustReleased);
                    triggerChannel("input#" + getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.RELEASED);
                }

                byte channelLongPressed = packet[7];
                if (channelLongPressed != 0) {
                    VelbusChannelIdentifier velbusChannelIdentifier = new VelbusChannelIdentifier(address,
                            channelLongPressed);
                    triggerChannel("input#" + getModuleAddress().getChannelId(velbusChannelIdentifier),
                            CommonTriggerEvents.LONG_PRESSED);
                }
            }
        }
    }
}
