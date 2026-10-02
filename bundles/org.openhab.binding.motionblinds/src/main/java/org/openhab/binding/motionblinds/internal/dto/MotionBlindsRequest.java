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
package org.openhab.binding.motionblinds.internal.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MotionBlindsRequest} is a request sent to a motor. Fields left {@code null} are not serialized.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsRequest {
    private static final DateTimeFormatter MSG_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public final String msgType;
    public @Nullable String mac;
    public @Nullable String deviceType;
    @SerializedName("AccessToken")
    public @Nullable String accessToken;
    public final String msgID;
    public @Nullable Map<String, Object> data;

    public MotionBlindsRequest(String msgType) {
        this.msgType = msgType;
        this.msgID = LocalDateTime.now(ZoneOffset.UTC).format(MSG_ID_FORMAT);
    }

    public MotionBlindsRequest(String msgType, String mac, String deviceType, String accessToken,
            @Nullable Map<String, Object> data) {
        this(msgType);
        this.mac = mac;
        this.deviceType = deviceType;
        this.accessToken = accessToken;
        this.data = data;
    }
}
