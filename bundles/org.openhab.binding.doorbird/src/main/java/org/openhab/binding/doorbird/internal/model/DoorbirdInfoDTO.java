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
package org.openhab.binding.doorbird.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DoorbirdInfoDTO} models the JSON response returned by the Doorbird in response
 * to calling the info.cgi API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class DoorbirdInfoDTO {
    /**
     * Top level container of information about the Doorbird configuration
     */
    @SerializedName("BHA")
    public DoorbirdInfoBha bha;

    public class DoorbirdInfoBha {
        /**
         * Return code from the Doorbird
         */
        @SerializedName("RETURNCODE")
        public String returnCode;

        /**
         * Contains information about the Doorbird configuration
         */
        @SerializedName("VERSION")
        public DoorbirdInfoArray[] doorbirdInfoArray;

        public class DoorbirdInfoArray {
            /**
             * Doorbird's firmware version
             */
            @SerializedName("FIRMWARE")
            public String firmwareVersion;

            /**
             * Doorbird's build number
             */
            @SerializedName("BUILD_NUMBER")
            public String buildNumber;

            /**
             * MAC address of Doorbird's wired interface
             */
            @SerializedName("PRIMARY_MAC_ADDR")
            public String primaryMacAddress;

            /**
             * MAC address of Doorbird's wifi interface
             */
            @SerializedName("WIFI_MAC_ADDR")
            public String wifiMacAddress;

            /**
             * Array of relays supported by this Doorbird
             */
            @SerializedName("RELAYS")
            public String[] relays;

            /**
             * Doorbird's model name
             */
            @SerializedName("DEVICE-TYPE")
            public String deviceType;
        }
    }
}
