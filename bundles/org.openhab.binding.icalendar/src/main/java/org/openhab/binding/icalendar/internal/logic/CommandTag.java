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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.PatternSyntaxException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This is a class that implements a Command Tag that may be embedded in an Event Description
 *  Valid Tags must follow one of the following forms..
 *
 *      BEGIN:<itemName>:<targetState>
 *      BEGIN:<itemName>:<targetState>:<authorizationCode>
 *      END:<itemName>:<targetState>
 *      END:<itemName>:<targetState>:<authorizationCode>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class CommandTag {
    private final Logger logger = LoggerFactory.getLogger(CommandTag.class);

    @Nullable
    public String itemName;

    @Nullable
    public String targetState;

    @Nullable
    public String fullTag;

    @Nullable
    public CommandTagType tagType;

    protected boolean isValid = false;

    @Nullable
    private String authorizationCode;

    private @Nullable Command castToCommandType(Class<? extends Command> commandType) {
        try {
            Method valueOf = commandType.getMethod("valueOf", String.class);
            Command cmd = (Command) valueOf.invoke(commandType, targetState);
            if (cmd != null) {
                return cmd;
            }
        } catch (IllegalArgumentException e) {
            logger.trace("IllegalArgumentException: \"{}\"", targetState);
        } catch (NoSuchMethodException e) {
            logger.trace("NoSuchMethodException: \"valueOf\"");
        } catch (IllegalAccessException e) {
            logger.trace("IllegalAccessException: \"valueOf\"");
        } catch (InvocationTargetException e) {
            logger.trace("InvocationTargetException: \"valueOf\"");
        }
        return null;
    }

    public CommandTag(String line) {
        String[] fields;
        try {
            fields = line.split(":");
        } catch (PatternSyntaxException e) {
            logger.trace("PatternSyntaxException: \"{}\"", line);
            return;
        }
        if (fields.length < 3) {
            logger.trace("Not enough fields: \"{}\"", line);
            return;
        }
        try {
            tagType = CommandTagType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            logger.trace("Bad tag type prefix: \"{}\"", line);
            return;
        }
        itemName = fields[1].trim();
        if (itemName.isEmpty()) {
            logger.trace("Empty item name: \"{}\"", line);
            return;
        }
        targetState = fields[2].trim();
        if (targetState.isEmpty()) {
            logger.trace("Empty target state: \"{}\"", line);
            return;
        }

        isValid = true;
        fullTag = line;
        if (fields.length > 3) {
            authorizationCode = fields[3].trim();
        } else {
            authorizationCode = "";
        }
    }

    public static @Nullable CommandTag createCommandTag(String line) {
        if (CommandTagType.prefixValid(line)) {
            CommandTag tag = new CommandTag(line.trim());
            return tag.isValid ? tag : null;
        } else {
            return null;
        }
    }

    public boolean isAuthorized(@Nullable String authCode) {
        return isValid && (authCode == null || authCode.isEmpty() || authCode.equals(authorizationCode));
    }

    public @Nullable Command getCommand() {
        // string is in double quotes => force StringType
        String currentTargetState = targetState;
        if (currentTargetState == null) {
            return null;
        }
        if (currentTargetState.startsWith("\"") && currentTargetState.endsWith("\"")) {
            return new StringType(currentTargetState.replaceAll("\"", ""));
        }

        // string is in single quotes => ditto
        if (currentTargetState.startsWith("'") && currentTargetState.endsWith("'")) {
            return new StringType(currentTargetState.replaceAll("'", ""));
        }

        Command cmd = null;

        // string ends with % => try PercentType
        if (currentTargetState.endsWith("%")) {
            try {
                cmd = new PercentType(currentTargetState.replaceAll("%", ""));
                return cmd;
            } catch (IllegalArgumentException e) {
                logger.trace("Illegal percentage value: \"{}\"", targetState);
            }
        }

        // try all other possible CommandTypes
        if ((cmd = castToCommandType(QuantityType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(OnOffType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(OpenClosedType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(UpDownType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(HSBType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(PlayPauseType.class)) != null) {
            return cmd;
        }
        if ((cmd = castToCommandType(RewindFastforwardType.class)) != null) {
            return cmd;
        }

        // fallback to StringType (should never fail)
        return castToCommandType(StringType.class);
    }

}
