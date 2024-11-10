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
package org.openhab.binding.haassohnpelletstove.internal;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HaasSohnpelletstoveJsonDataDTO} is the Java class used to map the JSON
 * response to an Oven request.
 *
 * @author Christian Feininger - Initial contribution
 */
public class HaasSohnpelletstoveJsonDataDTO {
    Metadata meta = new Metadata();
    boolean prg;
    boolean wprg;
    String mode = "";
    @SerializedName("sp_temp")
    String spTemp = "";
    @SerializedName("is_temp")
    String isTemp = "";
    @SerializedName("ht_char")
    String htChar = "";
    @SerializedName("weekprogram")
    private Wprogram[] weekprogram;
    @SerializedName("error")
    private Err[] error;
    @SerializedName("eco_mode")
    boolean ecoMode;
    boolean pgi;
    String ignitions = "";
    @SerializedName("on_time")
    String onTime = "";
    String consumption = "";
    @SerializedName("maintenance_in")
    String maintenanceIn = "";
    @SerializedName("cleaning_in")
    String cleaningIn = "";

    /***
     * Get the nonce
     *
     * @return nonce
     */
    public String getNonce() {
        return this.meta.getNonce();
    }

    /**
     * Returns the is Temperature of the Oven
     *
     * @return
     */
    public String getisTemp() {
        return isTemp;
    }

    public boolean getEcoMode() {
        return ecoMode;
    }

    public String getIgnitions() {
        return ignitions;
    }

    public String getOnTime() {
        return onTime;
    }

    public String getConsumption() {
        return consumption;
    }

    public String getMaintenanceIn() {
        return maintenanceIn;
    }

    public String getCleaningIn() {
        return cleaningIn;
    }

    /***
     * JSON response
     *
     * @return JSON response as object
     */
    public HaasSohnpelletstoveJsonDataDTO getResponse() {
        return this;
    }

    public class Metadata {
        @SerializedName("sw_version")
        String swVersion = "";
        @SerializedName("hw_version")
        String hwVersion = "";
        @SerializedName("bootl_version")
        String bootlVersion = "";
        @SerializedName("wifi_sw_version")
        String wifiSWVersion = "";
        @SerializedName("wifi_bootl_version")
        String wifiBootlVersion = "";
        String sn = "";
        String typ = "";
        String language = "";
        String nonce = "";
        @SerializedName("eco_editable")
        String ecoEditable = "";
        String ts = "";
        String ean = "";
        boolean rau;
        @SerializedName("wlan_features")
        private String[] wlanFeatures;

        public String getNonce() {
            return nonce;
        }
    }

    public class Err {
        String time = "";
        String nr = "";
    }

    public class Wprogram {
        String day = "";
        String begin = "";
        String end = "";
        String temp = "";
    }

    public String getMode() {
        return mode;
    }

    public String getspTemp() {
        return spTemp;
    }

    public boolean getPrg() {
        return prg;
    }
}
