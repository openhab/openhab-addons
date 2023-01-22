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
package org.openhab.binding.revogi.internal.api;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andi Bräu - Initial contribution
 */
public class DiscoveryResponseDTO {
    @SerializedName("sn")
    private final String serialNumber;
    @SerializedName("regid")
    private final String regId;
    private final String sak;
    private final String name;
    @SerializedName("mac")
    private final String macAddress;
    @SerializedName("ver")
    private final String version;

    public DiscoveryResponseDTO(String serialNumber, String regId, String sak, String name, String macAddress,
            String version) {
        this.serialNumber = serialNumber;
        this.regId = regId;
        this.sak = sak;
        this.name = name;
        this.macAddress = macAddress;
        this.version = version;
    }

    public DiscoveryResponseDTO() {
        serialNumber = "";
        regId = "";
        sak = "";
        name = "";
        macAddress = "";
        version = "";
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getRegId() {
        return regId;
    }

    public String getSak() {
        return sak;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoveryResponseDTO that = (DiscoveryResponseDTO) o;
        return serialNumber.equals(that.serialNumber) && regId.equals(that.regId) && sak.equals(that.sak)
                && name.equals(that.name) && macAddress.equals(that.macAddress) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber, regId, sak, name, macAddress, version);
    }
}
