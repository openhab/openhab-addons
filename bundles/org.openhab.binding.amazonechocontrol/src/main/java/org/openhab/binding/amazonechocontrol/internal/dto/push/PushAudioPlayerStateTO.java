/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.push;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PushAudioPlayerStateTO} encapsulates PUSH_AUDIO_PLAYER_STATE messages
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PushAudioPlayerStateTO extends PushDeviceTO {
    public String mediaReferenceId;
    public String quality;
    public boolean error;
    public AudioPlayerState audioPlayerState;
    public String errorMessage;

    @Override
    public @NonNull String toString() {
        return "PushAudioplayerStateTO{mediaReferenceId='" + mediaReferenceId + "', error=" + error
                + ", audioPlayerState=" + audioPlayerState + ", errorMessage='" + errorMessage
                + "', destinationUserId='" + destinationUserId + "', dopplerId=" + dopplerId + '}';
    }

    public enum AudioPlayerState {
        INTERRUPTED,
        FINISHED,
        PLAYING
    }
}
