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

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class SmartPlugDTO {

    @SerializedName("id")
    private Integer id;
    private String name;
    private String manualState;
    private String mode;
    private String awayAction;
    private String outputState;
    private String controlSource;
    private String scheduledState;
    private String targetState;
    private Integer debounceCount;
    private String overrideState;
    private Integer currentSummationDelivered;
    private Integer instantaneousDemand;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManualState() {
        return manualState;
    }

    public String getAwayAction() {
        return awayAction;
    }

    public String getOutputState() {
        return outputState;
    }

    public String getControlSource() {
        return controlSource;
    }

    public String getScheduledState() {
        return scheduledState;
    }

    public String getTargetState() {
        return targetState;
    }

    public Integer getDebounceCount() {
        return debounceCount;
    }

    public String getOverrideState() {
        return overrideState;
    }

    public String getMode() {
        return mode;
    }

    public Integer getCurrentSummationDelivered() {
        return currentSummationDelivered;
    }

    public Integer getInstantaneousDemand() {
        return instantaneousDemand;
    }
}
