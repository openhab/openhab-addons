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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the detailed information for a playlist entry in a LinkPlay PlayQueue
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PlayListInfo {
    private String source = "";
    private String searchUrl = "";
    private String picUrl = "";
    private String loginUsername = "";
    private int autoGenerate = 0;
    private int stationLimit = 0;
    private int markSearch = 0;
    private int quality = 0;
    private String requestQuality = "";
    private int updateTime = 0;
    private int lastPlayIndex = 0;
    private int alarmPlayIndex = 0;
    private int realIndex = 0;
    private int userId = 0;
    private String contentType = "";
    private int stationBackup = 0;
    private int trackNumber = 0;
    private int switchPageMode = 0;
    private int pressType = 0;
    private int volume = 0;
    private int tempQueue = 0;
    private int currentPage = 0;
    private int totalPages = 0;
    private int searching = 0;
    private int fadeEnable = 0;
    private int fadeInMS = 0;
    private int fadeOutMS = 0;
    private int modified = 0;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public int getAutoGenerate() {
        return autoGenerate;
    }

    public void setAutoGenerate(int autoGenerate) {
        this.autoGenerate = autoGenerate;
    }

    public int getStationLimit() {
        return stationLimit;
    }

    public void setStationLimit(int stationLimit) {
        this.stationLimit = stationLimit;
    }

    public int getMarkSearch() {
        return markSearch;
    }

    public void setMarkSearch(int markSearch) {
        this.markSearch = markSearch;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getRequestQuality() {
        return requestQuality;
    }

    public void setRequestQuality(String requestQuality) {
        this.requestQuality = requestQuality;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getLastPlayIndex() {
        return lastPlayIndex;
    }

    public void setLastPlayIndex(int lastPlayIndex) {
        this.lastPlayIndex = lastPlayIndex;
    }

    public int getAlarmPlayIndex() {
        return alarmPlayIndex;
    }

    public void setAlarmPlayIndex(int alarmPlayIndex) {
        this.alarmPlayIndex = alarmPlayIndex;
    }

    public int getRealIndex() {
        return realIndex;
    }

    public void setRealIndex(int realIndex) {
        this.realIndex = realIndex;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getStationBackup() {
        return stationBackup;
    }

    public void setStationBackup(int stationBackup) {
        this.stationBackup = stationBackup;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getSwitchPageMode() {
        return switchPageMode;
    }

    public void setSwitchPageMode(int switchPageMode) {
        this.switchPageMode = switchPageMode;
    }

    public int getPressType() {
        return pressType;
    }

    public void setPressType(int pressType) {
        this.pressType = pressType;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getTempQueue() {
        return tempQueue;
    }

    public void setTempQueue(int tempQueue) {
        this.tempQueue = tempQueue;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getSearching() {
        return searching;
    }

    public void setSearching(int searching) {
        this.searching = searching;
    }

    public int getFadeEnable() {
        return fadeEnable;
    }

    public void setFadeEnable(int fadeEnable) {
        this.fadeEnable = fadeEnable;
    }

    public int getFadeInMS() {
        return fadeInMS;
    }

    public void setFadeInMS(int fadeInMS) {
        this.fadeInMS = fadeInMS;
    }

    public int getFadeOutMS() {
        return fadeOutMS;
    }

    public void setFadeOutMS(int fadeOutMS) {
        this.fadeOutMS = fadeOutMS;
    }

    public int getModified() {
        return modified;
    }

    public void setModified(int modified) {
        this.modified = modified;
    }
}
