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
    private final @Nullable String upString;
    private final @Nullable String downString;
    private final String stopString;

    /**
     * Creates a new rollershutter value.
     *
     * @param upString The UP value string. This will be compared to MQTT messages.
     * @param downString The DOWN value string. This will be compared to MQTT messages.
     * @param stopString The STOP value string. This will be compared to MQTT messages.
     */
    public RollershutterValue(@Nullable String upString, @Nullable String downString, @Nullable String stopString) {
        super(CoreItemFactory.ROLLERSHUTTER,
                List.of(UpDownType.class, StopMoveType.class, PercentType.class, StringType.class));
        this.upString = upString;
        this.downString = downString;
        this.stopString = stopString == null ? StopMoveType.STOP.name() : stopString;
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof StopMoveType) {
            if (command == StopMoveType.STOP) {
                return command;
            } else {
                throw new IllegalArgumentException(command.toString() + " is not a valid command for MQTT.");
            }
        } else if (command instanceof UpDownType) {
            if (command == UpDownType.UP) {
                if (upString != null) {
                    return command;
                } else {
                    return PercentType.ZERO;
                }
            } else {
                if (downString != null) {
                    return command;
                } else {
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
            } else if (updatedValue.equals(stopString)) {
                return StopMoveType.STOP;
            }
        }
        throw new IllegalStateException("Cannot call parseCommand() with " + command.toString());
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        final String upString = this.upString;
        final String downString = this.downString;
        final String stopString = this.stopString;
        if (command == UpDownType.UP) {
            if (upString != null) {
                return upString;
            } else {
                return ((UpDownType) command).name();
            }
        } else if (command == UpDownType.DOWN) {
            if (downString != null) {
                return downString;
            } else {
                return ((UpDownType) command).name();
            }
        } else if (command == StopMoveType.STOP) {
            if (stopString != null) {
                return stopString;
            } else {
                return ((StopMoveType) command).name();
            }
        } else if (command instanceof PercentType percentage) {
            if (command.equals(PercentType.HUNDRED) && downString != null) {
                return downString;
            } else if (command.equals(PercentType.ZERO) && upString != null) {
                return upString;
            } else {
                return String.valueOf(percentage.intValue());
            }
        } else {
            throw new IllegalArgumentException("Invalid command type for Rollershutter item");
        }
    }
}
