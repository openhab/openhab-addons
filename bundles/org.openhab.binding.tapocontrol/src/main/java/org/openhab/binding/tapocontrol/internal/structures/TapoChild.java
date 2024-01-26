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
package org.openhab.binding.tapocontrol.internal.structures;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.MAC_DIVISION_CHAR;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.formatMac;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tapo Child Device Information class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TapoChild {
    private String fwVer = "";
    private String hwVer = "";
    private String type = "";
    private String model = "";
    private String mac = "";
    private String category = "";
    private String deviceId = "";
    private boolean overheatStatus = false;
    private int bindCount = 0;
    private long onTime = 0;
    private int slotNumber = 0;
    private int position = 0;
    private String nickname = "";
    private boolean deviceOn = false;
    private String region = "";

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getFirmwareVersion() {
        return fwVer;
    }

    public String getHardwareVersion() {
        return hwVer;
    }

    public Boolean isOff() {
        return !deviceOn;
    }

    public Boolean isOn() {
        return deviceOn;
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

    public Number getOnTime() {
        return onTime;
    }

    public String getRegion() {
        return region;
    }

    public String getRepresentationProperty() {
        return getMAC();
    }

    public String getSerial() {
        return deviceId;
    }

    public String getType() {
        return type;
    }

    public String getFwVer() {
        return fwVer;
    }

    public String getHwVer() {
        return hwVer;
    }

    public String getMac() {
        return mac;
    }

    public String getCategory() {
        return category;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Boolean getOverheatStatus() {
        return overheatStatus;
    }

    public Integer getBindCount() {
        return bindCount;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public Integer getPosition() {
        return position;
    }

    public Boolean getDeviceOn() {
        return deviceOn;
    }

    public void setDeviceOn(Boolean deviceOn) {
        this.deviceOn = deviceOn;
    }
}
