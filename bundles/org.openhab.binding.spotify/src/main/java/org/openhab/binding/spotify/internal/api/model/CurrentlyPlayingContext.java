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
package org.openhab.binding.spotify.internal.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Spotify Web API Currently Playing data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class CurrentlyPlayingContext {

    private long timestamp;
    private long progressMs;
    @SerializedName("is_playing")
    private boolean playing;
    private Item item;
    private Context context;
    private Device device;
    private String repeatState;
    private boolean shuffleState;

    public long getTimestamp() {
        return timestamp;
    }

    public long getProgressMs() {
        return progressMs;
    }

    public boolean isPlaying() {
        return playing;
    }

    public Item getItem() {
        return item;
    }

    public Context getContext() {
        return context;
    }

    public Device getDevice() {
        return device;
    }

    public String getRepeatState() {
        return repeatState;
    }

    public boolean isShuffleState() {
        return shuffleState;
    }
}
