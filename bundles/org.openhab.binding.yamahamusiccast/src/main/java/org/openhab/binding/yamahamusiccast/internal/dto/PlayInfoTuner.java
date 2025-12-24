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
package org.openhab.binding.yamahamusiccast.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the PlayInfo request requested from the Yamaha model/device via the API.
 *
 * @author Markus Kastner - Initial contribution
 */

public class PlayInfoTuner {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("band")
    private String band;

    @SerializedName("fm")
    private fmObject fm;

    @SerializedName("dab")
    private dabObject dab;

    public String getResponseCode() {
        if (responseCode == null) {
            responseCode = "";
        }
        return responseCode;
    }

    public String getBand() {
        if (band == null) {
            band = "";
        }
        return band;
    }

    public fmObject getFM() {
        return fm;
    }

    public dabObject getDAB() {
        return dab;
    }

    public class fmObject {
        @SerializedName("preset")
        private int preset = 0;
        @SerializedName("freq")
        private int freq = 0;

        public int getPreset() {
            return preset;
        }

        public int getFreq() {
            return freq;
        }
    }

    public class dabObject {
        @SerializedName("preset")
        private int preset = 0;
        @SerializedName("service_label")
        private String serviceLabel;

        public int getPreset() {
            return preset;
        }

        public String getServiceLabel() {
            if (serviceLabel == null) {
                serviceLabel = "";
            }
            return serviceLabel;
        }
    }
}
