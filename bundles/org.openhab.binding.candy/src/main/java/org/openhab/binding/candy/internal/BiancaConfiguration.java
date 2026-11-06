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
package org.openhab.binding.candy.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.candy.internal.protocol.CandyCodec;

/**
 * Configuration of a manually configured Bianca on the local network.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class BiancaConfiguration {
    public String hostName = "";
    public String encryptionKey = "";
    public String username = "";
    public String password = "";
    public String applianceId = "";
    public boolean enableControl;
    public int refreshInterval = 30;
    public int requestTimeout = 10;

    /**
     * Returns a localized configuration error, or an empty optional when the configuration is valid.
     */
    public Optional<String> validate() {
        if (statusUri().isEmpty()) {
            return Optional.of("@text/config-error-host");
        }
        if (!encryptionKey.isEmpty() && !CandyCodec.isSupportedKey(encryptionKey)) {
            return Optional.of("@text/config-error-key");
        }
        if (encryptionKey.isEmpty() && (username.isBlank() || password.isEmpty())) {
            return Optional.of("@text/config-error-account");
        }
        if (!applianceId.isEmpty() && !applianceId.matches("[A-Za-z0-9_-]{1,128}")) {
            return Optional.of("@text/config-error-appliance-id");
        }
        if (refreshInterval < 15 || requestTimeout < 1 || requestTimeout > 10) {
            return Optional.of("@text/config-error-interval");
        }
        return Optional.empty();
    }

    /**
     * Returns the fixed status URI if the configured host is valid.
     */
    public Optional<URI> statusUri() {
        try {
            URI uri = new URI("http", null, hostName, -1, "/http-read.json", null, null).parseServerAuthority();
            if (uri.getHost() == null || hostName.isBlank() || uri.getPort() != -1 || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null
                    || !"/http-read.json".equals(uri.getRawPath())) {
                return Optional.empty();
            }
            return Optional.of(uri);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}
