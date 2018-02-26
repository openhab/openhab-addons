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

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;

/**
 * {@link KNXBridgeBaseThingHandler} configuration
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class BridgeConfiguration {

    private BigDecimal autoReconnectPeriod;
    private BigDecimal readingPause;
    private BigDecimal readRetriesLimit;
    private BigDecimal responseTimeout;

    public BigDecimal getAutoReconnectPeriod() {
        return autoReconnectPeriod;
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

}
