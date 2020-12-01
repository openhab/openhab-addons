/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a content list result and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentListResult_1_0 {

    /** The channel name */
    private @Nullable String channelName;

    /** The direct remote number */
    private @Nullable Integer directRemoteNum;

    /** The content display number */
    private @Nullable String dispNum;

    /** The duration (in seconds) */
    private @Nullable Integer durationSec;

    /** The file size (bytes) */
    private @Nullable Integer fileSizeByte;

    /** The index. */
    private @Nullable Integer index;

    /** Whether the content has already been played */
    private @Nullable Boolean isAlreadyPlayed;

    /** Whether the content is protected */
    private @Nullable Boolean isProtected;

    /** The original display number. */
    private @Nullable String originalDispNum;

    /** The program media type */
    private @Nullable String programMediaType;

    /** The program number */
    private @Nullable Integer programNum;

    /** The start date time */
    private @Nullable String startDateTime;

    /** The content title */
    private @Nullable String title;

    /** The triplet channel number */
    private @Nullable String tripletStr;

    /** The content uri */
    private @Nullable String uri;

    /**
     * Constructor used for deserialization only
     */
    public ContentListResult_1_0() {
    }

    /**
     * Gets the channel name
     * 
     * @return the channel name
     */
    public @Nullable String getChannelName() {
        return channelName;
    }

    /**
     * Gets the direct remote number
     * 
     * @return the direct remote number
     */
    public @Nullable Integer getDirectRemoteNum() {
        return directRemoteNum;
    }

    /**
     * Gets the display number
     * 
     * @return the display number
     */
    public @Nullable String getDispNum() {
        return dispNum;
    }

    /**
     * Gets the duration in seconds
     * 
     * @return the duration in seconds
     */
    public @Nullable Integer getDurationSec() {
        return durationSec;
    }

    /**
     * Gets the file size (in bytes)
     * 
     * @return the file size
     */
    public @Nullable Integer getFileSizeByte() {
        return fileSizeByte;
    }

    /**
     * Gets the index position
     * 
     * @return the index position
     */
    public @Nullable Integer getIndex() {
        return index;
    }

    /**
     * Gets whether the content has already been played
     * 
     * @return whether the content has already been played
     */
    public @Nullable Boolean isAlreadyPlayed() {
        return isAlreadyPlayed;
    }

    /**
     * Gets whether the content is protected
     * 
     * @return whether the content is protected
     */
    public @Nullable Boolean isProtected() {
        return isProtected;
    }

    /**
     * Gets the original display number
     * 
     * @return the original display number
     */
    public @Nullable String getOriginalDispNum() {
        return originalDispNum;
    }

    /**
     * Gets the program media type
     * 
     * @return the program media type
     */
    public @Nullable String getProgramMediaType() {
        return programMediaType;
    }

    /**
     * Gets the program number
     * 
     * @return the program number
     */
    public @Nullable Integer getProgramNum() {
        return programNum;
    }

    /**
     * Gets the start date/time
     * 
     * @return the start date/time
     */
    public @Nullable String getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the content title
     * 
     * @return the content tile
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the content triplet channel string
     * 
     * @return the triplet channel string
     */
    public @Nullable String getTripletStr() {
        return tripletStr;
    }

    /**
     * Gets the content URI
     * 
     * @return the content URI
     */
    public @Nullable String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ContentListResult_1_0 [channelName=" + channelName + ", directRemoteNum=" + directRemoteNum
                + ", dispNum=" + dispNum + ", durationSec=" + durationSec + ", fileSizeByte=" + fileSizeByte
                + ", index=" + index + ", isAlreadyPlayed=" + isAlreadyPlayed + ", isProtected=" + isProtected
                + ", originalDispNum=" + originalDispNum + ", programMediaType=" + programMediaType + ", programNum="
                + programNum + ", startDateTime=" + startDateTime + ", title=" + title + ", tripletStr=" + tripletStr
                + ", uri=" + uri + "]";
    }
}
