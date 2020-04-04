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
package org.openhab.binding.icalendar.internal.logic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class that implements a Command Tag that may be embedded in an
 * Event Description. Valid Tags must follow one of the following forms..
 *
 * BEGIN:<itemName>:<targetState>
 * BEGIN:<itemName>:<targetState>:<authorizationCode>
 * END:<itemName>:<targetState> END:<itemName>:<targetState>:<authorizationCode>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class CommandTag {
    private static final List<Class<? extends Command>> otherCommandTypes = Arrays.asList(QuantityType.class,
            OnOffType.class, OpenClosedType.class, UpDownType.class, HSBType.class, PlayPauseType.class,
            RewindFastforwardType.class, StringType.class);
    private static final List<Class<? extends Command>> percentCommandType = Arrays.asList(PercentType.class);

    private final @Nullable String authorizationCode;
    private final String fullTag;
    private final boolean isValid;
    private final @Nullable String itemName;
    private final Logger logger = LoggerFactory.getLogger(CommandTag.class);
    private final @Nullable CommandTagType tagType;
    private final @Nullable String targetState;

    public CommandTag(String line) {
        fullTag = line;
        boolean seemsValid = false;
        @Nullable
        String parsedAuthorizationCode = null;
        @Nullable
        String parsedItemName = null;
        @Nullable
        CommandTagType parsedTagType = null;
        @Nullable
        String parsedTargetState = null;
        boolean parse = true;

        if (!fullTag.contains(":")) {
            logger.trace("Input line \"{}\" => No \":\" delimiters!", fullTag);
            parse = false;
        }
        String[] fields = fullTag.split(":");
        if (fields.length < 3 && parse) {
            logger.trace("Input line \"{}\" => Not enough fields!", fullTag);
            parse = false;
        }
        if (parse) {
            try {
                parsedTagType = CommandTagType.valueOf(fields[0]);
            } catch (IllegalArgumentException e) {
                logger.trace("Input line \"{}\" => Bad tag prefix!", fullTag);
                parse = false;
            }
        }
        if (parse) {
            String newItemName = fields[1].trim();
            if (newItemName.isEmpty()) {
                logger.trace("Input line \"{}\" => Item name empty!", fullTag);
                parse = false;
            } else {
                parsedItemName = newItemName;
            }
        }
        if (parse) {
            String newTargetState = fields[2].trim();
            if (newTargetState.isEmpty()) {
                logger.trace("Input line \"{}\" => Target State empty!", fullTag);
                parse = false;
            } else {
                parsedTargetState = newTargetState;
            }
        }

        if (parse) {
            seemsValid = true;
            if (fields.length > 3) {
                parsedAuthorizationCode = fields[3].trim();
            }
        }

        authorizationCode = parsedAuthorizationCode;
        isValid = seemsValid;
        itemName = parsedItemName;
        tagType = parsedTagType;
        targetState = parsedTargetState;
    }

    public static @Nullable CommandTag createCommandTag(String line) {
        if (CommandTagType.prefixValid(line)) {
            CommandTag tag = new CommandTag(line.trim());
            return tag.isValid ? tag : null;
        } else {
            return null;
        }
    }

    public @Nullable Command getCommand() {
        String currentTargetState = targetState;
        if (currentTargetState == null || currentTargetState.isEmpty()) {
            return null;
        }

        // string is in double quotes => force StringType
        if (currentTargetState.startsWith("\"") && currentTargetState.endsWith("\"")) {
            return new StringType(currentTargetState.replaceAll("\"", ""));
        }

        // string is in single quotes => ditto
        if (currentTargetState.startsWith("'") && currentTargetState.endsWith("'")) {
            return new StringType(currentTargetState.replaceAll("'", ""));
        }

        Command cmd;

        // string ends with % => try PercentType
        if (currentTargetState.endsWith("%")) {
            cmd = TypeParser.parseCommand(percentCommandType,
                    currentTargetState.substring(0, currentTargetState.length() - 1));
            if (cmd != null) {
                return cmd;
            }
        }

        // try all other possible CommandTypes
        cmd = TypeParser.parseCommand(otherCommandTypes, currentTargetState);
        if (cmd != null) {
            return cmd;
        }

        return null;
    }

    public String getFullTag() {
        return fullTag;
    }

    public String getItemName() {
        String currentItemName = itemName;
        return currentItemName != null ? currentItemName : "";
    }

    public @Nullable CommandTagType getTagType() {
        return tagType;
    }

    public String getTargetState() {
        String currentTargetState = targetState;
        return currentTargetState != null ? currentTargetState : "";
    }

    public boolean isAuthorized(@Nullable String userAuthorizationCode) {
        return isValid && (userAuthorizationCode == null || userAuthorizationCode.isEmpty()
                || userAuthorizationCode.equals(authorizationCode));
    }

    public boolean isValid() {
        return isValid;
    }
}
