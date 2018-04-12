/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.api;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeBodyData} is responsible for storing
 * the "data" node of the JSON response
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class PowerFlowRealtimeBodyData {

    @SerializedName("Site")
    private PowerFlowRealtimeSite site;

    @SerializedName("Inverters")
    private HashMap<String, PowerFlowRealtimeInverter> inverters;

    public HashMap<String, PowerFlowRealtimeInverter> getInverters() {
        return inverters;
    }

    public void setInverters(HashMap<String, PowerFlowRealtimeInverter> inverters) {
        this.inverters = inverters;
    }

    public PowerFlowRealtimeSite getSite() {
        return site;
    }

    public void setSite(PowerFlowRealtimeSite site) {
        this.site = site;
    }

}
