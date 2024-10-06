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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-Base-Device Information class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoBaseDeviceData {
    @SerializedName("device_id")
    @Expose(serialize = false, deserialize = true)
    private String deviceId = "";

    @SerializedName("fw_ver")
    @Expose(serialize = false, deserialize = true)
    private String fwVer = "";

    @SerializedName("hw_ver")
    @Expose(serialize = false, deserialize = true)
    private String hwVer = "";

    @Expose(serialize = false, deserialize = true)
    private String mac = "";

    @Expose(serialize = false, deserialize = true)
    private String model = "";

    @Expose(serialize = false, deserialize = true)
    private String nickname = "";

    @Expose(serialize = false, deserialize = true)
    private String region = "";

    @Expose(serialize = false, deserialize = true)
    private String type = "";

    @Expose(serialize = false, deserialize = true)
    private String lang = "";

    @SerializedName("hw_id")
    @Expose(serialize = false, deserialize = true)
    private String hwId = "";

    @SerializedName("fw_id")
    @Expose(serialize = false, deserialize = true)
    private String fwId = "";

    @SerializedName("oem_id")
    @Expose(serialize = false, deserialize = true)
    private String oemId = "";

    @Expose(serialize = false, deserialize = true)
    private String ip = "";

    @SerializedName(value = "overheated", alternate = "overheatStatus")
    @Expose(serialize = false, deserialize = true)
    private boolean overheated = false;

    @Expose(serialize = false, deserialize = true)
    private int rssi = 0;

    @SerializedName("signal_level")
    @Expose(serialize = false, deserialize = true)
    private int signalLevel = 0;

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getDeviceId() {
        return deviceId;
    }

    public String getFirmwareVersion() {
        return fwVer;
    }

    public String getFirmwareId() {
        return fwId;
    }

    public String getHardwareVersion() {
        return hwVer;
    }

    public String getHardwareId() {
        return hwId;
    }

    public boolean isOverheated() {
        return overheated;
    }

    public String getLanguage() {
        return lang;
    }

    public String getMAC() {
        return formatMac(mac, MAC_DIVISION_CHAR);
    }

    public String getModel() {
        return model.replace(" Series", "");
    }

    public String getNickname() {
        return nickname;
    }

    public String getOEM() {
        return oemId;
    }

    public String getRegion() {
        return region;
    }

    public String getRepresentationProperty() {
        return getMAC();
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public int getRSSI() {
        return rssi;
    }

    public String getType() {
        return type;
    }

    public String getIpAddress() {
        return ip;
    }
}
