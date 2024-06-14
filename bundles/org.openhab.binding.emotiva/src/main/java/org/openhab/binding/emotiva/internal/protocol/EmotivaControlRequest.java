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
package org.openhab.binding.emotiva.internal.protocol;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.clamp;
import static org.openhab.binding.emotiva.internal.EmotivaCommandHelper.volumePercentageToDecibel;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaCommandType.*;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.FREQUENCY_HERTZ;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_band;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags.tuner_channel;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binds channels to a given command with datatype.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaControlRequest {
    private final Logger logger = LoggerFactory.getLogger(EmotivaControlRequest.class);
    private String name;
    private final EmotivaDataType dataType;
    private String channel;
    private final EmotivaControlCommands defaultCommand;
    private final EmotivaControlCommands setCommand;
    private final EmotivaControlCommands onCommand;
    private final EmotivaControlCommands offCommand;
    private final EmotivaControlCommands upCommand;
    private final EmotivaControlCommands downCommand;
    private double maxValue;
    private double minValue;
    private final Map<String, Map<EmotivaControlCommands, String>> commandMaps;
    private final EmotivaProtocolVersion protocolVersion;

    public EmotivaControlRequest(String channel, EmotivaSubscriptionTags channelSubscription,
            EmotivaControlCommands controlCommand, Map<String, Map<EmotivaControlCommands, String>> commandMaps,
            EmotivaProtocolVersion protocolVersion) {
        if (channelSubscription.equals(EmotivaSubscriptionTags.unknown)) {
            if (controlCommand.equals(EmotivaControlCommands.none)) {
                this.defaultCommand = EmotivaControlCommands.none;
                this.onCommand = EmotivaControlCommands.none;
                this.offCommand = EmotivaControlCommands.none;
                this.setCommand = EmotivaControlCommands.none;
                this.upCommand = EmotivaControlCommands.none;
                this.downCommand = EmotivaControlCommands.none;
            } else {
                this.defaultCommand = controlCommand;
                this.onCommand = resolveOnCommand(controlCommand);
                this.offCommand = resolveOffCommand(controlCommand);
                this.setCommand = resolveSetCommand(controlCommand);
                this.upCommand = resolveUpCommand(controlCommand);
                this.downCommand = resolveDownCommand(controlCommand);
            }
        } else {
            this.defaultCommand = resolveControlCommand(channelSubscription.getEmotivaName(), controlCommand);
            if (controlCommand.equals(EmotivaControlCommands.none)) {
                this.onCommand = resolveOnCommand(defaultCommand);
                this.offCommand = resolveOffCommand(defaultCommand);
                this.setCommand = resolveSetCommand(defaultCommand);
                this.upCommand = resolveUpCommand(defaultCommand);
                this.downCommand = resolveDownCommand(defaultCommand);
            } else {
                this.onCommand = controlCommand;
                this.offCommand = controlCommand;
                this.setCommand = controlCommand;
                this.upCommand = controlCommand;
                this.downCommand = controlCommand;
            }
        }
        this.name = defaultCommand.name();
        this.dataType = defaultCommand.getDataType();
        this.channel = channel;
        this.commandMaps = commandMaps;
        this.protocolVersion = protocolVersion;
        if (name.equals(EmotivaControlCommands.volume.name())
                || name.equals(EmotivaControlCommands.zone2_volume.name())) {
            minValue = DEFAULT_VOLUME_MIN_DECIBEL;
            maxValue = DEFAULT_VOLUME_MAX_DECIBEL;
        } else if (setCommand.name().endsWith(TRIM_SET_COMMAND_SUFFIX)) {
            minValue = DEFAULT_TRIM_MIN_DECIBEL * 2;
            maxValue = DEFAULT_TRIM_MAX_DECIBEL * 2;
        }
    }

    public EmotivaControlDTO createDTO(Command ohCommand, @Nullable State previousState) {
        switch (defaultCommand.getCommandType()) {
            case CYCLE -> {
                return EmotivaControlDTO.create(defaultCommand);
            }
            case MENU_CONTROL -> {
                if (ohCommand instanceof StringType value) {
                    try {
                        return EmotivaControlDTO.create(EmotivaControlCommands.valueOf(value.toString().toLowerCase()));
                    } catch (IllegalArgumentException e) {
                        return EmotivaControlDTO.create(EmotivaControlCommands.none);
                    }
                }
            }
            case MODE -> {
                if (ohCommand instanceof StringType value) {
                    // Check if value can be interpreted as a mode-<command>
                    try {
                        OHChannelToEmotivaCommand ohChannelToEmotivaCommand = OHChannelToEmotivaCommand
                                .valueOf(value.toString());
                        return EmotivaControlDTO.create(ohChannelToEmotivaCommand.getCommand());
                    } catch (IllegalArgumentException e) {
                        if ("1".equals(value.toString())) {
                            return EmotivaControlDTO.create(getUpCommand(), 1);
                        } else if ("-1".equals(value.toString())) {
                            return EmotivaControlDTO.create(getDownCommand(), -1);
                        }
                        return EmotivaControlDTO.create(EmotivaControlCommands.none);
                    }
                } else if (ohCommand instanceof Number value) {
                    if (value.intValue() >= 1) {
                        return EmotivaControlDTO.create(getUpCommand(), 1);
                    } else if (value.intValue() <= -1) {
                        return EmotivaControlDTO.create(getDownCommand(), -1);
                    }
                }
            }
            case NUMBER -> {
                if (ohCommand instanceof Number value) {
                    return handleNumberTypes(getSetCommand(), ohCommand, value);
                } else {
                    logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name,
                            NUMBER, ohCommand.getClass().getSimpleName());
                    return EmotivaControlDTO.create(EmotivaControlCommands.none);
                }
            }
            case NONE -> {
                switch (channel) {
                    case CHANNEL_TUNER_BAND -> {
                        return matchToCommandMap(ohCommand, tuner_band.getEmotivaName());
                    }
                    case CHANNEL_TUNER_CHANNEL_SELECT -> {
                        return matchToCommandMap(ohCommand, tuner_channel.getEmotivaName());
                    }
                    case CHANNEL_SOURCE -> {
                        return matchToCommandMap(ohCommand, MAP_SOURCES_MAIN_ZONE);
                    }
                    case CHANNEL_ZONE2_SOURCE -> {
                        return matchToCommandMap(ohCommand, MAP_SOURCES_ZONE_2);
                    }
                    default -> {
                        return EmotivaControlDTO.create(EmotivaControlCommands.none);
                    }
                }
            }
            case SET -> {
                if (ohCommand instanceof StringType value) {
                    return EmotivaControlDTO.create(getSetCommand(), value.toString());
                } else if (ohCommand instanceof Number value) {
                    return handleNumberTypes(getSetCommand(), ohCommand, value);
                } else if (ohCommand instanceof OnOffType value) {
                    if (value.equals(OnOffType.ON)) {
                        return EmotivaControlDTO.create(getOnCommand());
                    } else {
                        return EmotivaControlDTO.create(getOffCommand());
                    }
                } else {
                    logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name, SET,
                            ohCommand.getClass().getSimpleName());
                    return EmotivaControlDTO.create(EmotivaControlCommands.none);
                }
            }
            case SPEAKER_PRESET -> {
                if (ohCommand instanceof StringType value) {
                    try {
                        return EmotivaControlDTO.create(EmotivaControlCommands.valueOf(value.toString()));
                    } catch (IllegalArgumentException e) {
                        // No match found for preset command, default to cycling
                        return EmotivaControlDTO.create(defaultCommand);
                    }
                } else {
                    return EmotivaControlDTO.create(defaultCommand);
                }
            }
            case TOGGLE -> {
                if (ohCommand instanceof OnOffType value) {
                    if (value.equals(OnOffType.ON)) {
                        return EmotivaControlDTO.create(getOnCommand());
                    } else {
                        return EmotivaControlDTO.create(getOffCommand());
                    }
                } else {
                    logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name,
                            TOGGLE, ohCommand.getClass().getSimpleName());
                    return EmotivaControlDTO.create(EmotivaControlCommands.none);
                }
            }
            case UP_DOWN_SINGLE -> {
                if (ohCommand instanceof Number value) {
                    if (dataType.equals(FREQUENCY_HERTZ)) {
                        if (previousState instanceof Number pre) {
                            if (value.doubleValue() > pre.doubleValue()) {
                                return EmotivaControlDTO.create(getUpCommand(), 1);
                            } else if (value.doubleValue() < pre.doubleValue()) {
                                return EmotivaControlDTO.create(getDownCommand(), -1);
                            }
                        }
                    }
                    if (value.intValue() <= maxValue || value.intValue() >= minValue) {
                        if (value.intValue() >= 1) {
                            return EmotivaControlDTO.create(getUpCommand(), 1);
                        } else if (value.intValue() <= -1) {
                            return EmotivaControlDTO.create(getDownCommand(), -1);
                        }
                    }
                    // Reached max or min value, not sending anything
                    return EmotivaControlDTO.create(EmotivaControlCommands.none);
                } else if (ohCommand instanceof StringType value) {
                    if ("1".equals(value.toString())) {
                        return EmotivaControlDTO.create(getUpCommand(), 1);
                    } else if ("-1".equals(value.toString())) {
                        return EmotivaControlDTO.create(getDownCommand(), -1);
                    }
                } else if (ohCommand instanceof UpDownType value) {
                    if (value.equals(UpDownType.UP)) {
                        return EmotivaControlDTO.create(getUpCommand(), 1);
                    } else {
                        return EmotivaControlDTO.create(getDownCommand(), -1);
                    }
                } else {
                    logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name,
                            UP_DOWN_SINGLE, ohCommand.getClass().getSimpleName());
                }
                return EmotivaControlDTO.create(EmotivaControlCommands.none);
            }
            case UP_DOWN_HALF -> {
                if (ohCommand instanceof Number value) {
                    if (value.intValue() <= maxValue || value.intValue() >= minValue) {
                        Number pre = (Number) previousState;
                        if (pre == null) {
                            if (value.doubleValue() > 0) {
                                return EmotivaControlDTO.create(getUpCommand());
                            } else if (value.doubleValue() < 0) {
                                return EmotivaControlDTO.create(getDownCommand());
                            }
                        } else {
                            if (value.doubleValue() > pre.doubleValue()) {
                                return EmotivaControlDTO.create(getUpCommand());
                            } else if (value.doubleValue() < pre.doubleValue()) {
                                return EmotivaControlDTO.create(getDownCommand());
                            }
                        }
                    }
                } else {
                    logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name,
                            UP_DOWN_HALF, ohCommand.getClass().getSimpleName());
                    return EmotivaControlDTO.create(EmotivaControlCommands.none);
                }
            }
            default -> {
                return EmotivaControlDTO.create(EmotivaControlCommands.none);
            }
        }
        return EmotivaControlDTO.create(EmotivaControlCommands.none);
    }

    private EmotivaControlDTO matchToCommandMap(Command ohCommand, String mapName) {
        if (ohCommand instanceof StringType value) {
            Map<EmotivaControlCommands, String> commandMap = commandMaps.get(mapName);
            if (commandMap != null) {
                for (EmotivaControlCommands command : commandMap.keySet()) {
                    String map = commandMap.get(command);
                    if (map != null && map.equals(value.toString())) {
                        return EmotivaControlDTO.create(EmotivaControlCommands.matchToInput(command.toString()));
                    } else if (command.name().equalsIgnoreCase(value.toString())) {
                        return EmotivaControlDTO.create(command);
                    }
                }
            }
        }
        return EmotivaControlDTO.create(EmotivaControlCommands.none);
    }

    private EmotivaControlDTO handleNumberTypes(EmotivaControlCommands setCommand, Command ohCommand, Number value) {
        switch (dataType) {
            case DIMENSIONLESS_PERCENT -> {
                if (name.equals(EmotivaControlCommands.volume.name())) {
                    return EmotivaControlDTO.create(EmotivaControlCommands.set_volume,
                            volumePercentageToDecibel(value.intValue()));
                } else if (name.equals(EmotivaControlCommands.zone2_set_volume.name())) {
                    return EmotivaControlDTO.create(EmotivaControlCommands.zone2_set_volume,
                            volumePercentageToDecibel(value.intValue()));
                } else {
                    return EmotivaControlDTO.create(setCommand, value.intValue());
                }
            }
            case DIMENSIONLESS_DECIBEL -> {
                if (name.equals(EmotivaControlCommands.volume.name())) {
                    return createForVolumeSetCommand(ohCommand, value, EmotivaControlCommands.set_volume);
                } else if (name.equals(EmotivaControlCommands.zone2_volume.name())) {
                    return createForVolumeSetCommand(ohCommand, value, EmotivaControlCommands.zone2_set_volume);
                } else {
                    double doubleValue = setCommand.name().endsWith(TRIM_SET_COMMAND_SUFFIX)
                            ? value.doubleValue() * PROTOCOL_V3_LEVEL_MULTIPLIER
                            : value.doubleValue();
                    if (doubleValue >= maxValue) {
                        return EmotivaControlDTO.create(getSetCommand(), maxValue);
                    } else if (doubleValue <= minValue) {
                        return EmotivaControlDTO.create(getSetCommand(), minValue);
                    } else {
                        return EmotivaControlDTO.create(getSetCommand(), doubleValue);
                    }
                }
            }
            case FREQUENCY_HERTZ -> {
                return EmotivaControlDTO.create(getDefaultCommand(), value.intValue());
            }
            default -> {
                logger.debug("Could not create EmotivaControlDTO for {}:{}:{}, ohCommand is {}", channel, name,
                        setCommand.getDataType(), ohCommand.getClass().getSimpleName());
                return EmotivaControlDTO.create(EmotivaControlCommands.none);
            }
        }
    }

    private EmotivaControlDTO createForVolumeSetCommand(Command ohCommand, Number value,
            EmotivaControlCommands emotivaControlCommands) {
        if (ohCommand instanceof PercentType) {
            return EmotivaControlDTO.create(emotivaControlCommands, volumePercentageToDecibel(value.intValue()));
        } else {
            return EmotivaControlDTO.create(emotivaControlCommands, clamp(value, minValue, maxValue));
        }
    }

    private EmotivaControlCommands resolveUpCommand(EmotivaControlCommands controlCommand) {
        try {
            return EmotivaControlCommands.valueOf("%s_up".formatted(controlCommand.name()));
        } catch (IllegalArgumentException e) {
            // not found, setting original command
            return controlCommand;
        }
    }

    private EmotivaControlCommands resolveDownCommand(EmotivaControlCommands controlCommand) {
        try {
            return EmotivaControlCommands.valueOf("%s_down".formatted(controlCommand.name()));
        } catch (IllegalArgumentException e) {
            // not found, setting original command
            return controlCommand;
        }
    }

    private EmotivaControlCommands resolveControlCommand(String name, EmotivaControlCommands controlCommand) {
        try {
            return controlCommand.equals(EmotivaControlCommands.none) ? EmotivaControlCommands.valueOf(name)
                    : controlCommand;
        } catch (IllegalArgumentException e) {
            // ignore
        }
        return EmotivaControlCommands.none;
    }

    private EmotivaControlCommands resolveOnCommand(EmotivaControlCommands controlCommand) {
        try {
            return EmotivaControlCommands.valueOf("%s_on".formatted(controlCommand.name()));
        } catch (IllegalArgumentException e) {
            // not found, setting original command
            return controlCommand;
        }
    }

    private EmotivaControlCommands resolveOffCommand(EmotivaControlCommands controlCommand) {
        try {
            return EmotivaControlCommands.valueOf("%s_off".formatted(controlCommand.name()));
        } catch (IllegalArgumentException e) {
            // not found, using original command
            return controlCommand;
        }
    }

    /**
     * Checks for commands with _trim_set suffix, which indicate speaker trims with a fixed min/max value.
     */
    private EmotivaControlCommands resolveSetCommand(EmotivaControlCommands controlCommand) {
        try {
            return EmotivaControlCommands.valueOf("%s_trim_set".formatted(controlCommand.name()));
        } catch (IllegalArgumentException e) {
            // not found, using original command
            return controlCommand;
        }
    }

    public String getName() {
        return name;
    }

    public EmotivaDataType getDataType() {
        return dataType;
    }

    public String getChannel() {
        return channel;
    }

    public EmotivaControlCommands getDefaultCommand() {
        return defaultCommand;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public EmotivaControlCommands getSetCommand() {
        return setCommand;
    }

    public EmotivaControlCommands getOnCommand() {
        return onCommand;
    }

    public EmotivaControlCommands getOffCommand() {
        return offCommand;
    }

    public EmotivaControlCommands getUpCommand() {
        return upCommand;
    }

    public EmotivaControlCommands getDownCommand() {
        return downCommand;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public EmotivaProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {
        return "EmotivaControlRequest{" + "name='" + name + '\'' + ", dataType=" + dataType + ", channel='" + channel
                + '\'' + ", defaultCommand=" + defaultCommand + ", setCommand=" + setCommand + ", onCommand="
                + onCommand + ", offCommand=" + offCommand + ", upCommand=" + upCommand + ", downCommand=" + downCommand
                + ", maxValue=" + maxValue + ", minValue=" + minValue + ", commandMaps=" + commandMaps
                + ", protocolVersion=" + protocolVersion + '}';
    }
}
