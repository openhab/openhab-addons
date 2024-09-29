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
package org.openhab.binding.knx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * IP Bridge handler configuration object.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public class IPBridgeConfiguration extends BridgeConfiguration {

    private boolean useNAT = false;
    private String type = "";
    private String ipAddress = "";
    private int portNumber = 0;
    private String localIp = "";
    private String localSourceAddr = "";
    private String routerBackboneKey = "";
    private String tunnelUserId = "";
    private String tunnelUserPassword = "";
    private String tunnelDeviceAuthentication = "";
    private String tunnelSourceAddress = "";

    public Boolean getUseNAT() {
        return useNAT;
    }

    public String getType() {
        return type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getLocalSourceAddr() {
        return localSourceAddr;
    }

    public String getRouterBackboneKey() {
        return routerBackboneKey;
    }

    public String getTunnelUserId() {
        return tunnelUserId;
    }

    public String getTunnelUserPassword() {
        return tunnelUserPassword;
    }

    public String getTunnelDeviceAuthentication() {
        return tunnelDeviceAuthentication;
    }

    public String getTunnelSourceAddress() {
        return tunnelSourceAddress;
    }
}
