/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Andi Bräu - Initial contribution
 */
public class DiscoveryResponse {
    @JsonProperty("sn")
    private String serialNumber = "";
    @JsonProperty("regid")
    private String regId = "";
    private String sak = "";
    private String name = "";
    @JsonProperty("mac")
    private String macAddress = "";
    @JsonProperty("ver")
    private String version = "";

    public DiscoveryResponse(String serialNumber, String regId, String sak, String name, String macAddress, String version) {
        this.serialNumber = serialNumber;
        this.regId = regId;
        this.sak = sak;
        this.name = name;
        this.macAddress = macAddress;
        this.version = version;
    }

    public DiscoveryResponse() {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryResponse that = (DiscoveryResponse) o;
        return serialNumber.equals(that.serialNumber) &&
                regId.equals(that.regId) &&
                sak.equals(that.sak) &&
                name.equals(that.name) &&
                macAddress.equals(that.macAddress) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber, regId, sak, name, macAddress, version);
    }
}