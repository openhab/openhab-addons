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
package org.openhab.binding.emby.internal.model;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * embyPlayState - part of the model for the json object received from the server
 *
 * @author Zachary Christiansen - Initial Contribution
 *
 */
@NonNullByDefault
public class EmbyPlayState {

    @SerializedName("PositionTicks")
    private BigDecimal positionTicks = BigDecimal.ZERO;

    @SerializedName("CanSeek")
    private boolean canSeek = false;

    @SerializedName("IsPaused")
    private boolean isPaused = false;

    @SerializedName("IsMuted")
    private boolean isMuted = false;

    @SerializedName("VolumeLevel")
    private Integer volumeLevel = 0;

    @SerializedName("MediaSourceId")
    private String mediaSoureId = "";

    @SerializedName("PlayMethod")
    private String playMethod = "";

    @SerializedName("repeatMode")
    private String repeatMode = "";

    /**
     * @return the current position in the playback of the now playing item, can be compared to the total
     *         runtimeticks
     *         to get percentage played
     */
    BigDecimal getPositionTicks() {
        return positionTicks;
    }

    boolean getPaused() {
        return isPaused;
    }

    boolean getIsMuted() {
        return isMuted;
    }

    /**
     * @return the item id of the now playing item
     */
    String getMediaSourceID() {
        return mediaSoureId;
    }
}
