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
public class ContentListResult_1_4 {

    /** The album name */
    private @Nullable String albumName;

    /** The artist */
    private @Nullable String artist;

    /** The audio codecs */
    private @Nullable AudioInfo @Nullable [] audioInfo;

    /** The broadcast frequency */
    private @Nullable Integer broadcastFreq;

    /** The broadcast frequency band */
    private @Nullable String broadcastFreqBand;

    /** The channel name */
    private @Nullable String channelName;

    /** The channel surfing visibility */
    private @Nullable String channelSurfingVisibility;

    /** The total chapter count */
    private @Nullable Integer chapterCount;

    /** The content information */
    private @Nullable ContentInfo content;

    /** THe content kind */
    private @Nullable String contentKind;

    /** The content type */
    private @Nullable String contentType;

    /** The created time */
    private @Nullable String createdTime;

    /** The direct remote number */
    private @Nullable Integer directRemoteNum;

    /** The content display number */
    private @Nullable String dispNum;

    /** The duration (in seconds) */
    private @Nullable Double durationMSec;

    /** The epg visibility */
    private @Nullable String epgVisibility;

    private @Nullable String fileNo;

    /** The file size (bytes) */
    private @Nullable Integer fileSizeByte;

    /** The folder number */
    private @Nullable String folderNo;

    /** The genre */
    private @Nullable String genre;

    /** The index */
    private @Nullable Integer index;

    /** The 3D setting */
    private @Nullable String is3D;

    /** Whether the content has already been played */
    private @Nullable String isAlreadyPlayed;

    /** Whether the content is browseable */
    private @Nullable String isBrowsable;

    /** Whether the content is playable */
    private @Nullable String isPlayable;

    /** Whether the content is protected */
    private @Nullable String isProtected;

    /** The original display number */
    private @Nullable String originalDispNum;

    /** The audio channels */
    private @Nullable ParentalInfo @Nullable [] parentalInfo;

    /** The parent index */
    private @Nullable Integer parentIndex;

    /** The parent URI */
    private @Nullable String parentUri;

    /** The current path */
    private @Nullable String path;

    /** The play list name */
    private @Nullable String playlistName;

    /** The podcast name */
    private @Nullable String podcastName;

    /** The product identifier */
    private @Nullable String productID;

    /** The program media type */
    private @Nullable String programMediaType;

    /** The program number */
    private @Nullable Integer programNum;

    /** The remote play type */
    private @Nullable String remotePlayType;

    /** The size (in MB) */
    private @Nullable Integer sizeMB;

    /** The start date time */
    private @Nullable String startDateTime;

    /** The storage uri (for usb, etc) */
    private @Nullable String storageUri;

    /** The audio frequencies */
    private @Nullable SubtitleInfo @Nullable [] subtitleInfo;

    /** The content title */
    private @Nullable String title;

    /** The triplet channel number */
    private @Nullable String tripletStr;

    /** The content uri */
    private @Nullable String uri;

    /** The user content flag */
    private @Nullable Boolean userContentFlag;

    /** The video information (codecs) */
    private @Nullable VideoInfo videoInfo;

    /** The visibility of the content */
    private @Nullable String visibility;

