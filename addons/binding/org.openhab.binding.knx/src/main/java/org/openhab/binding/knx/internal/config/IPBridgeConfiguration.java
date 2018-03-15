/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.config;

import java.math.BigDecimal;

/**
 * IP Bridge handler configuration object.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class IPBridgeConfiguration extends BridgeConfiguration {

    private boolean useNAT;
    private String type;
    private String ipAddress;
    private BigDecimal portNumber;
    private String localIp;
    private String localSourceAddr;

    public Boolean getUseNAT() {
        return useNAT;
    }

    public String getType() {
        return type;
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
