/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration of a wallbox using all available local protocols.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class KeContactCombinedConfiguration {
    public String ipAddress = "";
    public int refreshIntervalSlow = 60;
    public boolean modbusEnabled = true;
    public String modbusIpAddress = "";
    public int port = 502;
    public int unitId = 255;
    public int refreshInterval = 12;
    public boolean udpEnabled = true;
    public boolean udpDisplayOnly = false;
    public boolean restEnabled = false;
    public int restPort = 8443;
    public String username = "admin";
    public @Nullable String password;
    public boolean verifyCertificate = true;

    public String getModbusAddress() {
        return modbusIpAddress.isBlank() ? ipAddress : modbusIpAddress;
    }

    public String getRestBaseUrl() {
        try {
            URI uri = new URI("https", null, ipAddress, restPort, null, null, null);
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getPort() != restPort
                    || !"".equals(uri.getRawPath()) || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("Invalid wallbox network address");
            }
            return Objects.requireNonNull(uri.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid wallbox network address", e);
        }
    }
}
