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
 *   BEGIN:<itemName>:<targetState>
 *   BEGIN:<itemName>:<targetState>:<authorizationCode>
 *   END:<itemName>:<targetState> END:<itemName>:<targetState>:<authorizationCode>
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class CommandTag {
    private String itemName;
    private String targetState;
    private String fullTag;
    private CommandTagType tagType;
    private boolean isValid = false;
    private String authorizationCode;

    private final Logger logger = LoggerFactory.getLogger(CommandTag.class);

    private static final List<Class<? extends Command>> percentCommandType = Arrays.asList(PercentType.class);

    private static final List<Class<? extends Command>> otherCommandTypes = Arrays.asList(QuantityType.class,
            OnOffType.class, OpenClosedType.class, UpDownType.class, HSBType.class, PlayPauseType.class,
            RewindFastforwardType.class, StringType.class);

    public String getItemName() {
        return itemName != null ? itemName : "";
    }

    public String getTargetState() {
        return targetState != null ? targetState : "";
    }

    public String getFullTag() {
        return fullTag != null ? fullTag : "";
    }

    public CommandTagType getTagType() {
        return tagType;
    }

    public boolean isValid() {
        return isValid;
    }

    public CommandTag(String line) {
        fullTag = line;
        if (!fullTag.contains(":")) {
            logger.trace("Input line \"{}\" => No \":\" delimiters!", fullTag);
            return;
        }
        String[] fields = fullTag.split(":");
        if (fields.length < 3) {
            logger.trace("Input line \"{}\" => Not enough fields!", fullTag);
            return;
        }
        try {
            tagType = CommandTagType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            logger.trace("Input line \"{}\" => Bad tag prefix!", fullTag);
            return;
        }
        itemName = fields[1].trim();
        if (itemName == null || itemName.isEmpty()) {
            logger.trace("Input line \"{}\" => Item name empty!", fullTag);
            return;
        }
        targetState = fields[2].trim();
        if (targetState == null || targetState.isEmpty()) {
            logger.trace("Input line \"{}\" => Target State empty!", fullTag);
            return;
        }
        isValid = true;
        if (fields.length > 3) {
            authorizationCode = fields[3].trim();
        }
    }

    public static CommandTag createCommandTag(String line) {
        if (CommandTagType.prefixValid(line)) {
            CommandTag tag = new CommandTag(line.trim());
            return tag.isValid ? tag : null;
        } else {
            return null;
        }
    }

    public boolean isAuthorized(String userAuthorizationCode) {
        return isValid && (userAuthorizationCode == null || userAuthorizationCode.isEmpty()
                || userAuthorizationCode.equals(authorizationCode));
    }

    public Command getCommand() {
        if (targetState == null || targetState.isEmpty()) {
            return null;
        }

        // string is in double quotes => force StringType
        if (targetState.startsWith("\"") && targetState.endsWith("\"")) {
            return new StringType(targetState.replaceAll("\"", ""));
        }

        // string is in single quotes => ditto
        if (targetState.startsWith("'") && targetState.endsWith("'")) {
            return new StringType(targetState.replaceAll("'", ""));
        }

        Command cmd;

        // string ends with % => try PercentType
        if (targetState.endsWith("%")) {
            if ((cmd = TypeParser.parseCommand(percentCommandType,
                    targetState.substring(0, targetState.length() - 1))) != null) {
                return cmd;
            }
        }

        // try all other possible CommandTypes
        if ((cmd = TypeParser.parseCommand(otherCommandTypes, targetState)) != null) {
            return cmd;
        }

        return null;
    }

}
