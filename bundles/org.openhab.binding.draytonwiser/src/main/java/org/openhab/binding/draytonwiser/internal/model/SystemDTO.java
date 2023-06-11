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
package org.openhab.binding.draytonwiser.internal.model;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class SystemDTO {

    private String pairingStatus;
    private String overrideType;
    private Integer overrideSetpoint;
    private Integer timeZoneOffset;
    private Boolean automaticDaylightSaving;
    private Integer version;
    private Boolean fotaEnabled;
    private Boolean valveProtectionEnabled;
    private Boolean ecoModeEnabled;
    private Boolean comfortModeEnabled;
    private BoilerSettingsDTO boilerSettings;
    private Long unixTime;
    private String cloudConnectionStatus;
    private String zigbeeModuleVersion;
    private String zigbeeEui;
    private LocalDateAndTimeDTO localDateAndTime;
    private String heatingButtonOverrideState;
    private String hotWaterButtonOverrideState;

    public String getPairingStatus() {
        return pairingStatus;
    }

    public void setPairingStatus(final String pairingStatus) {
        this.pairingStatus = pairingStatus;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public Integer getOverrideSetpoint() {
        return overrideSetpoint;
    }

    public Integer getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(final Integer timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public Boolean getAutomaticDaylightSaving() {
        return automaticDaylightSaving;
    }

    public void setAutomaticDaylightSaving(final Boolean automaticDaylightSaving) {
        this.automaticDaylightSaving = automaticDaylightSaving;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public Boolean getFotaEnabled() {
        return fotaEnabled;
    }

    public void setFotaEnabled(final Boolean fotaEnabled) {
        this.fotaEnabled = fotaEnabled;
    }

    public Boolean getValveProtectionEnabled() {
        return valveProtectionEnabled;
    }

    public void setValveProtectionEnabled(final Boolean valveProtectionEnabled) {
        this.valveProtectionEnabled = valveProtectionEnabled;
    }

    public Boolean getEcoModeEnabled() {
        return ecoModeEnabled;
    }

    public void setEcoModeEnabled(final Boolean ecoModeEnabled) {
        this.ecoModeEnabled = ecoModeEnabled;
    }

    public BoilerSettingsDTO getBoilerSettings() {
        return boilerSettings;
    }

    public void setBoilerSettings(final BoilerSettingsDTO boilerSettings) {
        this.boilerSettings = boilerSettings;
    }

    public Long getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(final Long unixTime) {
        this.unixTime = unixTime;
    }

    public String getCloudConnectionStatus() {
        return cloudConnectionStatus;
    }

    public void setCloudConnectionStatus(final String cloudConnectionStatus) {
        this.cloudConnectionStatus = cloudConnectionStatus;
    }

    public String getZigbeeModuleVersion() {
        return zigbeeModuleVersion;
    }

    public void setZigbeeModuleVersion(final String zigbeeModuleVersion) {
        this.zigbeeModuleVersion = zigbeeModuleVersion;
    }

    public String getZigbeeEui() {
        return zigbeeEui;
    }

    public void setZigbeeEui(final String zigbeeEui) {
        this.zigbeeEui = zigbeeEui;
    }

    public LocalDateAndTimeDTO getLocalDateAndTime() {
        return localDateAndTime;
    }

    public void setLocalDateAndTime(final LocalDateAndTimeDTO localDateAndTime) {
        this.localDateAndTime = localDateAndTime;
    }

    public String getHeatingButtonOverrideState() {
        return heatingButtonOverrideState;
    }

    public void setHeatingButtonOverrideState(final String heatingButtonOverrideState) {
        this.heatingButtonOverrideState = heatingButtonOverrideState;
    }

    public String getHotWaterButtonOverrideState() {
        return hotWaterButtonOverrideState;
    }

    public void setHotWaterButtonOverrideState(final String hotWaterButtonOverrideState) {
        this.hotWaterButtonOverrideState = hotWaterButtonOverrideState;
    }

    public Boolean getComfortModeEnabled() {
        return comfortModeEnabled;
    }

    public void setComfortModeEnabled(final Boolean comfortModeEnabled) {
        this.comfortModeEnabled = comfortModeEnabled;
    }
}
