/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayingContentInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class PlayingContentInfo {

    /** The uri. */
    private final String uri;

    /** The source. */
    private final String source;

    /** The title. */
    private final String title;

    /** The disp num. */
    private final String dispNum;

    /** The original disp num. */
    private final String originalDispNum;

    /** The triplet str. */
    private final String tripletStr;

    /** The program num. */
    private final int programNum;

    /** The program title. */
    private final String programTitle;

    /** The start date time. */
    private final String startDateTime;

    /** The duration sec. */
    private final int durationSec;

    /** The media type. */
    private final String mediaType;

    /** The play speed. */
    private final String playSpeed;

    /** The bivl service id. */
    private final String bivl_serviceId;

    /** The bivl asset id. */
    private final String bivl_assetId;

    /** The bivl provider. */
    private final String bivl_provider;

    /** The field names. */
    private final Set<String> fieldNames;

    /**
     * Instantiates a new playing content info.
     *
     * @param uri the uri
     * @param source the source
     * @param title the title
     * @param dispNum the disp num
     * @param originalDispNum the original disp num
     * @param tripletStr the triplet str
     * @param programNum the program num
     * @param programTitle the program title
     * @param startDateTime the start date time
     * @param durationSec the duration sec
     * @param mediaType the media type
     * @param playSpeed the play speed
     * @param bivl_serviceId the bivl service id
     * @param bivl_assetId the bivl asset id
     * @param bivl_provider the bivl provider
     */
    public PlayingContentInfo(String uri, String source, String title, String dispNum, String originalDispNum,
            String tripletStr, int programNum, String programTitle, String startDateTime, int durationSec,
            String mediaType, String playSpeed, String bivl_serviceId, String bivl_assetId, String bivl_provider) {
        super();
        this.uri = uri;
        this.source = source;
        this.title = title;
        this.dispNum = dispNum;
        this.originalDispNum = originalDispNum;
        this.tripletStr = tripletStr;
        this.programNum = programNum;
        this.programTitle = programTitle;
        this.startDateTime = startDateTime;
        this.durationSec = durationSec;
        this.mediaType = mediaType;
        this.playSpeed = playSpeed;
        this.bivl_serviceId = bivl_serviceId;
        this.bivl_assetId = bivl_assetId;
        this.bivl_provider = bivl_provider;
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
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Checks for source.
     *
     * @return true, if successful
     */
    public boolean hasSource() {
        return has("source");
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
     * Gets the program title.
     *
     * @return the program title
     */
    public String getProgramTitle() {
        return programTitle;
    }

    /**
     * Checks for program title.
     *
     * @return true, if successful
     */
    public boolean hasProgramTitle() {
        return has("programTitle");
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
     * Gets the duration sec.
     *
     * @return the duration sec
     */
    public int getDurationSec() {
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
     * Gets the media type.
     *
     * @return the media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Checks for media type.
     *
     * @return true, if successful
     */
    public boolean hasMediaType() {
        return has("mediaType");
    }

    /**
     * Gets the play speed.
     *
     * @return the play speed
     */
    public String getPlaySpeed() {
        return playSpeed;
    }

    /**
     * Checks for play speed.
     *
     * @return true, if successful
     */
    public boolean hasPlaySpeed() {
        return has("playSpeed");
    }

    /**
     * Gets the bivl service id.
     *
     * @return the bivl service id
     */
    public String getBivlServiceId() {
        return bivl_serviceId;
    }

    /**
     * Checks for bivl service id.
     *
     * @return true, if successful
     */
    public boolean hasBivlServiceId() {
        return has("bivl_serviceId");
    }

    /**
     * Gets the bivl asset id.
     *
     * @return the bivl asset id
     */
    public String getBivlAssetId() {
        return bivl_assetId;
    }

    /**
     * Checks for bivl asset id.
     *
     * @return true, if successful
     */
    public boolean hasBivlAssetId() {
        return has("bivl_assetId");
    }

    /**
     * Gets the bivl provider.
     *
     * @return the bivl provider
     */
    public String getBivlProvider() {
        return bivl_provider;
    }

    /**
     * Checks for bivl provider.
     *
     * @return true, if successful
     */
    public boolean hasBivlProvider() {
        return has("bivl_provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PlayingContentInfo [uri=" + uri + ", source=" + source + ", title=" + title + ", dispNum=" + dispNum
                + ", originalDispNum=" + originalDispNum + ", tripletStr=" + tripletStr + ", programNum=" + programNum
                + ", programTitle=" + programTitle + ", startDateTime=" + startDateTime + ", durationSec=" + durationSec
                + ", mediaType=" + mediaType + ", playSpeed=" + playSpeed + ", bivl_serviceId=" + bivl_serviceId
                + ", bivl_assetId=" + bivl_assetId + ", bivl_provider=" + bivl_provider + "]";
    }
}
