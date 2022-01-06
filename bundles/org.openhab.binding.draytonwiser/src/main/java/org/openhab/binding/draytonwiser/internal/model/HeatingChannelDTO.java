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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class HeatingChannelDTO {

    @SerializedName("id")
    private Integer id;
    private String name;
    private List<Integer> roomIds;
    private Integer percentageDemand;
    private String demandOnOffOutput;
    private String heatingRelayState;
    private Boolean isSmartValvePreventingDemand;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(final List<Integer> roomIds) {
        this.roomIds = roomIds;
    }

    public Integer getPercentageDemand() {
        return percentageDemand;
    }

    public String getDemandOnOffOutput() {
        return demandOnOffOutput;
    }

    public String getHeatingRelayState() {
        return heatingRelayState;
    }

    public Boolean getIsSmartValvePreventingDemand() {
        return isSmartValvePreventingDemand;
    }
}
