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
        if (inverters == null) {
            inverters = new HashMap<String, PowerFlowRealtimeInverter>();
        }
        return inverters;
    }

    public void setInverters(HashMap<String, PowerFlowRealtimeInverter> inverters) {
        this.inverters = inverters;
    }

    public PowerFlowRealtimeSite getSite() {
        if (site == null) {
            site = new PowerFlowRealtimeSite();
        }
        return site;
    }

    public void setSite(PowerFlowRealtimeSite site) {
        this.site = site;
    }

}
