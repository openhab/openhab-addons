/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class ContentListResult.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ContentListResult {

    /** The uri. */
    private final String uri;

    /** The title. */
    private final String title;

    /** The index. */
    private final int index;

    /** The disp num. */
    private final String dispNum;

    /** The original disp num. */
    private final String originalDispNum;

    /** The triplet str. */
    private final String tripletStr;

    /** The program num. */
    private final int programNum;

    /** The program media type. */
    private final String programMediaType;

    /** The direct remote num. */
    private final int directRemoteNum;

    /** The epg visibility. */
    private final String epgVisibility;

    /** The channel surfing visibility. */
    private final String channelSurfingVisibility;

    /** The visibility. */
    private final String visibility;

    /** The start date time. */
    private final String startDateTime;

    /** The channel name. */
    private final String channelName;

    /** The file size byte. */
    private final int fileSizeByte;

    /** The is protected. */
    private final boolean isProtected;

    /** The is already played. */
    private final boolean isAlreadyPlayed;

    /** The product ID. */
    private final String productID;

    /** The content type. */
    private final String contentType;

    /** The storage uri. */
    private final String storageUri;

    /** The video codec. */
    private final String videoCodec;

    /** The chapter count. */
    private final int chapterCount;

    /** The duration sec. */
    private final double durationSec;

    /** The audio codec. */
    private final String[] audioCodec;

    /** The audio frequency. */
    private final String[] audioFrequency;

    /** The audio channel. */
    private final String[] audioChannel;

    /** The subtitle language. */
    private final String[] subtitleLanguage;

    /** The subtitle title. */
    private final String[] subtitleTitle;

    /** The parental rating. */
    private final String[] parentalRating;

    /** The parental system. */
    private final String[] parentalSystem;

    /** The parental country. */
    private final String[] parentalCountry;

    /** The size MB. */
    private final int sizeMB;

    /** The created time. */
    private final String createdTime;

    /** The user content flag. */
    private final boolean userContentFlag;

    /** The field names. */
    private final Set<String> fieldNames;

    /**
     * Instantiates a new content list result.
     *
     * @param uri the uri
     * @param title the title
     * @param index the index
     * @param dispNum the disp num
     * @param originalDispNum the original disp num
     * @param tripletStr the triplet str
     * @param programNum the program num
     * @param programMediaType the program media type
     * @param directRemoteNum the direct remote num
     * @param epgVisibility the epg visibility
     * @param channelSurfingVisibility the channel surfing visibility
     * @param visibility the visibility
     * @param startDateTime the start date time
     * @param channelName the channel name
     * @param fileSizeByte the file size byte
     * @param isProtected the is protected
     * @param isAlreadyPlayed the is already played
     * @param productID the product ID
     * @param contentType the content type
     * @param storageUri the storage uri
     * @param videoCodec the video codec
     * @param chapterCount the chapter count
     * @param durationSec the duration sec
     * @param audioCodec the audio codec
     * @param audioFrequency the audio frequency
     * @param audioChannel the audio channel
     * @param subtitleLanguage the subtitle language
     * @param subtitleTitle the subtitle title
     * @param parentalRating the parental rating
     * @param parentalSystem the parental system
     * @param parentalCountry the parental country
     * @param sizeMB the size MB
     * @param createdTime the created time
     * @param userContentFlag the user content flag
     */
    public ContentListResult(String uri, String title, int index, String dispNum, String originalDispNum,
            String tripletStr, int programNum, String programMediaType, int directRemoteNum, String epgVisibility,
            String channelSurfingVisibility, String visibility, String startDateTime, String channelName,
            int fileSizeByte, boolean isProtected, boolean isAlreadyPlayed, String productID, String contentType,
            String storageUri, String videoCodec, int chapterCount, double durationSec, String[] audioCodec,
            String[] audioFrequency, String[] audioChannel, String[] subtitleLanguage, String[] subtitleTitle,
            String[] parentalRating, String[] parentalSystem, String[] parentalCountry, int sizeMB, String createdTime,
            boolean userContentFlag) {
        this.uri = uri;
        this.title = title;
        this.index = index;
        this.dispNum = dispNum;
        this.originalDispNum = originalDispNum;
        this.tripletStr = tripletStr;
        this.programNum = programNum;
        this.programMediaType = programMediaType;
        this.directRemoteNum = directRemoteNum;
        this.epgVisibility = epgVisibility;
        this.channelSurfingVisibility = channelSurfingVisibility;
        this.visibility = visibility;
        this.startDateTime = startDateTime;
        this.channelName = channelName;
        this.fileSizeByte = fileSizeByte;
        this.isProtected = isProtected;
        this.isAlreadyPlayed = isAlreadyPlayed;
        this.productID = productID;
        this.contentType = contentType;
        this.storageUri = storageUri;
        this.videoCodec = videoCodec;
        this.chapterCount = chapterCount;
        this.durationSec = durationSec;
        this.audioCodec = audioCodec;
        this.audioFrequency = audioFrequency;
        this.audioChannel = audioChannel;
        this.subtitleLanguage = subtitleLanguage;
        this.subtitleTitle = subtitleTitle;
        this.parentalRating = parentalRating;
        this.parentalSystem = parentalSystem;
        this.parentalCountry = parentalCountry;
        this.sizeMB = sizeMB;
        this.createdTime = createdTime;
        this.userContentFlag = userContentFlag;
        this.fieldNames = new HashSet<String>();
    }

    /**
     * Checks for.
     *
     * @param fieldName the field name
     * @return true, if successful
     */
    private boolean has(String fieldName) {
        return fieldNames.size() == 0 || fieldNames.contains(fieldName);
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Checks for uri.
     *
     * @return true, if successful
     */
    public boolean hasUri() {
        return has("uri");
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Checks for title.
     *
     * @return true, if successful
     */
    public boolean hasTitle() {
        return has("title");
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Checks for index.
     *
     * @return true, if successful
     */
    public boolean hasIndex() {
        return has("index");
    }

    /**
     * Gets the disp num.
     *
     * @return the disp num
     */
    public String getDispNum() {
        return dispNum;
    }

    /**
     * Checks for disp num.
     *
     * @return true, if successful
     */
    public boolean hasDispNum() {
        return has("dispNum");
    }

    /**
     * Gets the original disp num.
     *
     * @return the original disp num
     */
    public String getOriginalDispNum() {
        return originalDispNum;
    }

    /**
     * Checks for original disp num.
     *
     * @return true, if successful
     */
    public boolean hasOriginalDispNum() {
        return has("originalDispNum");
    }

    /**
     * Gets the triplet str.
     *
     * @return the triplet str
     */
    public String getTripletStr() {
        return tripletStr;
    }

    /**
     * Checks for triplet str.
     *
     * @return true, if successful
     */
    public boolean hasTripletStr() {
        return has("tripletStr");
    }

    /**
     * Gets the program num.
     *
     * @return the program num
     */
    public int getProgramNum() {
        return programNum;
    }

    /**
     * Checks for program num.
     *
     * @return true, if successful
     */
    public boolean hasProgramNum() {
        return has("programNum");
    }

    /**
     * Gets the program media type.
     *
     * @return the program media type
     */
    public String getProgramMediaType() {
        return programMediaType;
    }

    /**
     * Checks for program media type.
     *
     * @return true, if successful
     */
    public boolean hasProgramMediaType() {
        return has("programMediaType");
    }

    /**
     * Gets the direct remote num.
     *
     * @return the direct remote num
     */
    public int getDirectRemoteNum() {
        return directRemoteNum;
    }

    /**
     * Checks for direct remote num.
     *
     * @return true, if successful
     */
    public boolean hasDirectRemoteNum() {
        return has("directRemoteNum");
    }

    /**
     * Gets the epg visibility.
     *
     * @return the epg visibility
     */
    public String getEpgVisibility() {
        return epgVisibility;
    }

    /**
     * Checks for epg visibility.
     *
     * @return true, if successful
     */
    public boolean hasEpgVisibility() {
        return has("epgVisibility");
    }

    /**
     * Gets the channel surfing visibility.
     *
     * @return the channel surfing visibility
     */
    public String getChannelSurfingVisibility() {
        return channelSurfingVisibility;
    }

    /**
     * Checks for channel surfing visibility.
     *
     * @return true, if successful
     */
    public boolean hasChannelSurfingVisibility() {
        return has("channelSurfingVisibility");
    }

    /**
     * Gets the visibility.
     *
     * @return the visibility
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Checks for visibility.
     *
     * @return true, if successful
     */
    public boolean hasVisibility() {
        return has("visibility");
    }

    /**
     * Gets the start date time.
     *
     * @return the start date time
     */
    public String getStartDateTime() {
        return startDateTime;
    }

    /**
     * Checks for start date time.
     *
     * @return true, if successful
     */
    public boolean hasStartDateTime() {
        return has("startDateTime");
    }

    /**
     * Gets the channel name.
     *
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Checks for channel name.
     *
     * @return true, if successful
     */
    public boolean hasChannelName() {
        return has("channelName");
    }

    /**
     * Gets the file size byte.
     *
     * @return the file size byte
     */
    public int getFileSizeByte() {
        return fileSizeByte;
    }

    /**
     * Checks for file size byte.
     *
     * @return true, if successful
     */
    public boolean hasFileSizeByte() {
        return has("fileSizeByte");
    }

    /**
     * Checks if is protected.
     *
     * @return true, if is protected
     */
    public boolean isProtected() {
        return isProtected;
    }

    /**
     * Checks for is protected.
     *
     * @return true, if successful
     */
    public boolean hasIsProtected() {
        return has("isProtected");
    }

    /**
     * Checks if is already played.
     *
     * @return true, if is already played
     */
    public boolean isAlreadyPlayed() {
        return isAlreadyPlayed;
    }

    /**
     * Checks for is already played.
     *
     * @return true, if successful
     */
    public boolean hasIsAlreadyPlayed() {
        return has("isAlreadyPlayed");
    }

    /**
     * Gets the product ID.
     *
     * @return the product ID
     */
    public String getProductID() {
        return productID;
    }

    /**
     * Checks for product ID.
     *
     * @return true, if successful
     */
    public boolean hasProductID() {
        return has("productID");
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Checks for content type.
     *
     * @return true, if successful
     */
    public boolean hasContentType() {
        return has("contentType");
    }

    /**
     * Gets the storage uri.
     *
     * @return the storage uri
     */
    public String getStorageUri() {
        return storageUri;
    }

    /**
     * Checks for storage uri.
     *
     * @return true, if successful
     */
    public boolean hasStorageUri() {
        return has("storageUri");
    }

    /**
     * Gets the video codec.
     *
     * @return the video codec
     */
    public String getVideoCodec() {
        return videoCodec;
    }

    /**
     * Checks for video codec.
     *
     * @return true, if successful
     */
    public boolean hasVideoCodec() {
        return has("videoCodec");
    }

    /**
     * Gets the chapter count.
     *
     * @return the chapter count
     */
    public int getChapterCount() {
        return chapterCount;
    }

    /**
     * Checks for chapter count.
     *
     * @return true, if successful
     */
    public boolean hasChapterCount() {
        return has("chapterCount");
    }

    /**
     * Gets the duration sec.
     *
     * @return the duration sec
     */
    public double getDurationSec() {
        return durationSec;
    }

    /**
     * Checks for duration sec.
     *
     * @return true, if successful
     */
    public boolean hasDurationSec() {
        return has("durationSec");
    }

    /**
     * Gets the audio codec.
     *
     * @return the audio codec
     */
    public String[] getAudioCodec() {
        return audioCodec;
    }

    /**
     * Checks for audio codec.
     *
     * @return true, if successful
     */
    public boolean hasAudioCodec() {
        return has("audioCodec");
    }

    /**
     * Gets the audio frequency.
     *
     * @return the audio frequency
     */
    public String[] getAudioFrequency() {
        return audioFrequency;
    }

    /**
     * Checks for audio frequency.
     *
     * @return true, if successful
     */
    public boolean hasAudioFrequency() {
        return has("audioFrequency");
    }

    /**
     * Gets the audio channel.
     *
     * @return the audio channel
     */
    public String[] getAudioChannel() {
        return audioChannel;
    }

    /**
     * Checks for audio channel.
     *
     * @return true, if successful
     */
    public boolean hasAudioChannel() {
        return has("audioChannel");
    }

    /**
     * Gets the subtitle language.
     *
     * @return the subtitle language
     */
    public String[] getSubtitleLanguage() {
        return subtitleLanguage;
    }

    /**
     * Checks for subtitle language.
     *
     * @return true, if successful
     */
    public boolean hasSubtitleLanguage() {
        return has("subtitleLanguage");
    }

    /**
     * Gets the subtitle title.
     *
     * @return the subtitle title
     */
    public String[] getSubtitleTitle() {
        return subtitleTitle;
    }

    /**
     * Checks for subtitle title.
     *
     * @return true, if successful
     */
    public boolean hasSubtitleTitle() {
        return has("subtitleTitle");
    }

    /**
     * Gets the parental rating.
     *
     * @return the parental rating
     */
    public String[] getParentalRating() {
        return parentalRating;
    }

    /**
     * Checks for parental rating.
     *
     * @return true, if successful
     */
    public boolean hasParentalRating() {
        return has("parentalRating");
    }

    /**
     * Gets the parental system.
     *
     * @return the parental system
     */
    public String[] getParentalSystem() {
        return parentalSystem;
    }

    /**
     * Checks for parental system.
     *
     * @return true, if successful
     */
    public boolean hasParentalSystem() {
        return has("parentalSystem");
    }

    /**
     * Gets the parental country.
     *
     * @return the parental country
     */
    public String[] getParentalCountry() {
        return parentalCountry;
    }

    /**
     * Checks for parental country.
     *
     * @return true, if successful
     */
    public boolean hasParentalCountry() {
        return has("parentalCountry");
    }

    /**
     * Gets the size MB.
     *
     * @return the size MB
     */
    public int getSizeMB() {
        return sizeMB;
    }

    /**
     * Checks for size MB.
     *
     * @return true, if successful
     */
    public boolean hasSizeMB() {
        return has("sizeMB");
    }

    /**
     * Gets the created time.
     *
     * @return the created time
     */
    public String getCreatedTime() {
        return createdTime;
    }

    /**
     * Checks for created time.
     *
     * @return true, if successful
     */
    public boolean hasCreatedTime() {
        return has("createdTime");
    }

    /**
     * Checks if is user content flag.
     *
     * @return true, if is user content flag
     */
    public boolean isUserContentFlag() {
        return userContentFlag;
    }

    /**
     * Checks for user content flag.
     *
     * @return true, if successful
     */
    public boolean hasUserContentFlag() {
        return has("userContentFlag");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContentListResult [uri=" + uri + ", title=" + title + ", index=" + index + ", dispNum=" + dispNum
                + ", originalDispNum=" + originalDispNum + ", tripletStr=" + tripletStr + ", programNum=" + programNum
                + ", programMediaType=" + programMediaType + ", directRemoteNum=" + directRemoteNum + ", epgVisibility="
                + epgVisibility + ", channelSurfingVisibility=" + channelSurfingVisibility + ", visibility="
                + visibility + ", startDateTime=" + startDateTime + ", channelName=" + channelName + ", fileSizeByte="
                + fileSizeByte + ", isProtected=" + isProtected + ", isAlreadyPlayed=" + isAlreadyPlayed
                + ", productID=" + productID + ", contentType=" + contentType + ", storageUri=" + storageUri
                + ", videoCodec=" + videoCodec + ", chapterCount=" + chapterCount + ", durationSec=" + durationSec
                + ", audioCodec=" + Arrays.toString(audioCodec) + ", audioFrequency=" + Arrays.toString(audioFrequency)
                + ", audioChannel=" + Arrays.toString(audioChannel) + ", subtitleLanguage="
                + Arrays.toString(subtitleLanguage) + ", subtitleTitle=" + Arrays.toString(subtitleTitle)
                + ", parentalRating=" + Arrays.toString(parentalRating) + ", parentalSystem="
                + Arrays.toString(parentalSystem) + ", parentalCountry=" + Arrays.toString(parentalCountry)
                + ", sizeMB=" + sizeMB + ", createdTime=" + createdTime + ", userContentFlag=" + userContentFlag + "]";
    }

}
