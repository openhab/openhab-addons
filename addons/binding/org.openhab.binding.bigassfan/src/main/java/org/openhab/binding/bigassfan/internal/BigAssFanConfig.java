/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bigassfan.internal;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link BigAssFanConfig} is responsible for storing the BigAssFan thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BigAssFanConfig {
    private String label;
    private String ipAddress;
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
