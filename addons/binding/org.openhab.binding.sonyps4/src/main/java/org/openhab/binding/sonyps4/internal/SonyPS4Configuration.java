/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     * IP Address of PS4.
     */
    private String ipAddress;

    /**
     * UID of PS4.
     */
    private String uid;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "IP" + ipAddress + ", UID" + uid + ".";
    }
}
