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
package org.openhab.binding.electroluxappliance.internal.dto;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApplianceDTO} class defines the DTO for the Electrolux Appliances.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ApplianceDTO {
    private String applianceId = "";
    private String applianceName = "";
    private String applianceType = "";
    private String created = "";

    private ApplianceInfoDTO applianceInfo = new ApplianceInfoDTO();
    private ApplianceStateDTO applianceState = new ApplianceStateDTO();
    private Instant applianceStateTs = Instant.now();

    public void setApplianceInfo(ApplianceInfoDTO applianceInfo) {
        this.applianceInfo = applianceInfo;
    }

    public void setApplianceState(ApplianceStateDTO applianceState) {
        this.applianceState = applianceState;
    }

    public void setApplianceState(final ApplianceStateDTO applianceState, final Instant timeRetrieved) {
        this.applianceState = applianceState;
        this.applianceStateTs = timeRetrieved;
    }

    public ApplianceInfoDTO getApplianceInfo() {
        return applianceInfo;
    }

    public ApplianceStateDTO getApplianceState() {
        return applianceState;
    }

    public Instant getApplianceStateTimestamp() {
        return applianceStateTs;
    }

    // Getters for each field
    public String getApplianceId() {
        return applianceId;
    }

    public String getApplianceName() {
        return applianceName;
    }

    public String getApplianceType() {
        return applianceType;
    }

    public String getCreated() {
        return created;
    }

    // Optional toString method for easier debugging and logging
    @Override
    public String toString() {
        return "Appliance{" + "applianceId='" + applianceId + '\'' + ", applianceName='" + applianceName + '\''
                + ", applianceType='" + applianceType + '\'' + ", created='" + created + '\'' + '}';
    }
}
