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
package org.openhab.binding.emby.internal.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyPlayStateModel} holds data about the current play state
 * and provides safe getters that guard against missing playState or nowPlayingItem.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyPlayStateModel {

    @Nullable
    @com.google.gson.annotations.SerializedName("PlayState")
    private EmbyPlayState playState;

    @com.google.gson.annotations.SerializedName("RemoteEndPoint")
    private String remoteEndPoint = "";

    @com.google.gson.annotations.SerializedName("Id")
    private String id = "";

    @com.google.gson.annotations.SerializedName("UserId")
    private String userId = "";

    @com.google.gson.annotations.SerializedName("UserName")
    private String userName = "";

    @com.google.gson.annotations.SerializedName("Client")
    private String client = "";

    @com.google.gson.annotations.SerializedName("DeviceName")
    private String deviceName = "";

    @com.google.gson.annotations.SerializedName("DeviceId")
    private String deviceId = "";

    @com.google.gson.annotations.SerializedName("SupportsRemoteControl")
    private Boolean supportsRemoteControl = false;

    @Nullable
    @com.google.gson.annotations.SerializedName("NowPlayingItem")
    private EmbyNowPlayingItem nowPlayingItem;

    private final Logger logger = LoggerFactory.getLogger(EmbyPlayStateModel.class);

    /** May be null if nothing is playing. */
    public @Nullable EmbyPlayState getPlayStates() {
        return playState;
    }

    public Boolean getEmbyPlayStatePausedState() {
        final EmbyPlayState state = this.playState;
        return (state != null) ? state.getPaused() : Boolean.FALSE;
    }

    public Boolean getEmbyMuteSate() {
        final EmbyPlayState state = this.playState;
        return (state != null) ? state.getIsMuted() : Boolean.FALSE;
    }

    public String getNowPlayingName() {
        final EmbyNowPlayingItem item = this.nowPlayingItem;
        return (item != null) ? item.getName() : "";
    }

    public BigDecimal getNowPlayingTime() {
        final EmbyPlayState state = this.playState;
        return (state != null) ? state.getPositionTicks() : BigDecimal.ZERO;
    }

    public BigDecimal getNowPlayingTotalTime() {
        final EmbyNowPlayingItem item = this.nowPlayingItem;
        return (item != null) ? item.getRunTimeTicks() : BigDecimal.ZERO;
    }

    public String getNowPlayingMediaType() {
        final EmbyNowPlayingItem item = this.nowPlayingItem;
        return (item != null) ? item.getNowPlayingType() : "";
    }

    public Boolean compareDeviceId(String compareId) {
        // deviceId is never null; equals() handles null compareId safely
        return getDeviceId().equals(compareId);
    }

    /**
     * Returns the fraction (0–1) of playback completed, rounded to 2 decimal places.
     */
    public BigDecimal getPercentPlayed() {
        final EmbyPlayState state = this.playState;
        final EmbyNowPlayingItem item = this.nowPlayingItem;

        BigDecimal positionTicks = (state != null) ? state.getPositionTicks() : BigDecimal.ZERO;
        BigDecimal runTimeTicks = (item != null) ? item.getRunTimeTicks() : BigDecimal.ZERO;

        logger.debug("The play state position is {}", positionTicks);

        if (BigDecimal.ZERO.equals(runTimeTicks)) {
            return BigDecimal.ZERO;
        }
        return positionTicks.divide(runTimeTicks, 2, RoundingMode.HALF_UP);
    }

    public @Nullable EmbyNowPlayingItem getNowPlayingItem() {
        return nowPlayingItem;
    }

    /** @return the IP address of the user playing the media */
    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    public String getId() {
        return id;
    }

    /** @return the Emby ID of the user */
    public String getuserId() {
        return userId;
    }

    public String userName() {
        return userName;
    }

    public String getClient() {
        return client;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceId() {
        EmbyDeviceEncoder encoder = new EmbyDeviceEncoder();
        return encoder.encodeDeviceID(deviceId);
    }

    public Boolean getSupportsRemoteControl() {
        return supportsRemoteControl;
    }

    public URI getPrimaryImageURL(String embyHost, int embyPort, String embyType, String maxWidth, String maxHeight)
            throws URISyntaxException {
        logger.debug("Received an image URL request for: {} , port: {}, type: {}, max: {}/{} , percentPlayed: {}",
                embyHost, embyPort, embyType, maxWidth, maxHeight, getPercentPlayed());

        final EmbyNowPlayingItem item = this.nowPlayingItem;
        if (item == null) {
            // no media playing → special URI
            return new URI("http", null, "NotPlaying", 8096, null, null, null);
        }

        // build path based on media type
        String imagePath = item.getNowPlayingType().equalsIgnoreCase("Episode")
                ? "/emby/items/" + item.getSeasonId() + "/Images/" + embyType
                : "/emby/items/" + item.getId() + "/Images/" + embyType;

        // build query string
        StringBuilder query = new StringBuilder();

        query.append("MaxWidth=").append(maxWidth).append("&");

        query.append("MaxHeight=").append(maxHeight).append("&");

        return new URI("http", null, embyHost, embyPort, imagePath, query.toString(), null);
    }
}
