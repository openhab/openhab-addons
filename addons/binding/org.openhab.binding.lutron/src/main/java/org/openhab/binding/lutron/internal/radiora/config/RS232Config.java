/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
