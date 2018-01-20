/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for reading tp-Link Smart Plug energy monitoring.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Realtime extends ErrorResponse {

    /* 
     * for devices like:
     *   {"system":{"get_sysinfo":{"sw_ver":"1.0.10 Build 161012 Rel.165510","hw_ver":"2.0","type":"IOT.SMARTPLUGSWITCH","model":"HS110(AU)"
     * The get_realtime json response looks like:
     *   "emeter":{"get_realtime":{"voltage_mv":239425,"current_ma":1234,"power_mw":123456,"total_wh":12345,"err_code":0}}}
     *
     * use gson's alternate {@link com.google.gson.annotations.SerializedName} so both are valid
     */

    @SerializedName(value="current", alternate={"current_ma"})
    private double current;
    @SerializedName(value="power", alternate={"power_mw"})
    private double power;
    @SerializedName(value="total", alternate={"total_wh"})
    private double total;
    @SerializedName(value="voltage", alternate={"voltage_mv"})
    private double voltage;

    public double getCurrent() {
        return current;
    }

    public double getPower() {
        return power;
    }

    public double getTotal() {
        return total;
    }

    public double getVoltage() {
        return voltage;
    }

    @Override
    public String toString() {
        return "current:" + current + ", power:" + power + ", total:" + total + ", voltage:" + voltage
                + super.toString();
    }

}
