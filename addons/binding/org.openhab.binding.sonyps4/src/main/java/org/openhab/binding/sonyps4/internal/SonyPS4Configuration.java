/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sonyps4.internal;

import org.openhab.binding.sonyps4.internal.discovery.SonyPS4Discovery;

/**
 * The {@link SonyPS4Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class SonyPS4Configuration {

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipAddress}.
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #userCredential}.
     */
    public static final String USER_CREDENTIAL = "userCredential";

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipPort}.
     */
    public static final String IP_PORT = "ipPort";

    /**
     * IP-address of PS4.
     */
    private String ipAddress;

    /**
     * User-credential for the PS4.
     */
    private String userCredential;

    /**
     * IP-port of PS4.
     */
    private Integer ipPort;

    /**
     * Size of artwork for applications.
     */
    private Integer artworkSize;

    /**
     * host-id of PS4.
     */
    private String hostId;

    /**
     * pin code for user.
     */
    private String pinCode;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserCredential() {
        return userCredential;
    }

    public void setUserCredential(String userCredential) {
        this.userCredential = userCredential;
    }

    public Integer getIpPort() {
        return ipPort;
    }

    public void setIpPort(Integer ipPort) {
        this.ipPort = ipPort;
    }

    public Integer getArtworkSize() {
        return artworkSize;
    }

    public void setArtworkSize(Integer artworkSize) {
        this.artworkSize = artworkSize;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    @Override
    public String toString() {
        return "IP" + ipAddress + ", User-credential" + userCredential + ", Port" + ipPort + ", HostId" + hostId + ".";
    }
}
