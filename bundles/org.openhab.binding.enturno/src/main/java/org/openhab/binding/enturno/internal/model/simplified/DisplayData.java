/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enturno.internal.model.simplified;

import java.util.List;

/**
 * {@link DisplayData} is a Plain Old Java Objects class to wrap only needed data after processing API call results.
 *
 * @author Michal Kloc - Initial contribution
 */
public class DisplayData {
    private String stopPlaceId;

    private String stopName;

    private String transportMode;

    private String lineCode;

    private String frontText;

    private List<String> departures;

    private List<String> estimatedFlags;

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }

    public List<String> getDepartures() {
        return departures;
    }

    public void setDepartures(List<String> departures) {
        this.departures = departures;
    }

    public String getStopPlaceId() {
        return stopPlaceId;
    }

    public void setStopPlaceId(String stopPlaceId) {
        this.stopPlaceId = stopPlaceId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public List<String> getEstimatedFlags() {
        return estimatedFlags;
    }

    public void setEstimatedFlags(List<String> estimatedFlags) {
        this.estimatedFlags = estimatedFlags;
    }

    @Override
    public String toString() {
        return "DisplayData{" +
                "stopPlaceId='" + stopPlaceId + '\'' +
                ", stopName='" + stopName + '\'' +
                ", transportMode='" + transportMode + '\'' +
                ", lineCode='" + lineCode + '\'' +
                ", frontText='" + frontText + '\'' +
                ", departures=" + departures +
                ", estimatedFlags=" + estimatedFlags +
                '}';
    }
}
