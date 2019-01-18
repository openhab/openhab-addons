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
package org.openhab.binding.bigassfan.internal;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link BigAssFanConfig} is responsible for storing the BigAssFan thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BigAssFanConfig {
    /**
     * Name of the device
     */
    private String label;

    /**
     * IP address of the device
     */
    private String ipAddress;

    /**
     * MAC address of the device
     */
    private String macAddress;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isValid() {
        if (StringUtils.isBlank(label)) {
            return false;
        }
        if (StringUtils.isBlank(ipAddress)) {
            return false;
        }
        if (StringUtils.isBlank(macAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BigAssFanConfig{label=" + label + ", ipAddress=" + ipAddress + ", macAddress=" + macAddress + "}";
    }
}
