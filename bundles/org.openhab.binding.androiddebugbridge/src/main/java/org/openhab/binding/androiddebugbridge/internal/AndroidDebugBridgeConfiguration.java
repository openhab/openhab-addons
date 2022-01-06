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
package org.openhab.binding.androiddebugbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AndroidDebugBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeConfiguration {
    /**
     * The IP address to use for connecting to the Android device.
     */
    public String ip = "";
    /**
     * Sample configuration parameter. Replace with your own.
     */
    public int port;
    /**
     * Time for scheduled state check.
     */
    public int refreshTime = 30;
    /**
     * Command timeout seconds.
     */
    public int timeout = 5;
    /**
     * Record input duration in seconds.
     */
    public int recordDuration = 5;
    /**
     * Configure media state detection behavior by package
     */
    public @Nullable String mediaStateJSONConfig;
}
