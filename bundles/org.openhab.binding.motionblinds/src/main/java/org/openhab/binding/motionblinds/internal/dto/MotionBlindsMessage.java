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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link MotionBlindsMessage} is any message received from a motor: an acknowledge of a request
 * ({@code GetDeviceListAck}, {@code ReadDeviceAck}, {@code WriteDeviceAck}) or a multicast push
 * ({@code Report}, {@code Heartbeat}).
 *
 * <p>
 * {@code data} is an array of {@link DeviceListEntry} for {@code GetDeviceListAck} and a {@link DeviceStatus}
 * object for all other message types.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class MotionBlindsMessage {
    public @Nullable String msgType;
    public @Nullable String mac;
    public @Nullable String deviceType;
    public @Nullable String token;
    public @Nullable String msgID;
    public @Nullable String actionResult;
    @SerializedName("ProtocolVersion")
    public @Nullable String protocolVersion;
    public @Nullable String fwVersion;
    public @Nullable JsonElement data;
}
