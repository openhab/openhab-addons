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
package org.openhab.binding.kvv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds information parameters.
 *
 * @author Maximilian Hess - Initial contribution
 *
 */
@NonNullByDefault
public class KVVBridgeConfig {

    /** maximum number of traines being queried */
    public int maxTrains;

    /** the update interval in seconds */
    public int updateInterval;

    /** API key of the KVV API */
    public String apiKey;

    public KVVBridgeConfig() {
        this.maxTrains = 0;
        this.updateInterval = 10;
        this.apiKey = "";
    }
}
