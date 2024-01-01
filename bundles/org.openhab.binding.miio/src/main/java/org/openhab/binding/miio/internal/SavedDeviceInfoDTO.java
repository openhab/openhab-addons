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
package org.openhab.binding.miio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Storing last id to to be able to reconnect better after binding restart
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class SavedDeviceInfoDTO {

    @SerializedName("lastId")
    @Expose
    private int lastId = 0;
    @SerializedName("deviceId")
    @Expose
    private String deviceId;

    public SavedDeviceInfoDTO(int lastId, String deviceId) {
        super();
        this.lastId = lastId;
        this.deviceId = deviceId;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
