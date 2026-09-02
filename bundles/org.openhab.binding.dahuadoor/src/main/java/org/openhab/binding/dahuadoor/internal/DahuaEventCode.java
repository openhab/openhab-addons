/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dahuadoor.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum representing all known Dahua VTO event codes.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public enum DahuaEventCode {
    ACCESS_CONTROL("AccessControl"),
    ACCESS_SNAP("AccessSnap"),
    ADD_CARD("AddCard"),
    ALARM_LOCAL("AlarmLocal"),
    BACK_KEY_LIGHT("BackKeyLight"),
    CALL_NO_ANSWERED("CallNoAnswered"),
    CALL_SNAP("CallSnap"),
    DGS_ERROR_REPORT("DGSErrorReport"),
    DOOR_CARD("DoorCard"),
    DOOR_CONTROL("DoorControl"),
    DOOR_NOT_CLOSED("DoorNotClosed"),
    DOOR_STATUS("DoorStatus"),
    FINGER_PRINT_CHECK("FingerPrintCheck"),
    HANGUP("Hangup"),
    HANGUP_PHONE("HangupPhone"),
    HUNGUP_PHONE("HungupPhone"),
    IGNORE_INVITE("IgnoreInvite"),
    INVITE("Invite"),
    KEEP_LIGHT_ON("KeepLightOn"),
    NETWORK_CHANGE("NetworkChange"),
    NEW_FILE("NewFile"),
    NTP_ADJUST_TIME("NTPAdjustTime"),
    PASSIVE_HANGUP("PassiveHangup"),
    PROFILE_ALARM_TRANSMIT("ProfileAlarmTransmit"),
    REBOOT("Reboot"),
    REQUEST_CALL_STATE("RequestCallState"),
    RTSP_SESSION_DISCONNECT("RtspSessionDisconnect"),
    SECURITY_IM_EXPORT("SecurityImExport"),
    SEND_CARD("SendCard"),
    SIP_REGISTER_RESULT("SIPRegisterResult"),
    TIME_CHANGE("TimeChange"),
    UPDATE_FILE("UpdateFile"),
    UPGRADE("Upgrade"),
    VIDEO_BLIND("VideoBlind"),
    VIDEO_MOTION("VideoMotion"),
    UNKNOWN("");

    private final String code;

    DahuaEventCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Convert a string to the corresponding enum value.
     *
     * @param code the event code string
     * @return the matching enum value, or UNKNOWN if not found
     */
    public static DahuaEventCode fromString(@Nullable String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN;
        }
        for (DahuaEventCode eventCode : values()) {
            if (eventCode.code.equals(code)) {
                return eventCode;
            }
        }
        return UNKNOWN;
    }
}
