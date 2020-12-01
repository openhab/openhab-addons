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
public class ContentListResult_1_5 {

    /** The album name */
    private @Nullable String albumName;

    /** The application name */
    private @Nullable String applicationName;

    /** The artist */
    private @Nullable String artist;

    /** The audio informaiton */
    private @Nullable AudioInfo @Nullable [] audioInfo;

    /** The bravia internet video link information */
    private @Nullable BivlInfo bivlInfo;

    /** The broadcast frequency information */
    private @Nullable BroadcastFreq broadcastFreq;

    /** The broadcase genre information */
    private @Nullable BroadcastGenreInfo @Nullable [] broadcastGenreInfo;

    /** The channel name */
    private @Nullable String channelName;

    /** The chapter count */
    private @Nullable Integer chapterCount;

    /** The chapter index */
    private @Nullable Integer chapterIndex;

    /** The clip count */
    private @Nullable Integer clipCount;

    /** The content information */
    private @Nullable ContentInfo content;

    /** The content kind */
    private @Nullable String contentKind;

    /** The content type */
    private @Nullable String contentType;

    /** The created time */
    private @Nullable String createdTime;

    /** The digital audio broadcast info */
    private @Nullable DabInfo dabInfo;

    /** The data broadcast info */
    private @Nullable DataBroadcastInfo dataInfo;

    /** The description */
    private @Nullable Description description;

    /** The direct remote number */
    private @Nullable Integer directRemoteNum;

    /** The content display number */
    private @Nullable String dispNum;

    /** The dubbing information */
    private @Nullable DubbingInfo dubbingInfo;

    /** The duration */
    private @Nullable Duration duration;

    /** The event id */
    private @Nullable String eventId;

    /** The file number */
    private @Nullable String fileNo;

    /** The file size (bytes) */
    private @Nullable Integer fileSizeByte;

    /** The folder number */
    private @Nullable String folderNo;

    /** The genre */
    private @Nullable String genre;

    /** The global playback count */
    private @Nullable Integer globalPlaybackCount;

    /** The group information */
    private @Nullable GroupInfo @Nullable [] groupInfo;

    /** Whether the content has resumed */
    private @Nullable String hasResume;

    /** The index */
    private @Nullable Integer index;

    /** The 3D setting */
    private @Nullable String is3D;

    /** The 4K setting */
    private @Nullable String is4K;

    /** Whether the content has already been played */
    private @Nullable String isAlreadyPlayed;

    /** Whether the content is set to auto delete */
    private @Nullable String isAutoDelete;

    /** Whether the content is browseable */
    private @Nullable String isBrowsable;

    /** Whether the content is new */
    private @Nullable String isNew;

    /** Whether the content is playable */
    private @Nullable String isPlayable;

    /** Whether the content is a play list */
    private @Nullable String isPlaylist;

    /** Whether the content is protected */
    private @Nullable String isProtected;

    /** Whether the content is a sound photo */
    private @Nullable String isSoundPhoto;

    /** The content media type */
    private @Nullable String mediaType;

    /** The original display number */
    private @Nullable String originalDispNum;

    /** The output */
    private @Nullable String output;

    /** The parental information */
    private @Nullable ParentalInfo @Nullable [] parentalInfo;

    /** The parent index */
    private @Nullable Integer parentIndex;

    /** The parent URL */
    private @Nullable String parentUri;

    /** The play list information */
    private @Nullable PlaylistInfo @Nullable [] playlistInfo;

    /** The play list name */
    private @Nullable String playlistName;

    /** The play speed */
    private @Nullable Speed playSpeed;

    /** The podcast name */
    private @Nullable String podcastName;

    /** Really doubt this is PIP position */
    private @Nullable Position position;

    /** The product identifier */
    private @Nullable String productID;

    /** The program media type */
    private @Nullable String programMediaType;

    /** The program number */
    private @Nullable Integer programNum;

    /** The program service type */
    private @Nullable String programServiceType;

    /** The program title */
    private @Nullable String programTitle;

    /** The recording information */
    private @Nullable RecordingInfo recordingInfo;

    /** The remote play type */
    private @Nullable String remotePlayType;

    /** The repeat type */
    private @Nullable String repeatType;

    /** The service */
    private @Nullable String service;

    /** The size (in MB) */
    private @Nullable Integer sizeMB;

    /** The content source */
    private @Nullable String source;

    /** The content source label */
    private @Nullable String sourceLabel;

    /** The start date time */
    private @Nullable String startDateTime;

    /** The state information */
    private @Nullable StateInfo stateInfo;

    /** The storage uri (for usb, etc) */
    private @Nullable String storageUri;

    /** The subtitle information */
    private @Nullable SubtitleInfo @Nullable [] subtitleInfo;

    /** The content sync priority */
    private @Nullable String syncContentPriority;

