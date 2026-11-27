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
package org.openhab.binding.mqtt.generic.values;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Implements a player value.
 * <p>
 * Each of the player commands ({@link PlayPauseType}, {@link RewindFastforwardType} and {@link NextPreviousType}) can
 * be mapped to an individual MQTT string. The same strings are used to recognise incoming MQTT messages.
 * If a string is not configured, the name of the corresponding enum constant (e.g. "PLAY") is used.
 * <p>
 * {@link NextPreviousType} is a pure command, so a received NEXT/PREVIOUS message is posted as a command to the
 * linked items instead of updating their state.
 *
 * @author Michael Lörcher - Initial contribution
 */
@NonNullByDefault
public class PlayerValue extends Value {
    private final String playString;
    private final String pauseString;
    private final String nextString;
    private final String previousString;
    private final String rewindString;
    private final String fastforwardString;

    /**
     * Creates a new player value.
     *
     * @param playString The string for PLAY. Defaults to "PLAY" if null or blank.
     * @param pauseString The string for PAUSE. Defaults to "PAUSE" if null or blank.
     * @param nextString The string for NEXT. Defaults to "NEXT" if null or blank.
     * @param previousString The string for PREVIOUS. Defaults to "PREVIOUS" if null or blank.
     * @param rewindString The string for REWIND. Defaults to "REWIND" if null or blank.
     * @param fastforwardString The string for FASTFORWARD. Defaults to "FASTFORWARD" if null or blank.
     */
    public PlayerValue(@Nullable String playString, @Nullable String pauseString, @Nullable String nextString,
            @Nullable String previousString, @Nullable String rewindString, @Nullable String fastforwardString) {
        // Incoming MQTT payloads are always parsed as strings and mapped in parseCommand()
        super(CoreItemFactory.PLAYER, List.of(StringType.class));
        this.playString = orDefault(playString, PlayPauseType.PLAY);
        this.pauseString = orDefault(pauseString, PlayPauseType.PAUSE);
        this.nextString = orDefault(nextString, NextPreviousType.NEXT);
        this.previousString = orDefault(previousString, NextPreviousType.PREVIOUS);
        this.rewindString = orDefault(rewindString, RewindFastforwardType.REWIND);
        this.fastforwardString = orDefault(fastforwardString, RewindFastforwardType.FASTFORWARD);
    }

    /**
     * Creates a new player value using the default strings (PLAY, PAUSE, NEXT, PREVIOUS, REWIND, FASTFORWARD).
     */
    public PlayerValue() {
        this(null, null, null, null, null, null);
    }

    private static String orDefault(@Nullable String value, Enum<?> defaultValue) {
        return value == null || value.isBlank() ? defaultValue.name() : value;
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof PlayPauseType || command instanceof RewindFastforwardType
                || command instanceof NextPreviousType) {
            return command;
        }
        if (command instanceof StringType) {
            final String value = command.toString();
            if (value.equals(playString)) {
                return PlayPauseType.PLAY;
            } else if (value.equals(pauseString)) {
                return PlayPauseType.PAUSE;
            } else if (value.equals(nextString)) {
                return NextPreviousType.NEXT;
            } else if (value.equals(previousString)) {
                return NextPreviousType.PREVIOUS;
            } else if (value.equals(rewindString)) {
                return RewindFastforwardType.REWIND;
            } else if (value.equals(fastforwardString)) {
                return RewindFastforwardType.FASTFORWARD;
            }
            throw new IllegalArgumentException("Value " + value + " not recognised as a player command");
        }
        throw new IllegalArgumentException(command.toString() + " is not a valid command for a player channel");
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        if (command == PlayPauseType.PLAY) {
            return playString;
        } else if (command == PlayPauseType.PAUSE) {
            return pauseString;
        } else if (command == NextPreviousType.NEXT) {
            return nextString;
        } else if (command == NextPreviousType.PREVIOUS) {
            return previousString;
        } else if (command == RewindFastforwardType.REWIND) {
            return rewindString;
        } else if (command == RewindFastforwardType.FASTFORWARD) {
            return fastforwardString;
        }
        // The output format is intentionally not applied to the player commands
        return command.toString();
    }
}
