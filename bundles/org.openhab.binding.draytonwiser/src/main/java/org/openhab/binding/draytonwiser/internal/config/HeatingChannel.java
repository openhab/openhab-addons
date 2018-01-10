/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class HeatingChannel {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("RoomIds")
    @Expose
    private List<Integer> roomIds = null;
    @SerializedName("PercentageDemand")
    @Expose
    private Integer percentageDemand;
    @SerializedName("DemandOnOffOutput")
    @Expose
    private String demandOnOffOutput;
    @SerializedName("HeatingRelayState")
    @Expose
    private String heatingRelayState;
    @SerializedName("IsSmartValvePreventingDemand")
    @Expose
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
