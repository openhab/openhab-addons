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
package org.openhab.binding.doorbird.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.doorbird.internal.model.SipStatusJson;
import org.openhab.binding.doorbird.internal.model.SipStatusJson.SipStatusBha;
import org.openhab.binding.doorbird.internal.model.SipStatusJson.SipStatusBha.SipStatusArray;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link SipStatus} holds SIP status information retrieved from the Doorbell.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SipStatus {
    private String returnCode = "";
    private String enable = "";
    private String prioritizeApp = "";
    private String registerUrl = "";
    private String registerUser = "";
    private String registerPassword = "";
    private String autocallMotionSensorUrl = "";
    private String autocallDoorbellUrl = "";
    private String speakerVolume = "";
    private String microphoneVolume = "";
    private String dtmf = "";
    private String relais1 = "";
    private String relais2 = "";
    private String lightPasscode = "";
    private String incomingCallEnable = "";
    private String incomingCallUser = "";
    private String autoNoiseCancellation = "";
    private String lastErrorCode = "";
    private String lastErrorText = "";
    private String ringTimeLimit = "";
    private String callTimeLimit = "";

    public SipStatus(String sipStatusJson) throws JsonSyntaxException {
        SipStatusJson sipStatus = DoorbirdAPI.fromJson(sipStatusJson, SipStatusJson.class);
        if (sipStatus != null) {
            SipStatusBha bha = sipStatus.bha;
            returnCode = bha.returnCode;
            for (SipStatusArray sip : bha.sipStatusArray) {
                enable = sip.enable != null ? sip.enable : "";
                prioritizeApp = sip.prioritizeApp != null ? sip.prioritizeApp : "";
                registerUrl = sip.registerUrl != null ? sip.registerUrl : "";
                registerUser = sip.registerUser != null ? sip.registerUser : "";
                registerPassword = sip.registerPassword != null ? sip.registerPassword : "";
                autocallMotionSensorUrl = sip.autocallMotionSensorUrl != null ? sip.autocallMotionSensorUrl : "";
                autocallDoorbellUrl = sip.autocallDoorbellUrl != null ? sip.autocallDoorbellUrl : "";
                speakerVolume = sip.speakerVolume != null ? sip.speakerVolume : "";
                microphoneVolume = sip.microphoneVolume != null ? sip.microphoneVolume : "";
                dtmf = sip.dtmf != null ? sip.dtmf : "";
                relais1 = sip.relais1 != null ? sip.relais1 : "";
                relais2 = sip.relais2 != null ? sip.relais2 : "";
                lightPasscode = sip.lightPasscode != null ? sip.lightPasscode : "";
                incomingCallEnable = sip.incomingCallEnable != null ? sip.incomingCallEnable : "";
                incomingCallUser = sip.incomingCallUser != null ? sip.incomingCallUser : "";
                autoNoiseCancellation = sip.autoNoiseCancellation != null ? sip.autoNoiseCancellation : "";
                lastErrorCode = sip.lastErrorCode != null ? sip.lastErrorCode : "";
                lastErrorText = sip.lastErrorText != null ? sip.lastErrorText : "";
                ringTimeLimit = sip.ringTimeLimit != null ? sip.ringTimeLimit : "";
                callTimeLimit = sip.callTimeLimit != null ? sip.callTimeLimit : "";
            }
        }
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getEnable() {
        return enable;
    }

    public String getPrioritizeApp() {
        return prioritizeApp;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public String getRegisterUser() {
        return registerUser;
    }

    public String getRegisterPassword() {
        return registerPassword;
    }

    public String getAutocallMotionSensorUrl() {
        return autocallMotionSensorUrl;
    }

    public String getAutocallDoorbellUrl() {
        return autocallDoorbellUrl;
    }

    public String getSpeakerVolume() {
        return speakerVolume;
    }

    public String getMicrophoneVolume() {
        return microphoneVolume;
    }

    public String getDtmf() {
        return dtmf;
    }

    public String getRelais1() {
        return relais1;
    }

    public String getRelais2() {
        return relais2;
    }

    public String getLightPasscode() {
        return lightPasscode;
    }

    public String getIncomingCallEnable() {
        return incomingCallEnable;
    }

    public String getIncomingCallUser() {
        return incomingCallUser;
    }

    public String getAutoNoiseCancellation() {
        return autoNoiseCancellation;
    }

    public String getLastErrorCode() {
        return lastErrorCode;
    }

    public String getLastErrorText() {
        return lastErrorText;
    }

    public String getRingTimeLimit() {
        return ringTimeLimit;
    }

    public String getCallTimeLimit() {
        return callTimeLimit;
    }
}
