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
package org.openhab.binding.etherrain.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EtherRainUdpResponse} is an encapsulation of the UDP broadcast response from the EtherRain
 *
 * @author Joe Inkenbrandt - Initial contribution
 */

@NonNullByDefault
public class EtherRainUdpResponse {
    private final boolean valid;
    private final String type;
    private final String address;
    private final int port;
    private final String uniqueName;
    private final String additionalParameters; // Note: version 3.77 of spec says this is unused

    public EtherRainUdpResponse(String type, String address, int port, String uniqueName, String additionalParameters) {
        this.valid = true;
        this.type = type;
        this.address = address;
        this.port = port;
        this.uniqueName = uniqueName;
        this.additionalParameters = additionalParameters;
    }

    public EtherRainUdpResponse() {
        this.valid = false;
        this.type = "";
        this.address = "";
        this.port = 0;
        this.uniqueName = "";
        this.additionalParameters = "";
    }

    public boolean isValid() {
        return valid;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getUnqiueName() {
        return uniqueName;
    }

    public String getAdditionalParameters() {
        return additionalParameters;
    }
}
