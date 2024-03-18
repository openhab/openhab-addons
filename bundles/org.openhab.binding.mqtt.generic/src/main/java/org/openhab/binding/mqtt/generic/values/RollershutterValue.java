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
package org.openhab.binding.mqtt.generic.values;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * Implements a rollershutter value.
 * <p>
 * The stop, up and down strings have multiple purposes.
 * For one if those strings are received via MQTT they are recognised as corresponding commands
 * and also posted as Commands to the framework.
 * And if a user commands an Item->Channel to perform Stop the corresponding string is send. For Up,Down
 * the percentage 0 and 100 is send.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RollershutterValue extends Value {
    // openHAB interprets open rollershutters as 0, and closed as 100
    private static final String UP_VALUE = "0";
    private static final String DOWN_VALUE = "100";
    // other devices may interpret it the opposite, so we need to be able
    // to invert it
    private static final String INVERTED_UP_VALUE = DOWN_VALUE;
    private static final String INVERTED_DOWN_VALUE = UP_VALUE;

    private final @Nullable String upCommandString;
    private final @Nullable String downCommandString;
    private final @Nullable String stopCommandString;
    private final @Nullable String upStateString;
    private final @Nullable String downStateString;
    private final boolean inverted;
    private final boolean transformExtentsToString;

    /**
     * Creates a new rollershutter value.
     *
     * @param upCommandString The UP command string.
     * @param downCommandString The DOWN command string.
     * @param stopCommandString The STOP command string.
     * @param upStateString The UP value string. This will be compared to MQTT messages.
     * @param downStateString The DOWN value string. This will be compared to MQTT messages.
     * @param inverted Whether to invert 0-100/100-0
     * @param transformExtentsToString Whether 0/100 will be sent as UP/DOWN
     */
    public RollershutterValue(@Nullable String upCommandString, @Nullable String downCommandString,
            @Nullable String stopCommandString, @Nullable String upStateString, @Nullable String downStateString,
            boolean inverted, boolean transformExtentsToString) {
        super(CoreItemFactory.ROLLERSHUTTER,
                List.of(UpDownType.class, StopMoveType.class, PercentType.class, StringType.class));
        this.upCommandString = upCommandString;
        this.downCommandString = downCommandString;
        this.stopCommandString = stopCommandString;
        if (upStateString == null) {
            this.upStateString = upCommandString;
        } else {
            this.upStateString = upStateString;
        }
        if (downStateString == null) {
            this.downStateString = downCommandString;
        } else {
            this.downStateString = downStateString;
        }
        this.inverted = inverted;
        this.transformExtentsToString = transformExtentsToString;
    }

    /**
     * Creates a new rollershutter value.
     *
     * @param upString The UP value string. This will be compared to MQTT messages.
     * @param downString The DOWN value string. This will be compared to MQTT messages.
     * @param stopString The STOP value string. This will be compared to MQTT messages.
     */
    public RollershutterValue(@Nullable String upString, @Nullable String downString, @Nullable String stopString) {
        this(upString, downString, stopString, upString, downString, false, true);
    }

    private Command parseType(Command command, @Nullable String upString, @Nullable String downString)
            throws IllegalArgumentException {
        if (command instanceof StopMoveType) {
            if (command == StopMoveType.STOP && stopCommandString != null) {
                return command;
            } else {
                throw new IllegalArgumentException(command.toString() + " is not a valid command for MQTT.");
            }
        } else if (command instanceof UpDownType) {
            if (command == UpDownType.UP) {
                if (upString != null) {
                    return command;
                } else {
                    // Do not handle inversion here. See parseCommand below
                    return PercentType.ZERO;
                }
            } else {
                if (downString != null) {
                    return command;
                } else {
                    // Do not handle inversion here. See parseCommand below
                    return PercentType.HUNDRED;
                }
            }
        } else if (command instanceof PercentType percentage) {
            return percentage;
        } else if (command instanceof StringType) {
            final String updatedValue = command.toString();
            if (updatedValue.equals(upString)) {
                return UpDownType.UP;
            } else if (updatedValue.equals(downString)) {
                return UpDownType.DOWN;
            } else if (updatedValue.equals(stopCommandString)) {
                return StopMoveType.STOP;
            } else {
                return PercentType.valueOf(updatedValue);
            }
        }
        throw new IllegalStateException("Cannot call parseCommand() with " + command.toString());
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        // Do not handle inversion in this code path. parseCommand might be called
        // multiple times when sending a command TO an MQTT topic. The inversion is
        // handled _only_ in getMQTTpublishValue
        return parseType(command, upCommandString, downCommandString);
    }

    @Override
    public Type parseMessage(Command command) throws IllegalArgumentException {
        if (command instanceof StringType string && string.toString().isEmpty()) {
            return UnDefType.NULL;
        }
        command = parseType(command, upStateString, downStateString);
        if (inverted && command instanceof PercentType percentType) {
            return new PercentType(100 - percentType.intValue());
        }
        return command;
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        return getMQTTpublishValue(command, transformExtentsToString);
    }

    public String getMQTTpublishValue(Command command, boolean transformExtentsToString) {
        final String upCommandString = this.upCommandString;
        final String downCommandString = this.downCommandString;
        final String stopCommandString = this.stopCommandString;
        if (command == UpDownType.UP) {
            if (upCommandString != null) {
                return upCommandString;
            } else {
                return (inverted ? INVERTED_UP_VALUE : UP_VALUE);
            }
        } else if (command == UpDownType.DOWN) {
            if (downCommandString != null) {
                return downCommandString;
            } else {
                return (inverted ? INVERTED_DOWN_VALUE : DOWN_VALUE);
            }
        } else if (command == StopMoveType.STOP) {
            if (stopCommandString != null) {
                return stopCommandString;
            } else {
                return ((StopMoveType) command).name();
            }
        } else if (command instanceof PercentType percentage) {
            if (transformExtentsToString && command.equals(PercentType.HUNDRED) && downCommandString != null) {
                return downCommandString;
            } else if (transformExtentsToString && command.equals(PercentType.ZERO) && upCommandString != null) {
                return upCommandString;
            } else {
                int value = percentage.intValue();
                if (inverted) {
                    value = 100 - value;
                }
                return String.valueOf(value);
            }
        } else {
            throw new IllegalArgumentException("Invalid command type for Rollershutter item");
        }
    }
}
