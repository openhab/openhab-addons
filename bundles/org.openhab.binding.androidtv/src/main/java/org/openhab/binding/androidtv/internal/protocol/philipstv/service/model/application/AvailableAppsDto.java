/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AvailableAppsDto} class defines the Data Transfer Object
 * for the Philips TV API /applications endpoint for retrieving all installed apps.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class AvailableAppsDto {

    @JsonProperty("version")
    private int version;

    @JsonProperty("applications")
    private @Nullable List<ApplicationsDto> applications;

    public AvailableAppsDto() {
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setApplications(List<ApplicationsDto> applications) {
        this.applications = applications;
    }

    public @Nullable List<ApplicationsDto> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "AvailableAppsDto{" + "version = '" + version + '\'' + ",applications = '" + applications + '\'' + "}";
    }
}
