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
package org.openhab.binding.mffan.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ShadowBufferDto} shadow buffer data transport object.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class ShadowBufferDto {
    @Expose
    private String clientId;

    @Expose
    private Integer cloudPort;

    @Expose
    private Boolean lightOn;

    @Expose
    private Boolean fanOn;

    @Expose
    private Integer lightBrightness;

    @Expose
    private Integer fanSpeed;

    @Expose
    private FanDirection fanDirection;

    @Expose
    private Boolean wind;

    @Expose
    private Integer windSpeed;

    @Expose
    private Boolean rfPairModeActive;

    @Expose
    private Boolean resetRfPairList;

    @Expose
    private Boolean factoryReset;

    @Expose
    private Boolean awayModeEnabled;

    @Expose
    private Integer fanTimer;

    @Expose
    private Integer lightTimer;

    @Expose
    private Boolean decommission;

    @Expose
    private String schedule;

    @Expose
    private Boolean adaptiveLearning;

    @Expose
    private String userData;

    @Expose
    private String timezone;

    @Expose
    @SerializedName("FrCodes")
    private String frCodes;

    @Expose
    private Boolean cdebug;

    @Expose
    private Boolean feedbackToneMute;

    public enum FanDirection {
        forward,
        reverse
    }

    public ShadowBufferDto() {
        this.clientId = "";
        this.cloudPort = 0;
        this.lightOn = false;
        this.fanOn = false;
        this.lightBrightness = 0;
        this.fanSpeed = 0;
        this.fanDirection = FanDirection.forward;
        this.wind = false;
        this.windSpeed = 0;
        this.rfPairModeActive = false;
        this.resetRfPairList = false;
        this.factoryReset = false;
        this.awayModeEnabled = false;
        this.fanTimer = 0;
        this.lightTimer = 0;
        this.decommission = false;
        this.schedule = "";
        this.adaptiveLearning = false;
        this.userData = "";
        this.timezone = "";
        this.frCodes = "";
        this.cdebug = false;
        this.feedbackToneMute = false;
    }

    public String getClientId() {
        return this.clientId;
    }

    public Integer getCloudPort() {
        return this.cloudPort;
    }

    public Boolean getLightOn() {
        return this.lightOn;
    }

    public Boolean getFanOn() {
        return this.fanOn;
    }

    public OnOffType getFanOnAsOnOffType() {
        return OnOffType.from(this.fanOn);
    }

    public Integer getLightBrightness() {
        return this.lightBrightness;
    }

    public Integer getFanSpeed() {
        return this.fanSpeed;
    }

    public FanDirection getFanDirection() {
        return this.fanDirection;
    }

    public Boolean getWind() {
        return this.wind;
    }

    public Integer getWindSpeed() {
        return this.windSpeed;
    }

    public Boolean getRfPairModeActive() {
        return this.rfPairModeActive;
    }

    public Boolean getResetRfPairList() {
        return this.resetRfPairList;
    }

    public Boolean getFactoryReset() {
        return this.factoryReset;
    }

    public Boolean getAwayModeEnabled() {
        return this.awayModeEnabled;
    }

    public Integer getFanTimer() {
        return this.fanTimer;
    }

    public Integer getLightTimer() {
        return this.lightTimer;
    }

    public Boolean getDecommission() {
        return this.decommission;
    }

    public String getSchedule() {
        return this.schedule;
    }

    public Boolean getAdaptiveLearning() {
        return this.adaptiveLearning;
    }

    public String getUserData() {
        return this.userData;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public String getFrCodes() {
        return this.frCodes;
    }

    public Boolean getCdebug() {
        return this.cdebug;
    }

    public Boolean getFeedbackToneMute() {
        return this.feedbackToneMute;
    }
}
