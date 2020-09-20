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
package org.openhab.binding.lutron.internal.radiora.config;

/**
 * Configuration class for RS232 thing type.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class RS232Config {

    private String portName;
    private int baud = 9600;
    private int zoneMapQueryInterval = 60;

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public int getZoneMapQueryInterval() {
        return zoneMapQueryInterval;
    }

    public void setZoneMapQueryInterval(int zoneMapQueryInterval) {
        this.zoneMapQueryInterval = zoneMapQueryInterval;
    }
}
