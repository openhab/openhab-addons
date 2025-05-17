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

package org.openhab.binding.ecoflow.internal.api.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class DeviceListResponseEntry {
    @SerializedName("sn")
    public final String serialNumber;
    public final String deviceName;
    public final int online;
    public final String productName;

    DeviceListResponseEntry(String serialNumber, String deviceName, int online, String productName) {
        this.serialNumber = serialNumber;
        this.deviceName = deviceName;
        this.online = online;
        this.productName = productName;
    }

    public boolean isOnline() {
        return online != 0;
    }
}
