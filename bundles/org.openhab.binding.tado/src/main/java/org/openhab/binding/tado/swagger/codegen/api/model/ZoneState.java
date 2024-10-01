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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class ZoneState {
    @SerializedName("tadoMode")
    private TadoMode tadoMode = null;

    @SerializedName("preparation")
    private Preparation preparation = null;

    @SerializedName("geolocationOverride")
    private Boolean geolocationOverride = null;

    @SerializedName("overlay")
    private Overlay overlay = null;

    @SerializedName("setting")
    private GenericZoneSetting setting = null;

    @SerializedName("openWindow")
    private OpenWindow openWindow = null;

    @SerializedName("openWindowDetected")
    private Boolean openWindowDetected = null;

    @SerializedName("link")
    private Link link = null;

    @SerializedName("activityDataPoints")
    private ActivityDataPoints activityDataPoints = null;

    @SerializedName("sensorDataPoints")
    private SensorDataPoints sensorDataPoints = null;

    public ZoneState tadoMode(TadoMode tadoMode) {
        this.tadoMode = tadoMode;
        return this;
    }

    public TadoMode getTadoMode() {
        return tadoMode;
    }

    public void setTadoMode(TadoMode tadoMode) {
        this.tadoMode = tadoMode;
    }

    public ZoneState preparation(Preparation preparation) {
        this.preparation = preparation;
        return this;
    }

    public Preparation getPreparation() {
        return preparation;
    }

    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    public ZoneState geolocationOverride(Boolean geolocationOverride) {
        this.geolocationOverride = geolocationOverride;
        return this;
    }

    public Boolean isGeolocationOverride() {
        return geolocationOverride;
    }

    public void setGeolocationOverride(Boolean geolocationOverride) {
        this.geolocationOverride = geolocationOverride;
    }

    public ZoneState overlay(Overlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public ZoneState setting(GenericZoneSetting setting) {
        this.setting = setting;
        return this;
    }

    public GenericZoneSetting getSetting() {
        return setting;
    }

    public void setSetting(GenericZoneSetting setting) {
        this.setting = setting;
    }

    public ZoneState openWindow(OpenWindow openWindow) {
        this.openWindow = openWindow;
        return this;
    }

    public OpenWindow getOpenWindow() {
        return openWindow;
    }

    public void setOpenWindow(OpenWindow openWindow) {
        this.openWindow = openWindow;
    }

    public ZoneState openWindowDetected(Boolean openWindowDetected) {
        this.openWindowDetected = openWindowDetected;
        return this;
    }

    public Boolean isOpenWindowDetected() {
        return openWindowDetected;
    }

    public void setOpenWindowDetected(Boolean openWindowDetected) {
        this.openWindowDetected = openWindowDetected;
    }

    public ZoneState link(Link link) {
        this.link = link;
        return this;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public ZoneState activityDataPoints(ActivityDataPoints activityDataPoints) {
        this.activityDataPoints = activityDataPoints;
        return this;
    }

    public ActivityDataPoints getActivityDataPoints() {
        return activityDataPoints;
    }

    public void setActivityDataPoints(ActivityDataPoints activityDataPoints) {
        this.activityDataPoints = activityDataPoints;
    }

    public ZoneState sensorDataPoints(SensorDataPoints sensorDataPoints) {
        this.sensorDataPoints = sensorDataPoints;
        return this;
    }

    public SensorDataPoints getSensorDataPoints() {
        return sensorDataPoints;
    }

    public void setSensorDataPoints(SensorDataPoints sensorDataPoints) {
        this.sensorDataPoints = sensorDataPoints;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZoneState zoneState = (ZoneState) o;
        return Objects.equals(this.tadoMode, zoneState.tadoMode)
                && Objects.equals(this.preparation, zoneState.preparation)
                && Objects.equals(this.geolocationOverride, zoneState.geolocationOverride)
                && Objects.equals(this.overlay, zoneState.overlay) && Objects.equals(this.setting, zoneState.setting)
                && Objects.equals(this.openWindow, zoneState.openWindow)
                && Objects.equals(this.openWindowDetected, zoneState.openWindowDetected)
                && Objects.equals(this.link, zoneState.link)
                && Objects.equals(this.activityDataPoints, zoneState.activityDataPoints)
                && Objects.equals(this.sensorDataPoints, zoneState.sensorDataPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tadoMode, preparation, geolocationOverride, overlay, setting, openWindow,
                openWindowDetected, link, activityDataPoints, sensorDataPoints);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ZoneState {\n");

        sb.append("    tadoMode: ").append(toIndentedString(tadoMode)).append("\n");
        sb.append("    preparation: ").append(toIndentedString(preparation)).append("\n");
        sb.append("    geolocationOverride: ").append(toIndentedString(geolocationOverride)).append("\n");
        sb.append("    overlay: ").append(toIndentedString(overlay)).append("\n");
        sb.append("    setting: ").append(toIndentedString(setting)).append("\n");
        sb.append("    openWindow: ").append(toIndentedString(openWindow)).append("\n");
        sb.append("    openWindowDetected: ").append(toIndentedString(openWindowDetected)).append("\n");
        sb.append("    link: ").append(toIndentedString(link)).append("\n");
        sb.append("    activityDataPoints: ").append(toIndentedString(activityDataPoints)).append("\n");
        sb.append("    sensorDataPoints: ").append(toIndentedString(sensorDataPoints)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
