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
package org.openhab.binding.heos.internal.json.payload;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for browse result types from the HEOS cli
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public enum BrowseResultType {
    @SerializedName("artist")
    ARTIST,
    @SerializedName("album")
    ALBUM,
    @SerializedName("song")
    SONG,
    @SerializedName("container")
    CONTAINER,
    @SerializedName("station")
    STATION,
    @SerializedName("playlist")
    PLAYLIST
}
