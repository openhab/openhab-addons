/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
