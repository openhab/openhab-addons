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
package org.openhab.binding.lutron.internal.radiora.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for RS232 thing type.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class RS232Config {

    public String portName = "";
    public int baud = 9600;
    public int zoneMapQueryInterval = 60;

    public String getPortName() {
        return portName;
    }

    public int getBaud() {
        return baud;
    }

    public int getZoneMapQueryInterval() {
        return zoneMapQueryInterval;
    }
}
