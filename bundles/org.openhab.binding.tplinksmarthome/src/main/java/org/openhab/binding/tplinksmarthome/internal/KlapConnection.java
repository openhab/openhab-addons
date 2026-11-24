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
package org.openhab.binding.tplinksmarthome.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.binding.tplinkbridge.klap.KlapTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection that sends the Kasa JSON commands over KLAP instead of the legacy XOR protocol on port 9999. Kasa
 * firmware that secures the local protocol only accepts this.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KlapConnection extends Connection {

    /**
     * Devices set up in the Kasa app may hold a KLAP key derived from the account e-mail and this fixed password
     * instead of the account password.
     */
    static final String KASA_SETUP_PASSWORD = "kasaSetup";

    /**
     * Fixed credentials some devices hold instead of the account, as tried by python-kasa: the Kasa and Tapo
     * defaults, and blank credentials for a device that was never bound to a cloud account.
     */
    static final List<TPLinkCredentials> DEFAULT_CREDENTIALS = List.of(
            new TPLinkCredentials("kasa@tp-link.net", KASA_SETUP_PASSWORD),
            new TPLinkCredentials("test@tp-link.net", "test"), new TPLinkCredentials("", ""));

    private final Logger logger = LoggerFactory.getLogger(KlapConnection.class);
    private final KlapTransport transport;

    /**
     * @param httpClient started HTTP client
     * @param ipAddress ip address of the device
     * @param credentials account credentials, fetched at every login
     */
    public KlapConnection(HttpClient httpClient, @Nullable String ipAddress, Supplier<TPLinkCredentials> credentials) {
        super(ipAddress);
        transport = new KlapTransport(httpClient, ipAddress, () -> candidates(credentials.get()));
    }

    /**
     * @return the credentials to try in order, or none if the account has no credentials, so that a missing account
     *         isn't mistaken for a device that accepts the defaults
     */
    static List<TPLinkCredentials> candidates(TPLinkCredentials account) {
        if (!account.areSet()) {
            return List.of();
        }
        // The Kasa app derives the setup key from the e-mail as entered and trimmed and lowercased
        final Set<TPLinkCredentials> candidates = new LinkedHashSet<>();
        candidates.add(account);
        candidates.add(new TPLinkCredentials(account.username(), KASA_SETUP_PASSWORD));
        candidates.add(new TPLinkCredentials(account.username().trim().toLowerCase(Locale.ROOT), KASA_SETUP_PASSWORD));
        candidates.addAll(DEFAULT_CREDENTIALS);
        return new ArrayList<>(candidates);
    }

    @Override
    public void setIpAddress(String ipAddress) {
        super.setIpAddress(ipAddress);
        transport.setHost(ipAddress);
    }

    @Override
    public String sendCommand(String command) throws IOException {
        logger.trace("Executing command over KLAP: {}", command);
        String response = transport.exchange(command);
        logger.trace("Command response: {}", response);
        return response;
    }

    @Override
    public void close() {
        transport.close();
    }
}
