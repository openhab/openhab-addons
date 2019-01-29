/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etherrain.internal.api;

public class EtherRainUdpResponse {
    private String type;
    private String address;
    private int port;
    private String uniqueName;
    private String additionalParameters; // Note: version 3.77 of spec says this is unused

    public EtherRainUdpResponse(String type, String address, int port, String uniqueName, String additionalParameters) {
        this.type = type;
        this.address = address;
        this.port = port;
        this.uniqueName = uniqueName;
        this.additionalParameters = additionalParameters;
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
