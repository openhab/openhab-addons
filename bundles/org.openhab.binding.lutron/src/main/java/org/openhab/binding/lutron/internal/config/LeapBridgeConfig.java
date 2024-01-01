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
package org.openhab.binding.lutron.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.internal.handler.LeapBridgeHandler}.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class LeapBridgeConfig {
    public @Nullable String ipAddress;
    public int port = 8081;
    public @Nullable String keystore;
    public @Nullable String keystorePassword;
    public boolean certValidate = false;
    public int reconnect;
    public int heartbeat;
    public int delay = 0;
}
