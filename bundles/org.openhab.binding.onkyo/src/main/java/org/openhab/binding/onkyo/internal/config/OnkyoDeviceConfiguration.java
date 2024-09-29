/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onkyo.internal.config;

/**
 * Configuration class for Onkyo device.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnkyoDeviceConfiguration {

    public String ipAddress;
    public int port;
    public String udn;
    public int refreshInterval;
    public int volumeLimit;
    public double volumeScale = 1.0d;

    @Override
    public String toString() {
        String str = "";

        str += "ipAddress = " + ipAddress;
        str += ", port = " + port;
        str += ", udn = " + udn;
        str += ", refreshInterval = " + refreshInterval;
        str += ", volumeLimit = " + volumeLimit;
        str += ", volumeScale = " + volumeScale;

        return str;
    }
}
