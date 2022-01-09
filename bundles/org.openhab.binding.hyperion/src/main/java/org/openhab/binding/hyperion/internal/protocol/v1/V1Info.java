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
package org.openhab.binding.hyperion.internal.protocol.v1;

import java.util.List;

import org.openhab.binding.hyperion.internal.protocol.ng.Adjustment;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link V1Info} is a POJO for receiving information from a V1 Hyperion Server
 *
 * @author Daniel Walters - Initial contribution
 */
public class V1Info {

    @SerializedName("activeEffects")
    private List<ActiveEffect> activeEffects = null;

    @SerializedName("activeLedColor")
    private List<ActiveLedColor> activeLedColor = null;

    @SerializedName("adjustment")
    private List<Adjustment> adjustment = null;

    @SerializedName("correction")
    private List<Correction> correction = null;

    @SerializedName("effects")
    private List<Effect> effects = null;

    @SerializedName("hostname")
    private String hostname;

    @SerializedName("hyperion_build")
    private List<HyperionBuild> hyperionBuild = null;

    @SerializedName("priorities")
    private List<Priority> priorities = null;

    @SerializedName("temperature")
    private List<Temperature> temperature = null;

    @SerializedName("transform")
    private List<Transform> transform = null;

    public List<ActiveEffect> getActiveEffects() {
        return activeEffects;
    }

    public void setActiveEffects(List<ActiveEffect> activeEffects) {
        this.activeEffects = activeEffects;
    }

    public List<ActiveLedColor> getActiveLedColor() {
        return activeLedColor;
    }

    public void setActiveLedColor(List<ActiveLedColor> activeLedColor) {
        this.activeLedColor = activeLedColor;
    }

    public List<Adjustment> getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(List<Adjustment> adjustment) {
        this.adjustment = adjustment;
    }

    public List<Correction> getCorrection() {
        return correction;
    }

    public void setCorrection(List<Correction> correction) {
        this.correction = correction;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<HyperionBuild> getHyperionBuild() {
        return hyperionBuild;
    }

    public void setHyperionBuild(List<HyperionBuild> hyperionBuild) {
        this.hyperionBuild = hyperionBuild;
    }

    public List<Priority> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public List<Temperature> getTemperature() {
        return temperature;
    }

    public void setTemperature(List<Temperature> temperature) {
        this.temperature = temperature;
    }

    public List<Transform> getTransform() {
        return transform;
    }

    public void setTransform(List<Transform> transform) {
        this.transform = transform;
    }
}