    /** The content title */
    private @Nullable String title;

    /** The content total count */
    private @Nullable Integer totalCount;

    /** The triplet channel number */
    private @Nullable String tripletStr;

    /** The content uri */
    private @Nullable String uri;

    /** The user content flag */
    private @Nullable Boolean userContentFlag;

    /** The content video information */
    private @Nullable VideoInfo @Nullable [] videoInfo;

    /** The visibility of the content */
    private @Nullable Visibility visibility;

    /**
     * Constructor used for deserialization only
     */
    public ContentListResult_1_5() {
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
     * Gets the audio information
     * 
     * @return the audio information
     */
    public @Nullable AudioInfo @Nullable [] getAudioInfo() {
        return audioInfo;
    }

    /**
     * Gets the BIVL info
     * 
     * @return the BIVL info
     */
    public @Nullable BivlInfo getBivlInfo() {
        return bivlInfo;
    }

    /**
     * Gets the broadcast frequency info
     * 
     * @return the broadcast frequency info
     */
    public @Nullable BroadcastFreq getBroadcastFreq() {
        return broadcastFreq;
    }

    /**
     * Gets the broadcast genre info
     * 
     * @return the broadcast genre info
     */
    public @Nullable BroadcastGenreInfo @Nullable [] getBroadcastGenreInfo() {
        return broadcastGenreInfo;
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
     * Gets the clip count
     * 
     * @return the clip count
     */
    public @Nullable Integer getClipCount() {
        return clipCount;
    }

    /**
     * Gets the content info
     * 
     * @return the content info
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
     * Gets the created time
     * 
     * @return the created time
     */
    public @Nullable String getCreatedTime() {
        return createdTime;
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
     * Gets the broadcast data information
     * 
     * @return the broadcast data information
     */
    public @Nullable DataBroadcastInfo getDataInfo() {
        return dataInfo;
    }

    /**
     * Gets the description
     * 
     * @return the description
     */
    public @Nullable Description getDescription() {
        return description;
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
     * Gets the dubbing information
     * 
     * @return the dubbing information
     */
    public @Nullable DubbingInfo getDubbingInfo() {
        return dubbingInfo;
    }

    /**
     * Gets the duration
     * 
     * @return the duration
     */
    public @Nullable Duration getDuration() {
        return duration;
    }

    /**
     * Gets the event ID
     * 
     * @return the event ID
     */
    public @Nullable String getEventId() {
        return eventId;
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
     * Gets the file size (in bytes)
     * 
     * @return the file size (in bytes)
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
     * Gets the global playback count
     * 
     * @return the global playback count
     */
    public @Nullable Integer getGlobalPlaybackCount() {
        return globalPlaybackCount;
    }

    /**
     * Gets the group info
     * 
     * @return the group info
     */
    public @Nullable GroupInfo @Nullable [] getGroupInfo() {
        return groupInfo;
    }

    /**
     * Gets the has resume
     * 
     * @return the has resume
     */
    public @Nullable String getHasResume() {
        return hasResume;
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
     * Gets the 4K setting
     * 
     * @return the 4K setting
     */
    public @Nullable String is4K() {
        return is4K;
    }

    /**
     * Whether the content has already played
     * 
     * @return whether the content has already played
     */
    public @Nullable String isAlreadyPlayed() {
        return isAlreadyPlayed;
    }

    /**
     * Whether the content auto deletes
     * 
     * @return whether the content auto deletes
     */
    public @Nullable String isAutoDelete() {
        return isAutoDelete;
    }

    /**
     * Whether the content is browesable
     * 
     * @return whether the content is browesable
     */
    public @Nullable String isBrowsable() {
        return isBrowsable;
    }

    /**
     * Whether the content is new
     * 
     * @return whether the content is new
     */
    public @Nullable String isNew() {
        return isNew;
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
     * Whether the content is a play list
     * 
     * @return whether the content is a play list
     */
    public @Nullable String isPlaylist() {
        return isPlaylist;
    }

    /**
     * Whether the content is protected
     * 
     * @return Whether the content is protected
     */
    public @Nullable String isProtected() {
        return isProtected;
    }

    /**
     * Whether the content is a sound photo
     * 
     * @return whether the content is a sound photo
     */
    public @Nullable String isSoundPhoto() {
        return isSoundPhoto;
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
     * Gets the output
     * 
     * @return the output
     */
    public @Nullable String getOutput() {
        return output;
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
     * Gets the play list info
     * 
     * @return the play list info
     */
    public @Nullable PlaylistInfo @Nullable [] getPlaylistInfo() {
        return playlistInfo;
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
     * Gets the play speed
     * 
     * @return the play speed
     */
    public @Nullable Speed getPlaySpeed() {
        return playSpeed;
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
     * Gets the position
     * 
     * @return the position
     */
    public @Nullable Position getPosition() {
        return position;
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
     * Gets the program service type
     * 
     * @return the program service type
     */
    public @Nullable String getProgramServiceType() {
        return programServiceType;
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
     * Gets the recording information
     * 
     * @return the recording information
     */
    public @Nullable RecordingInfo getRecordingInfo() {
        return recordingInfo;
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
     * Gets the size (in MB)
     * 
     * @return the size (in MB)
     */
    public @Nullable Integer getSizeMB() {
        return sizeMB;
    }

    /**
     * Gets the source
     * 
     * @return the source
     */
    public @Nullable String getSource() {
        return source;
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
     * Gets the start date time
     * 
     * @return the start date time
     */
    public @Nullable String getStartDateTime() {
        return startDateTime;
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
     * Gets the storage uri
     * 
     * @return the storage uri
     */
    public @Nullable String getStorageUri() {
        return storageUri;
    }

    /**
     * Gets the subtitle info
     * 
     * @return the subtitle info
     */
    public @Nullable SubtitleInfo @Nullable [] getSubtitleInfo() {
        return subtitleInfo;
    }

    /**
     * Gets the content sync priority
     * 
     * @return the content sync priority
     */
    public @Nullable String getSyncContentPriority() {
        return syncContentPriority;
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
     * Gets the total count
     * 
     * @return the total count
     */
    public @Nullable Integer getTotalCount() {
        return totalCount;
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
     * @return the content URI
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
     * Gets the video info
     * 
     * @return the video info
     */
    public @Nullable VideoInfo @Nullable [] getVideoInfo() {
        return videoInfo;
    }

    /**
     * Gets the content visibility
     * 
     * @return the content visibility
     */
    public @Nullable Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "ContentListResult_1_5 [albumName=" + albumName + ", applicationName=" + applicationName + ", artist="
                + artist + ", audioInfo=" + Arrays.toString(audioInfo) + ", bivlInfo=" + bivlInfo + ", broadcastFreq="
                + broadcastFreq + ", broadcastGenreInfo=" + Arrays.toString(broadcastGenreInfo) + ", channelName="
                + channelName + ", chapterCount=" + chapterCount + ", chapterIndex=" + chapterIndex + ", clipCount="
                + clipCount + ", content=" + content + ", contentKind=" + contentKind + ", contentType=" + contentType
                + ", createdTime=" + createdTime + ", dabInfo=" + dabInfo + ", dataInfo=" + dataInfo + ", description="
                + description + ", directRemoteNum=" + directRemoteNum + ", dispNum=" + dispNum + ", dubbingInfo="
                + dubbingInfo + ", duration=" + duration + ", eventId=" + eventId + ", fileNo=" + fileNo
                + ", fileSizeByte=" + fileSizeByte + ", folderNo=" + folderNo + ", genre=" + genre
                + ", globalPlaybackCount=" + globalPlaybackCount + ", groupInfo=" + Arrays.toString(groupInfo)
                + ", hasResume=" + hasResume + ", index=" + index + ", is3D=" + is3D + ", is4K=" + is4K
                + ", isAlreadyPlayed=" + isAlreadyPlayed + ", isAutoDelete=" + isAutoDelete + ", isBrowsable="
                + isBrowsable + ", isNew=" + isNew + ", isPlayable=" + isPlayable + ", isPlaylist=" + isPlaylist
                + ", isProtected=" + isProtected + ", isSoundPhoto=" + isSoundPhoto + ", mediaType=" + mediaType
                + ", originalDispNum=" + originalDispNum + ", output=" + output + ", parentalInfo="
                + Arrays.toString(parentalInfo) + ", parentIndex=" + parentIndex + ", parentUri=" + parentUri
                + ", playlistInfo=" + Arrays.toString(playlistInfo) + ", playlistName=" + playlistName + ", playSpeed="
                + playSpeed + ", podcastName=" + podcastName + ", position=" + position + ", productID=" + productID
                + ", programMediaType=" + programMediaType + ", programNum=" + programNum + ", programServiceType="
                + programServiceType + ", programTitle=" + programTitle + ", recordingInfo=" + recordingInfo
                + ", remotePlayType=" + remotePlayType + ", repeatType=" + repeatType + ", service=" + service
                + ", sizeMB=" + sizeMB + ", source=" + source + ", sourceLabel=" + sourceLabel + ", startDateTime="
                + startDateTime + ", stateInfo=" + stateInfo + ", storageUri=" + storageUri + ", subtitleInfo="
                + Arrays.toString(subtitleInfo) + ", syncContentPriority=" + syncContentPriority + ", title=" + title
                + ", totalCount=" + totalCount + ", tripletStr=" + tripletStr + ", uri=" + uri + ", userContentFlag="
                + userContentFlag + ", videoInfo=" + Arrays.toString(videoInfo) + ", visibility=" + visibility + "]";
    }
}
