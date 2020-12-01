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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the request to play content information and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PlayingContentInfoResult_1_2 extends PlayingContentInfoResult_1_0 {

    /** The album name */
    private @Nullable String albumName;

    /** The application name to the content */
    private @Nullable String applicationName;

    /** The artist */
    private @Nullable String artist;

    /** The audio information */
    private @Nullable AudioInfo @Nullable [] audioInfo;

    /** The broadcast frequency */
    private @Nullable Integer broadcastFreq;

    /** The broadcast frequency band */
    private @Nullable String broadcastFreqBand;

    /** The channel name */
    private @Nullable String channelName;

    /** The chapter count */
    private @Nullable Integer chapterCount;

    /** The chapter index */
    private @Nullable Integer chapterIndex;

    /** The content kind */
    private @Nullable String contentKind;

    /** The dab info */
    private @Nullable DabInfo dabInfo;

    /** The duration milliseconds of the content */
    private @Nullable Integer durationMsec;

    /** The file number? */
    private @Nullable String fileNo;

    /** The genre */
    private @Nullable String genre;

    /** The index of the content */
    private @Nullable Integer index;

    /** The index of the content */
    private @Nullable String is3D;

    /** The output of the content */
    private @Nullable String output;

    /** The parent index of the content */
    private @Nullable Integer parentIndex;

    /** The URI of the parent */
    private @Nullable String parentUri;

    /** The path to the content */
    private @Nullable String path;

    /** The playlist name */
    private @Nullable String playlistName;

    /** The play step speed of the content */
    private @Nullable Integer playStepSpeed;

    /** The podcast name */
    private @Nullable String podcastName;

    /** The position milliseconds of the content */
    private @Nullable Integer positionMsec;

    /** The position seconds of the content */
    private @Nullable Double positionSec;

    /** The repeat type of the content */
    private @Nullable String repeatType;

    /** The service of the content */
    private @Nullable String service;

    /** The source label of the content */
    private @Nullable String sourceLabel;

    /** The state info of the content */
    private @Nullable StateInfo stateInfo;

    /** The subtitle index */
    private @Nullable Integer subtitleIndex;

    /** The total count */
    private @Nullable Integer totalCount;

    /** The video information */
    private @Nullable VideoInfo videoInfo;

    /**
     * Constructor used for deserialization only
     */
    public PlayingContentInfoResult_1_2() {
    }

    /**
     * Gets the album name
     * 
     * @return the album name
     */
    public @Nullable String getAlbumName() {
        return albumName;
    }

    /**
     * Gets the application name
     * 
     * @return the application name
     */
    public @Nullable String getApplicationName() {
        return applicationName;
    }

    /**
     * Gets the artist
     * 
     * @return the artist
     */
    public @Nullable String getArtist() {
        return artist;
    }

    /**
     * Gets the audio info
     * 
     * @return the audio info
     */
    public @Nullable AudioInfo @Nullable [] getAudioInfo() {
        return audioInfo;
    }

    /**
     * Gets the broadcast frequency
     * 
     * @return the broadcast frequency
     */
    public @Nullable Integer getBroadcastFreq() {
        return broadcastFreq;
    }

    /**
     * Gets the broadcast frequency band
     * 
     * @return the broadcast frequency band
     */
    public @Nullable String getBroadcastFreqBand() {
        return broadcastFreqBand;
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
     * Gets the chapter count
     * 
     * @return the chapter count
     */
    public @Nullable Integer getChapterCount() {
        return chapterCount;
    }

    /**
     * Gets the chapter index
     * 
     * @return the chapter index
     */
    public @Nullable Integer getChapterIndex() {
        return chapterIndex;
    }

    /**
     * Gets the content kind
     * 
     * @return the content kind
     */
    public @Nullable String getContentKind() {
        return contentKind;
    }

    /**
     * Gets the DAB info
     * 
     * @return the DAB info
     */
    public @Nullable DabInfo getDabInfo() {
        return dabInfo;
    }

    /**
     * Gets the duration (in milliseconds)
     * 
     * @return the duration (in milliseconds)
     */
    public @Nullable Integer getDurationMsec() {
        return durationMsec;
    }

    /**
     * Gets the file number
     * 
     * @return the file number
     */
    public @Nullable String getFileNo() {
        return fileNo;
    }

    /**
     * Gets the genre
     * 
     * @return the genre
     */
    public @Nullable String getGenre() {
        return genre;
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
     * Gets the 3D setting
     * 
     * @return the 3D setting
     */
    public @Nullable String getIs3D() {
        return is3D;
    }

    /**
     * Gets the output
     * 
     * @return the output
     */
    public @Nullable String getOutput() {
        return output;
    }

    /**
     * Gets the output or a default value if output is empty
     * 
     * @param defValue a non-null, non-empty default value
     * @return a non-null, non-empty output value
     */
    public String getOutput(final String defValue) {
        Validate.notEmpty(defValue, "defValue cannot be empty");
        return StringUtils.defaultIfEmpty(output, defValue);
    }

    /**
     * Gets the parent index
     * 
     * @return the parent index
     */
    public @Nullable Integer getParentIndex() {
        return parentIndex;
    }

    /**
     * Gets the parent uri
     * 
     * @return the parent uri
     */
    public @Nullable String getParentUri() {
        return parentUri;
    }

    /**
     * Gets the path
     * 
     * @return the path
     */
    public @Nullable String getPath() {
        return path;
    }

    /**
     * Gets the play list name
     * 
     * @return the play list name
     */
    public @Nullable String getPlaylistName() {
        return playlistName;
    }

    /**
     * Gets the play step speed
     * 
     * @return the play step speed
     */
    public @Nullable Integer getPlayStepSpeed() {
        return playStepSpeed;
    }

    /**
     * Gets the podcast name
     * 
     * @return the podcast name
     */
    public @Nullable String getPodcastName() {
        return podcastName;
    }

    /**
     * Gets the position(in milliseconds)
     * 
     * @return the position(in milliseconds)
     */
    public @Nullable Integer getPositionMsec() {
        return positionMsec;
    }

    /**
     * Gets the position(in seconds)
     * 
     * @return the position(in seconds)
     */
    public @Nullable Double getPositionSec() {
        return positionSec;
    }

    /**
     * Gets the repeat type
     * 
     * @return the repeat type
     */
    public @Nullable String getRepeatType() {
        return repeatType;
    }

    /**
     * Gets the service
     * 
     * @return the service
     */
    public @Nullable String getService() {
        return service;
    }

    /**
     * Gets the source label
     * 
     * @return the source label
     */
    public @Nullable String getSourceLabel() {
        return sourceLabel;
    }

    /**
     * Gets the state information
     * 
     * @return the state information
     */
    public @Nullable StateInfo getStateInfo() {
        return stateInfo;
    }

    /**
     * Gets the subtitle index
     * 
     * @return the subtitle index
     */
    public @Nullable Integer getSubtitleIndex() {
        return subtitleIndex;
    }

    /**
     * Gets the total count
     * 
     * @return the total count
     */
    public @Nullable Integer getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the video information
     * 
     * @return the video information
     */
    public @Nullable VideoInfo getVideoInfo() {
        return videoInfo;
    }

    @Override
    public String toString() {
        return "PlayingContentInfoResult_1_2 [albumName=" + albumName + ", applicationName=" + applicationName
                + ", artist=" + artist + ", audioInfo=" + Arrays.toString(audioInfo) + ", broadcastFreq="
                + broadcastFreq + ", broadcastFreqBand=" + broadcastFreqBand + ", channelName=" + channelName
                + ", chapterCount=" + chapterCount + ", chapterIndex=" + chapterIndex + ", contentKind=" + contentKind
                + ", dabInfo=" + dabInfo + ", durationMsec=" + durationMsec + ", fileNo=" + fileNo + ", genre=" + genre
                + ", index=" + index + ", is3D=" + is3D + ", output=" + output + ", parentIndex=" + parentIndex
                + ", parentUri=" + parentUri + ", path=" + path + ", playlistName=" + playlistName + ", playStepSpeed="
                + playStepSpeed + ", podcastName=" + podcastName + ", positionMsec=" + positionMsec + ", positionSec="
                + positionSec + ", repeatType=" + repeatType + ", service=" + service + ", sourceLabel=" + sourceLabel
                + ", stateInfo=" + stateInfo + ", subtitleIndex=" + subtitleIndex + ", totalCount=" + totalCount
                + ", videoInfo=" + videoInfo + ", getBivlAssetId()=" + getBivlAssetId() + ", getBivlProvider()="
                + getBivlProvider() + ", getBivlServiceId()=" + getBivlServiceId() + ", getDispNum()=" + getDispNum()
                + ", getDurationSec()=" + getDurationSec() + ", getMediaType()=" + getMediaType()
                + ", getOriginalDispNum()=" + getOriginalDispNum() + ", getPlaySpeed()=" + getPlaySpeed()
                + ", getProgramNum()=" + getProgramNum() + ", getProgramTitle()=" + getProgramTitle() + ", getSource()="
                + getSource() + ", getStartDateTime()=" + getStartDateTime() + ", getTitle()=" + getTitle()
                + ", getTripletStr()=" + getTripletStr() + ", getUri()=" + getUri() + "]";
    }
}
