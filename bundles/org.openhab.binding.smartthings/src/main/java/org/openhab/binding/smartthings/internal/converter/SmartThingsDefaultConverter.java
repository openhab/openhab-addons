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
package org.openhab.binding.smartthings.internal.converter;

import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsArgument;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCommand;
import org.openhab.binding.smartthings.internal.dto.SmartThingsEnumCommand;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * This "Converter" is assigned to a channel when a special converter is not needed.
 * A channel specific converter is specified in the thing-type channel property smartthings-converter then that channel
 * is used.
 * If a channel specific converter is not found a convert based on the channel ID is used.
 * If there is no convert found then this Default converter is used.
 * Yes, it would be possible to change the SamrtthingsConverter class to not being abstract and implement these methods
 * there. But, this makes it explicit that the default converter is being used.
 * See SmartThingsThingHandler.initialize() for details
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsDefaultConverter extends SmartThingsConverter {
    private static final String CAPABILITY_MEDIA_PLAYBACK = "mediaPlayback";
    private static final String ATTRIBUTE_PLAYBACK_STATUS = "playbackStatus";
    private static final String ATTRIBUTE_SUPPORTED_PLAYBACK_COMMANDS = "supportedPlaybackCommands";
    private static final String PLAYBACK_STATUS_FAST_FORWARDING = "fast forwarding";
    private static final String PLAYBACK_STATUS_PAUSED = "paused";
    private static final String PLAYBACK_STATUS_PLAYING = "playing";
    private static final String PLAYBACK_STATUS_REWINDING = "rewinding";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsDefaultConverter.class);

    public SmartThingsDefaultConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    private Object getValue(Command command, ThingTypeUID thingUid, String channelId, String targetType)
            throws SmartThingsException {
        Object value = null;

        String commandSt = command.toString().toLowerCase(Locale.ROOT);

        if (command instanceof DateTimeType dateTimeCommand) {
            value = dateTimeCommand.format("%m/%d/%Y %H.%M.%S");
        } else if (command instanceof HSBType hsbCommand) {
            value = String.format("[%d, %d, %d ]", hsbCommand.getHue().intValue(),
                    hsbCommand.getSaturation().intValue(), hsbCommand.getBrightness().intValue());
        } else if (command instanceof DecimalType) {
            DecimalType dc = (DecimalType) command;
            value = getNumberValue(dc.intValue(), dc.doubleValue(), targetType);
        } else if (command instanceof QuantityType<?> quantityCommand) {
            value = getNumberValue(quantityCommand.intValue(), quantityCommand.doubleValue(), targetType);
        } else if (command instanceof IncreaseDecreaseType) {
            value = commandSt;
        } else if (command instanceof NextPreviousType) {
            value = commandSt;
        } else if (command instanceof OnOffType) {
            value = commandSt;
        } else if (command instanceof OpenClosedType) {
            value = commandSt;
        } else if (command instanceof PercentType) {
            value = command.toString();
        } else if (command instanceof PointType) {
            logger.warn(
                    "Warning - PointType Command is not supported by SmartThings. Please configure to use a different command type. CapabilityKey: {}, capabilityAttribute {}",
                    thingUid, channelId);
            value = command.toFullString();
        } else if (command instanceof RefreshType) {
            value = commandSt;
        } else if (command instanceof RewindFastforwardType) {
            value = commandSt;
        } else if (command instanceof StopMoveType) {
            value = commandSt;
        } else if (command instanceof PlayPauseType) {
            value = commandSt;
        } else if (command instanceof StringListType) {
            value = command.toString();
        } else if (command instanceof StringType) {
            String st = command.toString();
            if (st.startsWith("{")) {
                value = gson.fromJson(st, JsonElement.class);
            } else {
                value = st;
            }
        } else if (command instanceof UpDownType) {
            value = commandSt;
        } else {
            logger.warn(
                    "Warning - The SmartThings converter does not know how to handle the {} command. The SmartThingsonverter class should be updated.  CapabilityKey: {},  capabilityAttribute {}",
                    command.getClass().getName(), thingUid, channelId);
            value = commandSt;
        }

        return Objects.requireNonNull(value);
    }

    private Object getNumberValue(int intValue, double doubleValue, String targetType) throws SmartThingsException {
        if (SmartThingsBindingConstants.SM_TYPE_INTEGER.equals(targetType)) {
            return intValue;
        } else if (SmartThingsBindingConstants.SM_TYPE_NUMBER.equals(targetType)) {
            return doubleValue;
        } else {
            throw new SmartThingsException("Unknown conversion type:" + targetType);
        }
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command,
            SmartThingsCapability capa, SmartThingsAttribute attr, String componentKey, String capaKey, String attrKey,
            String targetType, String commandKey) throws SmartThingsException {
        String cmdName = "";
        Object[] arguments = null;

        Object value = getValue(command, thing.getThingTypeUID(), channelUid.getId(), targetType);
        value = convertMediaPlaybackCommand(capaKey, attrKey, command, value);

        if (SmartThingsBindingConstants.CHANNEL_NAME_COLOR.equals(attrKey)) {
            attr.setter = SmartThingsBindingConstants.CMD_SET_COLOR;
        }

        if (attr.setter != null) {
            SmartThingsCommand cmd = getCommand(capa, attr.setter);
            cmdName = cmd.name;
            arguments = getCommandArguments(cmd, value);
        } else {
            String enumCommand = getEnumCommand(attr, value.toString());
            if (enumCommand != null) {
                SmartThingsCommand cmd = getCommand(capa, enumCommand);
                cmdName = cmd.name;
            } else if (!commandKey.isBlank()) {
                cmdName = commandKey;
                arguments = new Object[] { convertStaticCommandArgument(command) };
            } else {
                cmdName = value.toString();
            }
        }

        pushCommand(componentKey, capaKey, cmdName, arguments);
    }

    private Object convertMediaPlaybackCommand(String capaKey, String attrKey, Command command, Object value)
            throws SmartThingsException {
        if (!CAPABILITY_MEDIA_PLAYBACK.equals(capaKey)) {
            return value;
        }
        if (ATTRIBUTE_SUPPORTED_PLAYBACK_COMMANDS.equals(attrKey)) {
            if (PlayPauseType.PLAY.equals(command) || PlayPauseType.PAUSE.equals(command)
                    || StopMoveType.STOP.equals(command)) {
                return value;
            }
            throw new SmartThingsException("mediaPlayback does not support playback command: " + command);
        }
        if (!ATTRIBUTE_PLAYBACK_STATUS.equals(attrKey)) {
            return value;
        }
        if (PlayPauseType.PLAY.equals(command)) {
            return PLAYBACK_STATUS_PLAYING;
        }
        if (PlayPauseType.PAUSE.equals(command)) {
            return PLAYBACK_STATUS_PAUSED;
        }
        if (RewindFastforwardType.REWIND.equals(command)) {
            return PLAYBACK_STATUS_REWINDING;
        }
        if (RewindFastforwardType.FASTFORWARD.equals(command)) {
            return PLAYBACK_STATUS_FAST_FORWARDING;
        }
        if (command instanceof NextPreviousType) {
            throw new SmartThingsException("mediaPlayback does not support navigation command: " + command);
        }
        return value;
    }

    private SmartThingsCommand getCommand(SmartThingsCapability capa, String commandName) throws SmartThingsException {
        SmartThingsCommand cmd = capa.commands.get(commandName);
        if (cmd == null) {
            throw new SmartThingsException("Command not found for capaKey: " + capa.id + " command: " + commandName);
        }
        return cmd;
    }

    private Object[] getCommandArguments(SmartThingsCommand cmd, Object value) {
        if (cmd.arguments == null) {
            return new Object[0];
        }

        Stack<Object> stack = new Stack<Object>();
        for (SmartThingsArgument arg : cmd.arguments) {
            if (Boolean.TRUE.equals(arg.optional)) {
                continue;
            }

            stack.push(value);
        }
        return stack.toArray();
    }

    private @Nullable String getEnumCommand(SmartThingsAttribute attr, String value) {
        if (attr.enumCommands == null) {
            return null;
        }

        for (SmartThingsEnumCommand enumCommand : attr.enumCommands) {
            if (value.equals(enumCommand.value)) {
                return enumCommand.command;
            }
        }
        return null;
    }

    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartThings) {
        return defaultConvertToOpenHab(thing, channelUid, dataFromSmartThings);
    }
}
