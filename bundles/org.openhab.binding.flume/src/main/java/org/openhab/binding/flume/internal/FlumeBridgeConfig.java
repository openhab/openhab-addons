/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.flume.internal;

import static org.openhab.binding.flume.internal.FlumeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FlumeBridgeConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeBridgeConfig {
    public String clientId = "";
    public String clientSecret = "";
    public String username = "";
    public String password = "";

    public int refreshIntervalInstantaneous = DEFAULT_POLLING_INTERVAL_INSTANTANEOUS_MIN;
    public int refreshIntervalCumulative = DEFAULT_POLLING_INTERVAL_CUMULATIVE_MIN;
}