    /**
     * Constructor used for deserialization only
     */
    public ContentListResult_1_4() {
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
     * Gets the artist
     * 
     * @return the artist
     */
    public @Nullable String getArtist() {
        return artist;
    }

    /**
     * Gets the audio information
     * 
     * @return the audio information
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
     * Gets the content information
     * 
     * @return the content information
     */
    public @Nullable ContentInfo getContent() {
        return content;
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
     * Gets the content type
     * 
     * @return the content type
     */
    public @Nullable String getContentType() {
        return contentType;
    }

    /**
     * Gets the create time
     * 
     * @return the create time
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
     * Gets the duration in milliseconds
     * 
     * @return the duration in milliseconds
     */
    public @Nullable Double getDurationMSec() {
        return durationMSec;
    }

    /**
     * Gets the EPG visibility
     * 
     * @return the EPG visibility
     */
    public @Nullable String getEpgVisibility() {
        return epgVisibility;
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
     * Gets the file size in bytes
     * 
     * @return the file size in bytes
     */
    public @Nullable Integer getFileSizeByte() {
        return fileSizeByte;
    }

    /**
     * Gets the folder number
     * 
     * @return the folder number
     */
    public @Nullable String getFolderNo() {
        return folderNo;
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
    public @Nullable String is3D() {
        return is3D;
    }

    /**
     * Whether the content was already played
     * 
     * @return whether the content was already played
     */
    public @Nullable String isAlreadyPlayed() {
        return isAlreadyPlayed;
    }

    /**
     * Whether the content is browseable
     * 
     * @return whether the content is browseable
     */
    public @Nullable String isBrowsable() {
        return isBrowsable;
    }

    /**
     * Whether the content is playable
     * 
     * @return whether the content is playable
     */
    public @Nullable String isPlayable() {
        return isPlayable;
    }

    /**
     * Whether the content is protected
     * 
     * @return whether the content is protected
     */
    public @Nullable String isProtected() {
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
     * Gets the parental information
     * 
     * @return the parental information
     */
    public @Nullable ParentalInfo @Nullable [] getParentalInfo() {
        return parentalInfo;
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
     * Gets the parent URI
     * 
     * @return the parent URI
     */
    public @Nullable String getParentUri() {
        return parentUri;
    }

    /**
     * Gets the content path
     * 
     * @return the content path
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
     * Gets the podcast name
     * 
     * @return the podcast name
     */
    public @Nullable String getPodcastName() {
        return podcastName;
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
     * Gets the remote play type
     * 
     * @return the remote play type
     */
    public @Nullable String getRemotePlayType() {
        return remotePlayType;
    }

    /**
     * Gets the size in MB
     * 
     * @return the size in MB
     */
    public @Nullable Integer getSizeMB() {
        return sizeMB;
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
     * Gets the storage URI
     * 
     * @return the storage URI
     */
    public @Nullable String getStorageUri() {
        return storageUri;
    }

    /**
     * Gets the subtitle information
     * 
     * @return the subtitle information
     */
    public @Nullable SubtitleInfo @Nullable [] getSubtitleInfo() {
        return subtitleInfo;
    }

    /**
     * Gets the content title
     * 
     * @return the content title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the channel triplet string
     * 
     * @return the channel triplet string
     */
    public @Nullable String getTripletStr() {
        return tripletStr;
    }

    /**
     * Gets the content URI
     * 
     * @return the contentURI
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the content user flag
     * 
     * @return the content user flag
     */
    public @Nullable Boolean getUserContentFlag() {
        return userContentFlag;
    }

    /**
     * Gets the video information
     * 
     * @return the video information
     */
    public @Nullable VideoInfo getVideoInfo() {
        return videoInfo;
    }

    /**
     * Gets the content visibility
     * 
     * @return the content visibility
     */
    public @Nullable String getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "ContentListResult_1_4 [albumName=" + albumName + ", artist=" + artist + ", audioInfo="
                + Arrays.toString(audioInfo) + ", broadcastFreq=" + broadcastFreq + ", broadcastFreqBand="
                + broadcastFreqBand + ", channelName=" + channelName + ", channelSurfingVisibility="
                + channelSurfingVisibility + ", chapterCount=" + chapterCount + ", content=" + content
                + ", contentKind=" + contentKind + ", contentType=" + contentType + ", createdTime=" + createdTime
                + ", directRemoteNum=" + directRemoteNum + ", dispNum=" + dispNum + ", durationMSec=" + durationMSec
                + ", epgVisibility=" + epgVisibility + ", fileNo=" + fileNo + ", fileSizeByte=" + fileSizeByte
                + ", folderNo=" + folderNo + ", genre=" + genre + ", index=" + index + ", is3D=" + is3D
                + ", isAlreadyPlayed=" + isAlreadyPlayed + ", isBrowsable=" + isBrowsable + ", isPlayable=" + isPlayable
                + ", isProtected=" + isProtected + ", originalDispNum=" + originalDispNum + ", parentalInfo="
                + Arrays.toString(parentalInfo) + ", parentIndex=" + parentIndex + ", parentUri=" + parentUri
                + ", path=" + path + ", playlistName=" + playlistName + ", podcastName=" + podcastName + ", productID="
                + productID + ", programMediaType=" + programMediaType + ", programNum=" + programNum
                + ", remotePlayType=" + remotePlayType + ", sizeMB=" + sizeMB + ", startDateTime=" + startDateTime
                + ", storageUri=" + storageUri + ", subtitleInfo=" + Arrays.toString(subtitleInfo) + ", title=" + title
                + ", tripletStr=" + tripletStr + ", uri=" + uri + ", userContentFlag=" + userContentFlag
                + ", videoInfo=" + videoInfo + ", visibility=" + visibility + "]";
    }
}
