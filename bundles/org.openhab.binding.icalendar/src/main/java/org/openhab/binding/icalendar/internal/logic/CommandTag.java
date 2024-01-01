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
package org.openhab.binding.icalendar.internal.logic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class that implements a Command Tag that may be embedded in an
 * Event Description. Valid Tags must follow one of the following forms..
 *
 * <pre>
 * {@code
 * BEGIN:<itemName>:<targetState>
 * BEGIN:<itemName>:<targetState>:<authorizationCode>
 * END:<itemName>:<targetState>
 * END:<itemName>:<targetState>:<authorizationCode>
 * }
 * </pre>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CommandTag {

    private static final List<Class<? extends Command>> otherCommandTypes = Arrays.asList(HSBType.class,
            DecimalType.class, QuantityType.class, OnOffType.class, OpenClosedType.class, UpDownType.class,
            PlayPauseType.class, RewindFastforwardType.class, StringType.class);

    private static final List<Class<? extends Command>> percentCommandType = Arrays.asList(PercentType.class);

    private static final Logger logger = LoggerFactory.getLogger(CommandTag.class);

    private String inputLine;
    private CommandTagType tagType;
    private String itemName;
    private String targetState;
    private String authorizationCode;
    private @Nullable Command theCommand;

    public CommandTag(String inputLine) throws IllegalArgumentException {
        this.inputLine = inputLine.trim();

        if (!CommandTagType.prefixValid(inputLine)) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Bad tag prefix!", inputLine));
        }

        if (!inputLine.contains(":")) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Missing \":\" delimiters!", inputLine));
        }

        String[] fields = inputLine.split(":");
        if (fields.length < 3) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Not enough fields!", inputLine));
        }

        if (fields.length > 4) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Too many fields!", inputLine));
        }

        try {
            tagType = CommandTagType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Invalid Tag Type!", inputLine));
        }

        itemName = fields[1].trim();
        if (itemName.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Item name empty!", inputLine));
        }

        if (!itemName.matches("^\\w+$")) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Bad syntax for Item name!", inputLine));
        }

        targetState = fields[2].trim();
        if (targetState.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Command Tag Exception \"%s\" => Target State empty!", inputLine));
        }

        // string is in double quotes => force StringType
        if (targetState.startsWith("\"") && targetState.endsWith("\"")) {
            // new StringType() should always succeed
            theCommand = new StringType(targetState.replace("\"", ""));
        }

        // string is in single quotes => ditto
        else if (targetState.startsWith("'") && targetState.endsWith("'")) {
            // new StringType() should always succeed
            theCommand = new StringType(targetState.replace("'", ""));
        }

        // string ends with % => try PercentType
        else if (targetState.endsWith("%")) {
            theCommand = TypeParser.parseCommand(percentCommandType,
                    targetState.substring(0, targetState.length() - 1));
            if (theCommand == null) {
                throw new IllegalArgumentException(String
                        .format("Command Tag Exception \"%s\" => Invalid Target State percent value!", inputLine));
            }
        }

        // try all other possible CommandTypes
        if (theCommand == null) {
            // TypeParser.parseCommand(otherCommandTypes should always succeed (with new StringType())
            theCommand = TypeParser.parseCommand(otherCommandTypes, targetState);
        }

        if (fields.length == 4) {
            authorizationCode = fields[3].trim();
        } else {
            authorizationCode = "";
        }
    }

    public static @Nullable CommandTag createCommandTag(String inputLine) {
        if (inputLine.isEmpty() || !CommandTagType.prefixValid(inputLine)) {
            logger.trace("Command Tag Trace: \"{}\" => NOT a (valid) Command Tag!", inputLine);
            return null;
        }
        try {
            final CommandTag tag = new CommandTag(inputLine);
            logger.trace("Command Tag Trace: \"{}\" => Fully valid Command Tag!", inputLine);
            return tag;
        } catch (IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return null;
        }
    }

    public @Nullable Command getCommand() {
        return theCommand;
    }

    public String getFullTag() {
        return inputLine;
    }

    public String getItemName() {
        return itemName;
    }

    public CommandTagType getTagType() {
        return tagType;
    }

    public boolean isAuthorized(@Nullable String userAuthorizationCode) {
        return (userAuthorizationCode == null || userAuthorizationCode.isEmpty()
                || userAuthorizationCode.equals(authorizationCode));
    }
}
