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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PlayerStateInfoTO} encapsulates the information about a player
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PlayerStateInfoTO {
    public String queueId;
    public String mediaId;
    @SerializedName(value = "state", alternate = { "playerState" })
    public String state;
    public PlayerStateInfoTextTO infoText = new PlayerStateInfoTextTO();
    public PlayerStateInfoTextTO miniInfoText = new PlayerStateInfoTextTO();
    public PlayerStateProviderTO provider = new PlayerStateProviderTO();
    public PlayerStateVolumeTO volume = new PlayerStateVolumeTO();
    public PlayerStateMainArtTO mainArt = new PlayerStateMainArtTO();
    public PlayerStateProgressTO progress = new PlayerStateProgressTO();
    public PlayerStateMediaReferenceTO mediaReference = new PlayerStateMediaReferenceTO();

    @Override
    public @NonNull String toString() {
        return "PlayerStateInfoTO{queueId='" + queueId + "', mediaId='" + mediaId + "', state='" + state
                + "', infoText=" + infoText + ", miniInfoText=" + miniInfoText + ", provider=" + provider + ", volume="
                + volume + ", mainArt=" + mainArt + ", progress=" + progress + ", mediaReference=" + mediaReference
                + "}";
    }
}
