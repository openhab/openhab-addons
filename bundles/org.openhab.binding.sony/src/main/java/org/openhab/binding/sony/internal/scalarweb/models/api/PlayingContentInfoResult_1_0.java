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
 * This class represents the request to play content information and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PlayingContentInfoResult_1_0 {

    /** The BIVL asset ID */
    private @Nullable String bivlAssetId;

    /** The BIVL provider */
    private @Nullable String bivlProvider;

    /** The Bravia Internet Video Link (BIVL) service id */
    private @Nullable String bivlServiceId;

    /** The display number */
    private @Nullable String dispNum;

    /** The duration (in seconds) */
    private @Nullable Double durationSec;

    /** The media type */
    private @Nullable String mediaType;

    /** The original display number */
    private @Nullable String originalDispNum;

    /** The play speed */
    private @Nullable String playSpeed;

    /** The program number */
    private @Nullable Integer programNum;

    /** The program title */
    private @Nullable String programTitle;

    /** The source of the content */
    private @Nullable String source;

    /** The start date time */
    private @Nullable String startDateTime;

    /** The title of the content */
    private @Nullable String title;

    /** The triplet string identifier */
    private @Nullable String tripletStr;

    /** The uri of the content */
    private @Nullable String uri;

    /**
     * Constructor used for deserialization only
     */
    public PlayingContentInfoResult_1_0() {
    }

    /**
     * Gets the BIVL asset id
     *
     * @return the BIVL asset id
     */
    public @Nullable String getBivlAssetId() {
        return bivlAssetId;
    }

    /**
     * Gets the BIVL provider
     *
     * @return the BIVL provider
     */
    public @Nullable String getBivlProvider() {
        return bivlProvider;
    }

    /**
     * Gets the BIVL service id
     *
     * @return the BIVL service id
     */
    public @Nullable String getBivlServiceId() {
        return bivlServiceId;
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
     * Gets the duration (in seconds)
     *
     * @return the duration (in seconds)
     */
    public @Nullable Double getDurationSec() {
        return durationSec;
    }

    /**
     * Gets the media type
     *
     * @return the media type
     */
    public @Nullable String getMediaType() {
        return mediaType;
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
     * Gets the play speed
     *
     * @return the play speed
     */
    public @Nullable String getPlaySpeed() {
        return playSpeed;
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
     * Gets the program title
     *
     * @return the program title
     */
    public @Nullable String getProgramTitle() {
        return programTitle;
    }

    /**
     * Gets the source of the content
     *
     * @return the source of the content
     */
    public @Nullable String getSource() {
        return source;
    }

    /**
     * Gets the start date time
     *
     * @return the start date time
     */
    public @Nullable String getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the title
     *
     * @return the title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the triplet string identifier
     *
     * @return the triplet string identifier
     */
    public @Nullable String getTripletStr() {
        return tripletStr;
    }

    /**
     * Gets the uri of the ocntent
     *
     * @return the uri of the content
     */
    public @Nullable String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "PlayingContentInfoResult_1_0 [bivlAssetId=" + bivlAssetId + ", bivlProvider=" + bivlProvider
                + ", bivlServiceId=" + bivlServiceId + ", dispNum=" + dispNum + ", durationSec=" + durationSec
                + ", mediaType=" + mediaType + ", originalDispNum=" + originalDispNum + ", playSpeed=" + playSpeed
                + ", programNum=" + programNum + ", programTitle=" + programTitle + ", source=" + source
                + ", startDateTime=" + startDateTime + ", title=" + title + ", tripletStr=" + tripletStr + ", uri="
                + uri + "]";
    }
}
