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
package org.openhab.binding.omnilink.internal;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AudioPlayer} defines some methods that are used to
 * interface with an OmniLink Audio Player.
 *
 * @author Brian O'Connell - Initial contribution
 */
@NonNullByDefault
public enum AudioPlayer {
    NUVO(1, 6, 8, 7, 9, 10),
    NUVO_GRAND_ESSENTIA_SIMPLESE(2, 6, 8, 7, 9, 10),
    NUVO_GRAND_GRAND_CONCERTO(3, 6, 6, 6, 9, 10),
    RUSSOUND(4, 6, 8, 7, 11, 12),
    XANTECH(6, 13, 15, 14, 16, 17),
    SPEAKERCRAFT(7, 45, 44, 46, 42, 43),
    PROFICIENT(8, 45, 44, 46, 42, 43);

    private final int featureCode;
    private final int playCommand;
    private final int pauseCommand;
    private final int stopCommand;
    private final int previousCommand;
    private final int nextCommand;

    AudioPlayer(int featureCode, int playCommand, int pauseCommand, int stopCommand, int previousCommand,
            int nextCommand) {
        this.featureCode = featureCode;
        this.playCommand = playCommand;
        this.pauseCommand = pauseCommand;
        this.stopCommand = stopCommand;
        this.previousCommand = previousCommand;
        this.nextCommand = nextCommand;
    }

    public int getPlayCommand() {
        return playCommand;
    }

    public int getPauseCommand() {
        return pauseCommand;
    }

    public int getStopCommand() {
        return stopCommand;
    }

    public int getPreviousCommand() {
        return previousCommand;
    }

    public int getNextCommand() {
        return nextCommand;
    }

    public static Optional<AudioPlayer> getAudioPlayerForFeatureCode(int featureCode) {
        return Arrays.stream(values()).filter(v -> v.featureCode == featureCode).findAny();
    }
}
