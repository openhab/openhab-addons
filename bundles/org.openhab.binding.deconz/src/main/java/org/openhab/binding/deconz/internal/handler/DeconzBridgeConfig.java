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
package org.openhab.binding.deconz.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DeconzBridgeConfig} class holds the configuration properties of the bridge.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeconzBridgeConfig {
    public String host = "";
    public int httpPort = 80;
    public int port = 0;
    public @Nullable String apikey;
    int timeout = 2000;

    public String getHostWithoutPort() {
        String hostWithoutPort = host;
        if (hostWithoutPort.indexOf(':') > 0) {
            hostWithoutPort = hostWithoutPort.substring(0, hostWithoutPort.indexOf(':'));
        }
        return hostWithoutPort;
    }
}
