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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler} configuration
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public class BridgeConfiguration {
    private int autoReconnectPeriod = 0;
    private int readingPause = 0;
    private int readRetriesLimit = 0;
    private int responseTimeout = 0;

    public int getAutoReconnectPeriod() {
        return autoReconnectPeriod;
    }

    public int getReadingPause() {
        return readingPause;
    }

    public int getReadRetriesLimit() {
        return readRetriesLimit;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setAutoReconnectPeriod(int period) {
        autoReconnectPeriod = period;
    }
}
