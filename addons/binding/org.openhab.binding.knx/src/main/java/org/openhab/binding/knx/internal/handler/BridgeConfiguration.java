/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

import java.math.BigDecimal;

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;

/**
 * {@link KNXBridgeBaseThingHandler} configuration
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class BridgeConfiguration {

    private BigDecimal autoReconnectPeriod;
    private Boolean enableDiscovery;
    private String knxProj;
    private BigDecimal readingPause;
    private BigDecimal readRetriesLimit;
    private BigDecimal responseTimeout;
    private Boolean useNAT;

    public BigDecimal getAutoReconnectPeriod() {
        return autoReconnectPeriod;
    }

    public Boolean getEnableDiscovery() {
        return enableDiscovery;
    }

    public String getKnxProj() {
        return knxProj;
    }

    public BigDecimal getReadingPause() {
        return readingPause;
    }

    public BigDecimal getReadRetriesLimit() {
        return readRetriesLimit;
    }

    public BigDecimal getResponseTimeout() {
        return responseTimeout;
    }

    public Boolean getUseNAT() {
        return useNAT;
    }

}
