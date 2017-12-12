/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.config;

import java.math.BigDecimal;

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;

/**
 * {@link KNXBridgeBaseThingHandler} configuration
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class IPBridgeConfiguration extends BridgeConfiguration {

    private boolean useNAT;
    private String ipConnectionType;
    private String ipAddress;
    private BigDecimal portNumber;
    private String localIp;
    private String localSourceAddr;

    public Boolean getUseNAT() {
        return useNAT;
    }

    public String getIpConnectionType() {
        return ipConnectionType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public BigDecimal getPortNumber() {
        return portNumber;
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getLocalSourceAddr() {
        return localSourceAddr;
    }

}
