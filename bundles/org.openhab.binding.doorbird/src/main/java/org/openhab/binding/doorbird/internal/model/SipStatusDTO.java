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
package org.openhab.binding.doorbird.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SipStatusDTO} models the JSON response returned by the Doorbird in response
 * to calling the sip.cgi status API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SipStatusDTO {
    /**
     * Top level container of information about the Doorbird status
     */
    @SerializedName("BHA")
    public SipStatusBha bha;

    public class SipStatusBha {
        /**
         * Return code from the Doorbird
         */
        @SerializedName("RETURNCODE")
        public String returnCode;

        /**
         * Contains information about the Doorbird SIP status
         */
        @SerializedName("SIP")
        public SipStatusArray[] sipStatusArray;

        public class SipStatusArray {
            @SerializedName("ENABLE")
            public String enable;

            @SerializedName("PRIORITIZE_APP")
            public String prioritizeApp;

            @SerializedName("REGISTER_URL")
            public String registerUrl;

            @SerializedName("REGISTER_USER")
            public String registerUser;

            @SerializedName("REGISTER_PASSWORD")
            public String registerPassword;

            @SerializedName("AUTOCALL_MOTIONSENSOR_URL")
            public String autocallMotionSensorUrl;

            @SerializedName("AUTOCALL_DOORBELL_URL")
            public String autocallDoorbellUrl;

            /**
             * Speaker volume
             */
            @SerializedName("SPK_VOLUME")
            public String speakerVolume;

            /**
             * Microphone volume
             */
            @SerializedName("MIC_VOLUME")
            public String microphoneVolume;

            @SerializedName("DTMF")
            public String dtmf;

            @SerializedName("relais:1")
            public String relais1;

            @SerializedName("relais:2")
            public String relais2;

            @SerializedName("LIGHT_PASSCODE")
            public String lightPasscode;

            @SerializedName("INCOMING_CALL_ENABLE")
            public String incomingCallEnable;

            @SerializedName("INCOMING_CALL_USER")
            public String incomingCallUser;

            @SerializedName("ANC")
            public String autoNoiseCancellation;

            /**
             * SIP call last error code
             */
            @SerializedName("LASTERRORCODE")
            public String lastErrorCode;

            /**
             * SIP call last error code
             */
            @SerializedName("LASTERRORTEXT")
            public String lastErrorText;

            /**
             * Maximum SIP ring time
             */
            @SerializedName("RING_TIME_LIMIT")
            public String ringTimeLimit;

            /**
             * Maximum SIP call time
             */
            @SerializedName("CALL_TIME_LIMIT")
            public String callTimeLimit;
        }
    }
}
