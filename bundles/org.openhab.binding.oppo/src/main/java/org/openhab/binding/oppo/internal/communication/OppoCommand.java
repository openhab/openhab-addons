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
package org.openhab.binding.oppo.internal.communication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different kinds of commands
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum OppoCommand {
    POWER_ON("PON"),
    POWER_OFF("POF"),
    PLAY("PLA"),
    PAUSE("PAU"),
    PREV("PRE"),
    REWIND("REV"),
    FFORWARD("FWD"),
    NEXT("NXT"),
    MUTE("MUT"),
    QUERY_POWER_STATUS("QPW"),
    QUERY_FIRMWARE_VERSION("QVR"),
    QUERY_VOLUME("QVL"),
    QUERY_HDMI_RESOLUTION("QHD"),
    QUERY_HDR_SETTING("QHR"),
    QUERY_PLAYBACK_STATUS("QPL"),
    QUERY_TITLE_TRACK("QTK"),
    QUERY_CHAPTER("QCH"),
    QUERY_TITLE_ELAPSED("QTE"),
    QUERY_TITLE_REMAIN("QTR"),
    QUERY_CHAPTER_ELAPSED("QCE"),
    QUERY_CHAPTER_REMAIN("QCR"),
    QUERY_DISC_TYPE("QDT"),
    QUERY_AUDIO_TYPE("QAT"),
    QUERY_SUBTITLE_TYPE("QST"),
    QUERY_SUBTITLE_SHIFT("QSH"),
    QUERY_OSD_POSITION("QOP"),
    QUERY_REPEAT_MODE("QRP"),
    QUERY_ZOOM_MODE("QZM"),
    QUERY_INPUT_SOURCE("QIS"),
    SET_VERBOSE_MODE("SVM"),
    SET_HDMI_MODE("SHD"),
    SET_HDR_MODE("SHR"),
    SET_ZOOM_RATIO("SZM"),
    SET_VOLUME_LEVEL("SVL"),
    SET_REPEAT("SRP"),
    SET_SUBTITLE_SHIFT("SSH"),
    SET_OSD_POSITION("SOP"),
    SET_TIME_DISPLAY("STC"),
    SET_INPUT_SOURCE("SIS"),
    NO_OP("NOP");

    private final String value;

    public static final Set<OppoCommand> QUERY_COMMANDS = new HashSet<>(
            Arrays.asList(QUERY_VOLUME, QUERY_HDMI_RESOLUTION, QUERY_HDR_SETTING, QUERY_PLAYBACK_STATUS,
                    QUERY_DISC_TYPE, QUERY_AUDIO_TYPE, QUERY_SUBTITLE_SHIFT, QUERY_OSD_POSITION, QUERY_REPEAT_MODE,
                    QUERY_ZOOM_MODE, QUERY_INPUT_SOURCE, QUERY_FIRMWARE_VERSION));

    OppoCommand(String value) {
        this.value = value;
    }

    /**
     * Get the command name
     *
     * @return the command name
     */
    public String getValue() {
        return value;
    }
}
