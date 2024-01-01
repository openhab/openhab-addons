/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal.dto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlexSession} is the class used internally by the PlexPlayer things
 * to keep state of updates from the Bridge.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
public class PlexSession {
    private String title;
    private String thumb;
    private String art;
    private long viewOffset;
    private String type;
    private BigDecimal progress;
    private String machineIdentifier;
    private PlexPlayerState state;
    private String local;
    private long duration;
    private Date endTime;
    private String sessionKey = "";
    private Integer userId;
    private String userTitle = "";

    private final Logger logger = LoggerFactory.getLogger(PlexSession.class);

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getTitle() {
        return title;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public long getViewOffset() {
        return viewOffset;
    }

    public void setViewOffset(long viewOffset) {
        this.viewOffset = viewOffset;
        updateProgress();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public String getMachineIdentifier() {
        return machineIdentifier;
    }

    public void setMachineIdentifier(String machineIdentifier) {
        this.machineIdentifier = machineIdentifier;
    }

    public PlexPlayerState getState() {
        return state;
    }

    public void setState(PlexPlayerState state) {
        this.state = state;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    private void updateProgress() {
        try {
            if (this.duration > 0) {
                BigDecimal progress = new BigDecimal("100")
                        .divide(new BigDecimal(this.duration), new MathContext(100, RoundingMode.HALF_UP))
                        .multiply(new BigDecimal(this.viewOffset)).setScale(2, RoundingMode.HALF_UP);
                progress = BigDecimal.ZERO.max(progress);
                progress = new BigDecimal("100").min(progress);

                this.endTime = new Date(System.currentTimeMillis() + (this.duration - this.viewOffset));
                this.progress = progress;
            }
        } catch (Exception e) {
            logger.debug("An exception occurred while polling the updating Progress: '{}'", e.getMessage());
        }
    }
}
