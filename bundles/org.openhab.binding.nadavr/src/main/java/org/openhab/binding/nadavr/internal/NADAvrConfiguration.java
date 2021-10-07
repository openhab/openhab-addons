/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal;

import java.math.BigDecimal;

/**
 * The {@link NADAvrConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public class NADAvrConfiguration {

    /**
     * The host name of the NAD A/V Receiver
     */
    public String hostname;

    /**
     * The IP Address for the NAD A/V Receiver
     */
    public String ipAddress;

    /**
     * The telnet port used for connecting the telnet session for the NAD A/V Receiver
     */
    public int telnetPort;

    /**
     * Telnet is enabled vs serial
     *
     * @return telnetEnabled
     */
    public boolean telnetEnabled;

    private Integer zoneCount;

    // Default maximum volume
    public static final BigDecimal MAX_VOLUME = new BigDecimal("99");

    private BigDecimal mainVolumeMax = MAX_VOLUME;

    public BigDecimal getMainVolumeMax() {
        return mainVolumeMax;
    }

    public void setMainVolumeMax(BigDecimal mainVolumeMax) {
        this.mainVolumeMax = mainVolumeMax;
    }

    public Integer getZoneCount() {
        return zoneCount;
    }

    public void setZoneCount(Integer zoneCount) {
        this.zoneCount = zoneCount;
    }

    public boolean isTelnet() {
        return telnetEnabled;
    }

    public void setTelnet(boolean telnetEnabled) {
        this.telnetEnabled = telnetEnabled;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getTelnetPort() {
        return telnetPort;
    }

    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }

    @Override
    public String toString() {
        return "NADAvrConfiguration [hostname=" + hostname + ", ipAddress=" + ipAddress + ", telnetPort=" + telnetPort
                + ", telnetEnabled=" + telnetEnabled + ", zoneCount=" + zoneCount + "]";
    }

}
