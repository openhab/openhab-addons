/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler.audio;

import java.util.Optional;

/**
 *
 * @author Brian O'Connell
 *
 */
public enum AudioPlayer {
    NUVO(1, 6, 8, 7, 9, 10),
    NUVO_GRAND_ESSENTIA_SIMPLESE(2, 6, 8, 7, 9, 10),
    NUVO_GRAND_GRAND_CONCERTO(3, 6, 6, 6, 9, 10),
    RUSSOUND(4, 6, 8, 7, 11, 12),
    XANTECH(6, 13, 15, 14, 16, 17),
    SPEAKERCRAFT(7, 45, 44, 46, 42, 43),
    PROFICIENT(8, 45, 44, 46, 42, 43);

    final int featureCode;
    final int playCommand;
    final int pauseCommand;
    final int stopCommand;
    final int previousCommand;
    final int nextCommand;

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
        for (AudioPlayer player : values()) {
            if (player.featureCode == featureCode) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

}
