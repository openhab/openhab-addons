/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.arcam.internal.connection;

import static org.openhab.binding.arcam.internal.ArcamBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ArcamCommandCode} enum provides a way specify the supported commands and link them to a channel
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public enum ArcamCommandCode {
    // Non channel commands
    HEARTBEAT(""),
    SYSTEM_STATUS(""),

    // Generic channel commands
    DAC_FILTER(CHANNEL_DAC_FILTER),
    DISPLAY_BRIGHTNESS(CHANNEL_DISPLAY_BRIGHTNESS),
    HEADPHONES(CHANNEL_HEADPHONES),
    INCOMING_SAMPLE_RATE(CHANNEL_INCOMING_SAMPLE_RATE),
    LIFTER_TEMPERATURE(CHANNEL_LIFTER_TEMPERATURE),
    OUTPUT_TEMPERATURE(CHANNEL_OUTPUT_TEMPERATURE),
    REBOOT(CHANNEL_REBOOT),
    SOFTWARE_VERSION(CHANNEL_SOFTWARE_VERSION),
    TIMEOUT_COUNTER(CHANNEL_TIMEOUT_COUNTER),

    // Master channel commands
    MASTER_BALANCE(CHANNEL_MASTER_BALANCE),
    MASTER_DC_OFFSET(CHANNEL_DC_OFFSET),
    MASTER_DIRECT_MODE(CHANNEL_MASTER_DIRECT_MODE),
    MASTER_INPUT(CHANNEL_MASTER_INPUT),
    MASTER_INPUT_DETECT(CHANNEL_MASTER_INPUT_DETECT),
    MASTER_MUTE(CHANNEL_MASTER_MUTE),
    MASTER_NOW_PLAYING_ALBUM(CHANNEL_MASTER_NOW_PLAYING_ALBUM),
    MASTER_NOW_PLAYING_APPLICATION(CHANNEL_MASTER_NOW_PLAYING_APPLICATION),
    MASTER_NOW_PLAYING_ARTIST(CHANNEL_MASTER_NOW_PLAYING_ARTIST),
    MASTER_NOW_PLAYING_AUDIO_ENCODER(CHANNEL_MASTER_NOW_PLAYING_AUDIO_ENCODER),
    MASTER_NOW_PLAYING_SAMPLE_RATE(CHANNEL_MASTER_NOW_PLAYING_SAMPLE_RATE),
    MASTER_NOW_PLAYING_TITLE(CHANNEL_MASTER_NOW_PLAYING_TITLE),
    MASTER_POWER(CHANNEL_MASTER_POWER),
    MASTER_ROOM_EQUALISATION(CHANNEL_MASTER_ROOM_EQUALISATION),
    MASTER_SHORT_CIRCUIT(CHANNEL_MASTER_SHORT_CIRCUIT),
    MASTER_VOLUME(CHANNEL_MASTER_VOLUME),

    // Zone2 channel commands
    ZONE2_BALANCE(CHANNEL_ZONE2_BALANCE),
    ZONE2_DIRECT_MODE(CHANNEL_ZONE2_DIRECT_MODE),
    ZONE2_INPUT(CHANNEL_ZONE2_INPUT),
    ZONE2_MUTE(CHANNEL_ZONE2_MUTE),
    ZONE2_POWER(CHANNEL_ZONE2_POWER),
    ZONE2_ROOM_EQUALISATION(CHANNEL_ZONE2_ROOM_EQUALISATION),
    ZONE2_VOLUME(CHANNEL_ZONE2_VOLUME);

    public final String channel;

    private ArcamCommandCode(String channel) {
        this.channel = channel;
    }

    @Nullable
    public static ArcamCommandCode getFromChannel(String channel) {
        for (ArcamCommandCode c : ArcamCommandCode.values()) {
            if (c.channel.equalsIgnoreCase(channel)) {
                return c;
            }
        }
        return null;
    }
}
