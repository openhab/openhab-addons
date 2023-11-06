/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.model.SipStatusDTO;
import org.openhab.binding.doorbird.internal.model.SipStatusDTO.SipStatusBha;
import org.openhab.binding.doorbird.internal.model.SipStatusDTO.SipStatusBha.SipStatusArray;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link SipStatus} holds SIP status information retrieved from the Doorbell
 * that is used in the binding handler.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SipStatus {
    private @Nullable String returnCode;
    private @Nullable String speakerVolume;
    private @Nullable String microphoneVolume;
    private @Nullable String lastErrorCode;
    private @Nullable String lastErrorText;
    private @Nullable String ringTimeLimit;
    private @Nullable String callTimeLimit;

    @SuppressWarnings("null")
    public SipStatus(String sipStatusJson) throws JsonSyntaxException {
        SipStatusDTO sipStatus = DoorbirdAPI.fromJson(sipStatusJson, SipStatusDTO.class);
        if (sipStatus != null) {
            SipStatusBha bha = sipStatus.bha;
            returnCode = bha.returnCode;
            // SIP array should have only one entry
            if (bha.sipStatusArray.length == 1) {
                SipStatusArray sip = bha.sipStatusArray[0];
                speakerVolume = sip.speakerVolume;
                microphoneVolume = sip.microphoneVolume;
                lastErrorCode = sip.lastErrorCode;
                lastErrorText = sip.lastErrorText;
                ringTimeLimit = sip.ringTimeLimit;
                callTimeLimit = sip.callTimeLimit;
            }
        }
    }

    public String getReturnCode() {
        String value = returnCode;
        return value != null ? value : "";
    }

    public String getSpeakerVolume() {
        String value = speakerVolume;
        return value != null ? value : "";
    }

    public String getMicrophoneVolume() {
        String value = microphoneVolume;
        return value != null ? value : "";
    }

    public String getLastErrorCode() {
        String value = lastErrorCode;
        return value != null ? value : "";
    }

    public String getLastErrorText() {
        String value = lastErrorText;
        return value != null ? value : "";
    }

    public String getRingTimeLimit() {
        String value = ringTimeLimit;
        return value != null ? value : "";
    }

    public String getCallTimeLimit() {
        String value = callTimeLimit;
        return value != null ? value : "";
    }
}
