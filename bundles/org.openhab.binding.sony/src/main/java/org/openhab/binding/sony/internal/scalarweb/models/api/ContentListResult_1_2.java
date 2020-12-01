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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a content list result and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentListResult_1_2 {

    /** The audio channels */
    private @Nullable String @Nullable [] audioChannel;

    /** The audio codecs */
    private @Nullable String @Nullable [] audioCodec;

    /** The audio frequencies */
    private @Nullable String @Nullable [] audioFrequency;

    /** The channel name */
    private @Nullable String channelName;

    /** The channel surfing visibility */
    private @Nullable String channelSurfingVisibility;

    /** The total chapter count */
    private @Nullable Integer chapterCount;

    /** The content type */
    private @Nullable String contentType;

    /** The created time */
    private @Nullable String createdTime;

    /** The direct remote number */
    private @Nullable Integer directRemoteNum;

    /** The content display number */
    private @Nullable String dispNum;

    /** The duration (in seconds) */
    private @Nullable Double durationSec;

    /** The epg visibility */
    private @Nullable String epgVisibility;

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

    /** The parental countries */
    private @Nullable String @Nullable [] parentalCountry;

    /** The parental ratings */
    private @Nullable String @Nullable [] parentalRating;

    /** The parental systems */
    private @Nullable String @Nullable [] parentalSystem;

    /** The product identifier */
    private @Nullable String productID;

    /** The program media type */
    private @Nullable String programMediaType;

    /** The program number */
    private @Nullable Integer programNum;

    /** The size (in MB) */
    private @Nullable Integer sizeMB;

    /** The start date time */
    private @Nullable String startDateTime;

    /** The storage uri (for usb, etc) */
    private @Nullable String storageUri;

    /** The subtitle languages */
    private @Nullable String @Nullable [] subtitleLanguage;

    /** The subtitle titles */
    private @Nullable String @Nullable [] subtitleTitle;

    /** The content title */
    private @Nullable String title;

    /** The triplet channel number */
    private @Nullable String tripletStr;

    /** The content uri */
    private @Nullable String uri;

    /** The user content flag */
    private @Nullable Boolean userContentFlag;

    /** The video codec */
    private @Nullable String videoCodec;

    /** The visibility of the content */
    private @Nullable String visibility;

    /**
     * Constructor used for deserialization only
     */
    public ContentListResult_1_2() {
    }

    /**
     * Gets the audio channel
     *
     * @return the audio channel
     */
    public @Nullable String @Nullable [] getAudioChannel() {
        return audioChannel;
    }

    /**
     * Gets the audio codec
     *
     * @return the audio codec
     */
    public @Nullable String @Nullable [] getAudioCodec() {
        return audioCodec;
    }

    /**
     * Gets the audio frequency
     *
     * @return the audio frequency
     */
    public @Nullable String @Nullable [] getAudioFrequency() {
        return audioFrequency;
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
     * Gets the channel surfing visibility
     *
     * @return the channel surfing visibility
     */
    public @Nullable String getChannelSurfingVisibility() {
        return channelSurfingVisibility;
    }

    /**
     * Gets the chapter count
     *
     * @return the chapter count
     */
    public @Nullable Integer getChapterCount() {
        return chapterCount;
    }

    /**
     * Gets the content type
     *
     * @return the content type
     */
    public @Nullable String getContentType() {
        return contentType;
    }

    /**
     * Gets the created time
     *
     * @return the created time
     */
    public @Nullable String getCreatedTime() {
        return createdTime;
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
     * Gets the duration (in seconds)
     *
     * @return the duration (in seconds)
     */
    public @Nullable Double getDurationSec() {
        return durationSec;
    }

    /**
     * Gets the epg visibility
     *
     * @return the epg visibility
     */
    public @Nullable String getEpgVisibility() {
        return epgVisibility;
    }

    /**
     * Gets the file size byte
     *
     * @return the file size byte
     */
    public @Nullable Integer getFileSizeByte() {
        return fileSizeByte;
    }

    /**
     * Gets the index
     *
     * @return the index
     */
    public @Nullable Integer getIndex() {
        return index;
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
     * Gets the parental country
     *
     * @return the parental country
     */
    public @Nullable String @Nullable [] getParentalCountry() {
        return parentalCountry;
    }

    /**
     * Gets the parental rating
     *
     * @return the parental rating
     */
    public @Nullable String @Nullable [] getParentalRating() {
        return parentalRating;
    }

    /**
     * Gets the parental system
     *
     * @return the parental system
     */
    public @Nullable String @Nullable [] getParentalSystem() {
        return parentalSystem;
    }

    /**
     * Gets the product ID
     *
     * @return the product ID
     */
    public @Nullable String getProductID() {
        return productID;
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
     * Gets the size (in MB)
     *
     * @return the size (in MB)
     */
    public @Nullable Integer getSizeMB() {
        return sizeMB;
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
     * Gets the storage uri
     *
     * @return the storage uri
     */
    public @Nullable String getStorageUri() {
        return storageUri;
    }

    /**
     * Gets the subtitle language
     *
     * @return the subtitle language
     */
    public @Nullable String @Nullable [] getSubtitleLanguage() {
        return subtitleLanguage;
    }

    /**
     * Gets the subtitle title
     *
     * @return the subtitle title
     */
    public @Nullable String @Nullable [] getSubtitleTitle() {
        return subtitleTitle;
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
     * Gets the triplet string
     *
     * @return the triplet string
     */
    public @Nullable String getTripletStr() {
        return tripletStr;
    }

    /**
     * Gets the uri of the content (ie the identifier)
     *
     * @return the uri of the content
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the video codec
     *
     * @return the video codec
     */
    public @Nullable String getVideoCodec() {
        return videoCodec;
    }

    /**
     * Gets the overall visibility
     *
     * @return the overall visibility
     */
    public @Nullable String getVisibility() {
        return visibility;
    }

    /**
     * Checks if the media was already played
     *
     * @return true if played, false otherwise
     */
    public @Nullable Boolean isAlreadyPlayed() {
        return isAlreadyPlayed;
    }

    /**
     * Checks if media is protected
     *
     * @return true if protected - false otherwise
     */
    public @Nullable Boolean isProtected() {
        return isProtected;
    }

    /**
     * Checks the user content flag
     *
     * @return true if user content, false otherwise
     */
    public @Nullable Boolean isUserContentFlag() {
        return userContentFlag;
    }

    @Override
    public String toString() {
        return "ContentListResult_1_2 [audioChannel=" + Arrays.toString(audioChannel) + ", audioCodec="
                + Arrays.toString(audioCodec) + ", audioFrequency=" + Arrays.toString(audioFrequency) + ", channelName="
                + channelName + ", channelSurfingVisibility=" + channelSurfingVisibility + ", chapterCount="
                + chapterCount + ", contentType=" + contentType + ", createdTime=" + createdTime + ", directRemoteNum="
                + directRemoteNum + ", dispNum=" + dispNum + ", durationSec=" + durationSec + ", epgVisibility="
                + epgVisibility + ", fileSizeByte=" + fileSizeByte + ", index=" + index + ", isAlreadyPlayed="
                + isAlreadyPlayed + ", isProtected=" + isProtected + ", originalDispNum=" + originalDispNum
                + ", parentalCountry=" + Arrays.toString(parentalCountry) + ", parentalRating="
                + Arrays.toString(parentalRating) + ", parentalSystem=" + Arrays.toString(parentalSystem)
                + ", productID=" + productID + ", programMediaType=" + programMediaType + ", programNum=" + programNum
                + ", sizeMB=" + sizeMB + ", startDateTime=" + startDateTime + ", storageUri=" + storageUri
                + ", subtitleLanguage=" + Arrays.toString(subtitleLanguage) + ", subtitleTitle="
                + Arrays.toString(subtitleTitle) + ", title=" + title + ", tripletStr=" + tripletStr + ", uri=" + uri
                + ", userContentFlag=" + userContentFlag + ", videoCodec=" + videoCodec + ", visibility=" + visibility
                + "]";
    }
}
