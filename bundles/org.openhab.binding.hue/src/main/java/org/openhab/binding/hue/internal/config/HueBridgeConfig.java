/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;

/**
 * Configuration for the {@link HueBridgeHandler}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HueBridgeConfig {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public @Nullable String ipAddress;
    public @Nullable Integer port;
    public String protocol = HTTPS;
    public boolean useSelfSignedCertificate = true;
    public @Nullable String userName;
    public int pollingInterval = 10;
    public int sensorPollingInterval = 500;

    public int getPort() {
        Integer thePort = port;
        return (thePort != null) ? thePort.intValue() : HTTPS.equals(protocol) ? 443 : 80;
    }
}
