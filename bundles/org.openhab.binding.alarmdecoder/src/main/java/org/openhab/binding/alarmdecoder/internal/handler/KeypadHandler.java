/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.alarmdecoder.internal.config.KeypadConfig;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.binding.alarmdecoder.internal.protocol.IntCommandMap;
import org.openhab.binding.alarmdecoder.internal.protocol.KeypadMessage;
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
    private @Nullable IntCommandMap intCommandMap;
    private @Nullable KeypadMessage previousMessage;

    public KeypadHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(KeypadConfig.class);

        if (config.addressMask < 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid addressMask setting");
            return;
        }
        singleAddress = (Integer.bitCount(config.addressMask) == 1);

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
            if (command instanceof StringType) {
                String cmd = ((StringType) command).toString();
                handleKeypadCommand(cmd);
            }
        } else if (channelUID.getId().equals(CHANNEL_KP_INTCOMMAND)) {
            if (command instanceof Number) {
                int icmd = ((Number) command).intValue();
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
                sendCommand(ADCommand.addressedMessage(config.addressMask, cmd)); // send from keypad address
            } else {
                sendCommand(new ADCommand(cmd)); // send from AD address
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
        int addressMask = kpMsg.getIntAddressMask();
        if (!(((config.addressMask & addressMask) != 0) || config.addressMask == 0 || addressMask == 0)) {
            return;
        }
        logger.trace("Keypad handler for address mask {} received update: {}", config.addressMask, kpMsg);

        if (kpMsg.equals(previousMessage)) {
            return; // ignore repeated messages
        }

        if (config.sendStar) {
            if (kpMsg.alphaMessage.contains("Hit * for faults") || kpMsg.alphaMessage.contains("Press * to show faults")
                    || kpMsg.alphaMessage.contains("Press * Key")) {
                logger.debug("Sending * command to show faults.");
                if (singleAddress) {
                    sendCommand(ADCommand.addressedMessage(config.addressMask, "*")); // send from keypad address
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
