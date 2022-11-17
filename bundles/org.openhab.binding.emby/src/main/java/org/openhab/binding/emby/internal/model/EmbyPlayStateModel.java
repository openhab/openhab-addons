/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EmbyPlayStateModelGson} is responsible to hold
 * data that models pin information which can be sent to a Konnected Module
 *
 * @author Zachary Christiansen - Initial contribution
 *
 */
public class EmbyPlayStateModel {

    @SerializedName("PlayState")
    private EmbyPlayState playState;
    @SerializedName("RemoteEndPoint")
    private String remoteEndPoint;
    @SerializedName("Id")
    private String id;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("UserName")
    private String userName;
    @SerializedName("Client")
    private String client;
    @SerializedName("DeviceName")
    private String deviceName;
    @SerializedName("DeviceId")
    private String deviceId;
    @SerializedName("SupportsRemoteControl")
    private Boolean supportsRemoteControl;
    @SerializedName("NowPlayingItem")
    private EmbyNowPlayingItem nowPlayingItem;
    private final Logger logger = LoggerFactory.getLogger(EmbyPlayStateModel.class);

    public EmbyPlayState getPlayStates() {
        return playState;
    }

    public Boolean getEmbyPlayStatePausedState() {
        return this.playState.getPaused();
    }

    public Boolean getEmbyMuteSate() {
        return this.playState.getIsMuted();
    }

    public String getNowPlayingName() {
        return this.nowPlayingItem.getName();
    }

    public BigDecimal getNowPlayingTime() {
        return this.playState.getPositionTicks();
    }

    public BigDecimal getNowPlayingTotalTime() {
        return this.nowPlayingItem.getRunTimeTicks();
    }

    public String getNowPlayingMediaType() {
        return this.getNowPlayingItem().getNowPlayingType();
    }

    public Boolean compareDeviceId(String compareId) {

        @Nullable
        Boolean returnValue = getDeviceId().equals(compareId);

        if (!(returnValue == null)) {
            return returnValue;
        } else {
            return false;
        }
    }

    /**
     * @return the BigInteger Value of the PlayState PositionTicks over the Total Run Time Position Ticks which is the
     *         mathmatical representation of the percentage played
     */
    public BigDecimal getPercentPlayed() {
        BigDecimal positionTicks = playState.getPositionTicks();
        BigDecimal runTimeTicks = nowPlayingItem.getRunTimeTicks();
        logger.debug("The play state position is {}", positionTicks.toString());
        if (runTimeTicks.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        } else {
            return (positionTicks.divide(runTimeTicks, 2, RoundingMode.HALF_UP));
        }
    }

    public EmbyNowPlayingItem getNowPlayingItem() {
        return nowPlayingItem;
    }

    /**
     * @return the ip address of the user playing the meida
     */
    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the emby id of the user playing the media, this can be used to obtain the images in conjunction with the
     *         media id
     */
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
        EmbyDeviceEncoder encode = new EmbyDeviceEncoder();
        return encode.encodeDeviceID(deviceId);
    }

    public Boolean getSupportsRemoteControl() {
        return supportsRemoteControl;
    }

    public URI getPrimaryImageURL(String embyHost, int embyPort, String embyType, String maxWidth, String maxHeight) throws URISyntaxException {
        String imagePath = "";
        logger.debug(
                "Received an image URL request for: {} , embyPort: {}, embyType: {}, maxWidth: {}, maxHeight: {}, percentPlayed: {}",
                embyHost, embyPort, embyType, maxWidth, maxHeight);
        if (this.nowPlayingItem == null) {
            // If there is no nowPlayingItem this means the device has stopped so we will return
            // a URI that will let us check for that, if any of this returns null
            URI imageURI = new URI("http", null, "NotPlaying", 8096, null, null, null);
            return imageURI;
        } else {
            if (this.nowPlayingItem.getNowPlayingType().equalsIgnoreCase("Episode")) {
                // if its a TV Series use that instead of ID for image
                imagePath = "/emby/items/" + this.nowPlayingItem.getSeasonId() + "/Images/" + embyType;
            } else {
                imagePath = "/emby/items/" + this.nowPlayingItem.getId() + "/Images/" + embyType;

            }

            String query = "";

            if (!(maxWidth == null)) {
                query = "MaxWidth=" + maxWidth + "&" + query;
            }

            if (!(maxHeight == null)) {
                query = "MaxHeight=" + maxHeight + "&" + query;
            }

            // http://192.168.20.152:8096/emby/Items/%7BItemid%7D/Images/Primary?PercentPlayed=47
            URI imageURI = new URI("http", null, embyHost, embyPort, imagePath, query, null);
            return imageURI;
        }
    }
}
