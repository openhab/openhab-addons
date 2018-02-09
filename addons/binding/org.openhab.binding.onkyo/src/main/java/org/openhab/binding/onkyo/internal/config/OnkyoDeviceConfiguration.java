/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.internal.config;

/**
 * Configuration class for {@link OnkyoBinding} device.
 *
 * @author Pauli Anttila - Initial contribution
 */

public class OnkyoDeviceConfiguration {

    public String ipAddress;
    public int port;
    public String udn;
    public int refreshInterval;
    public int volumeLimit;

    @Override
    public String toString() {
        String str = "";

        str += "ipAddress = " + ipAddress;
        str += ", port = " + port;
        str += ", udn = " + udn;
        str += ", refreshInterval = " + refreshInterval;
        str += ", volumeLimit = " + volumeLimit;

        return str;
    }
}
