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
package org.openhab.binding.alarmdecoder.internal.handler;

import static org.openhab.binding.alarmdecoder.internal.AlarmDecoderBindingConstants.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.config.KeypadConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADAddress;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.IntCommandMap;
import org.openhab.binding.alarmdecoder.internal.protocol.KeypadMessage;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeypadHandler} is responsible for handling keypad messages.
 *
 * @author Bob Adair - Initial contribution
 * @author Bill Forsyth - Initial contribution
 */
@NonNullByDefault
public class KeypadHandler extends ADThingHandler {

    private static final Pattern VALID_COMMAND_PATTERN = Pattern.compile(ADCommand.KEYPAD_COMMAND_REGEX);

    private final Logger logger = LoggerFactory.getLogger(KeypadHandler.class);

    private KeypadConfig config = new KeypadConfig();
    private boolean singleAddress;
    private int sendingAddress;
    private @Nullable IntCommandMap intCommandMap;
    private @Nullable KeypadMessage previousMessage;
    private long addressMaskLong = 0;

    public KeypadHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(KeypadConfig.class);

        try {
            addressMaskLong = Long.parseLong(config.addressMask, 16);
        } catch (NumberFormatException e) {
            logger.debug("Number format exception parsing addressMask parameter: {}", e.getMessage());
            addressMaskLong = -1;
        }

        if (addressMaskLong < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid addressMask setting");
            return;
        }
        // If 1 and only 1 device is set in the addressMask parameter, use that device number as the sending address
        singleAddress = ADAddress.singleAddress(addressMaskLong);
        if (singleAddress) {
            ADAddress device = ADAddress.getDevice(addressMaskLong);
            if (device != null) {
                sendingAddress = device.deviceNum();
            }
        }

        try {
            intCommandMap = new IntCommandMap(config.commandMapping);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid commmandMapping parameter supplied. Error: {}.", e.getMessage());
            intCommandMap = null;
        }

        logger.debug("Keypad handler initializing for address mask {}", config.addressMask);

        initDeviceState();

        logger.trace("Keypad handler finished initializing");
    }

    @Override
    public void initChannelState() {
        previousMessage = null;
    }

    @Override
    public void notifyPanelReady() {
        // Do nothing
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IntCommandMap intCommandMap = this.intCommandMap;

        if (channelUID.getId().equals(CHANNEL_KP_COMMAND)) {
            if (command instanceof StringType commandString) {
                String cmd = commandString.toString();
                handleKeypadCommand(cmd);
            }
        } else if (channelUID.getId().equals(CHANNEL_KP_INTCOMMAND)) {
            if (command instanceof Number numberCommand) {
                int icmd = numberCommand.intValue();
                if (intCommandMap != null) {
                    String cmd = intCommandMap.getCommand(icmd);
                    if (cmd != null) {
                        handleKeypadCommand(cmd);
                    }
                }
            }
        }
    }

    private void handleKeypadCommand(String command) {
        String cmd = command;
        if (cmd.length() > 0) {
            if (!config.sendCommands) {
                logger.info("Sending keypad commands is disabled. Enable using the sendCommands keypad parameter.");
                return;
            }

            // check that received command is valid
            Matcher matcher = VALID_COMMAND_PATTERN.matcher(cmd);
            if (!matcher.matches()) {
                logger.info("Invalid characters in command. Ignoring command: {}", cmd);
                return;
            }

            // Replace A-H in command string with special key strings
            cmd = cmd.replace("A", ADCommand.SPECIAL_KEY_1);
            cmd = cmd.replace("B", ADCommand.SPECIAL_KEY_2);
            cmd = cmd.replace("C", ADCommand.SPECIAL_KEY_3);
            cmd = cmd.replace("D", ADCommand.SPECIAL_KEY_4);
            cmd = cmd.replace("E", ADCommand.SPECIAL_KEY_5);
            cmd = cmd.replace("F", ADCommand.SPECIAL_KEY_6);
            cmd = cmd.replace("G", ADCommand.SPECIAL_KEY_7);
            cmd = cmd.replace("H", ADCommand.SPECIAL_KEY_8);

            if (singleAddress) {
                sendCommand(ADCommand.addressedMessage(sendingAddress, cmd)); // Send from keypad address
            } else {
                sendCommand(new ADCommand(cmd)); // Send from AD address
            }
        }
    }

    @Override
    public void handleUpdate(ADMessage msg) {
        // This will ignore a received message unless it is a KeypadMessage and either this handler's address mask is 0
        // (all), the message's address mask is 0 (all), or any bits in this handler's address mask match bits set in
        // the message's address mask.
        if (!(msg instanceof KeypadMessage)) {
            return;
        }
        KeypadMessage kpMsg = (KeypadMessage) msg;

        long msgAddressMask = kpMsg.getLongAddressMask();

        if (!(((addressMaskLong & msgAddressMask) != 0) || addressMaskLong == 0 || msgAddressMask == 0)) {
            return;
        }
        logger.trace("Keypad handler for address mask {} received update: {}", config.addressMask, kpMsg);

        if (kpMsg.equals(previousMessage)) {
            return; // ignore repeated messages
        }

        if (config.sendStar) {
            if (kpMsg.alphaMessage.contains("Hit * for faults") || kpMsg.alphaMessage.contains("Press * to show faults")
                    || kpMsg.alphaMessage.contains("Press * Key")
                    || kpMsg.alphaMessage.contains("Press *  to show faults")) {
                logger.debug("Sending * command to show faults.");
                if (singleAddress) {
                    sendCommand(ADCommand.addressedMessage(sendingAddress, "*")); // Send from keypad address
                } else {
                    sendCommand(new ADCommand("*")); // send from AD address
                }
            }
        }

        updateState(CHANNEL_KP_ZONE, new DecimalType(kpMsg.getZone()));
        updateState(CHANNEL_KP_TEXT, new StringType(kpMsg.alphaMessage));

        updateState(CHANNEL_KP_READY, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_READY)));
        updateState(CHANNEL_KP_ARMEDAWAY, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_ARMEDAWAY)));
        updateState(CHANNEL_KP_ARMEDHOME, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_ARMEDHOME)));
        updateState(CHANNEL_KP_BACKLIGHT, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_BACKLIGHT)));
        updateState(CHANNEL_KP_PRORGAM, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_PRORGAM)));

        updateState(CHANNEL_KP_BEEPS, new DecimalType(kpMsg.nbeeps));

        updateState(CHANNEL_KP_BYPASSED, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_BYPASSED)));
        updateState(CHANNEL_KP_ACPOWER, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_ACPOWER)));
        updateState(CHANNEL_KP_CHIME, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_CHIME)));
        updateState(CHANNEL_KP_ALARMOCCURRED, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_ALARMOCCURRED)));
        updateState(CHANNEL_KP_ALARM, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_ALARM)));
        updateState(CHANNEL_KP_LOWBAT, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_LOWBAT)));
        updateState(CHANNEL_KP_DELAYOFF, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_DELAYOFF)));
        updateState(CHANNEL_KP_FIRE, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_FIRE)));
        updateState(CHANNEL_KP_SYSFAULT, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_SYSFAULT)));
        updateState(CHANNEL_KP_PERIMETER, OnOffType.from(kpMsg.getStatus(KeypadMessage.BIT_PERIMETER)));

        previousMessage = kpMsg;
    }
}
