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
package org.openhab.binding.semsportal.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for mapping the portal data response to the {@link StatusRequest} and the {@link StationListRequest}
 *
 * @author Iwan Bron - Initial contribution
 *
 */
public class Station {
    @SerializedName("powerstation_id")
    private String stationId;
    @SerializedName("stationname")
    private String name;
    @SerializedName("sn")
    private String serialNumber;
    private String type;
    private Double capacity;
    private int status;
    @SerializedName("out_pac")
    private Double currentPower;
    @SerializedName("eday")
    private Double dayTotal;
    @SerializedName("emonth")
    private Double monthTotal;
    @SerializedName("etotal")
    private Double overallTotal;
    @SerializedName("d")
    private InverterDetails details;

    public String getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getType() {
        return type;
    }

    public Double getCapacity() {
        return capacity;
    }

    public int getStatus() {
        return status;
    }

    public Double getCurrentPower() {
        return currentPower;
    }

    public Double getDayTotal() {
        return dayTotal;
    }

    public Double getMonthTotal() {
        return monthTotal;
    }

    public Double getOverallTotal() {
        return overallTotal;
    }

    public InverterDetails getDetails() {
        return details;
    }
}
