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
package org.openhab.binding.meross.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Device} class is a record holding device's components
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public record Device(@SerializedName(value = "deviceType") String deviceType,
        @SerializedName(value = "devIconId") String devIconId, @SerializedName(value = "onlineStatus") int onlineStatus,
        @SerializedName(value = "devName") String devName,
        @SerializedName(value = "fmwareVersion") String firmwareVersion, @SerializedName(value = "uuid") String uuid,
        @SerializedName(value = "userDevIcon") String userDeviceIcon, @SerializedName(value = "bindTime") long bindTime,
        @SerializedName(value = "iconType") int iconType, @SerializedName(value = "domain") String domain,
        @SerializedName(value = "reservedDomain") String reservedDomain,
        @SerializedName(value = "subType") String subType, @SerializedName(value = "region") String region,
        @SerializedName(value = "hdwareVersion") String hardwareVersion) {
}
