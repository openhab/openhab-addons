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
package org.openhab.binding.smartthings.internal.ocf;

import java.util.HexFormat;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Appliance connection and optional imported credentials.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ApplianceConfiguration {
    public String host = "";
    public int port;
    public int clientPort;
    public int refreshInterval = 60;
    public int timeout = 12;
    public String deviceId = "";
    public String ownerId = "";
    public String ownerPsk = "";
    public String keyStore = "";
    public String keyStorePassword = "";
    public String serverFingerprint = "";

    void validate() {
        if (host.isBlank() || !host.matches("[A-Za-z0-9_.:%-]+") || port < 0 || port > 65535 || clientPort < 0
                || clientPort > 65535 || refreshInterval < 10 || timeout < 1 || timeout > 60) {
            throw new IllegalArgumentException("Check host, ports, refresh interval and timeout");
        }
        if (!deviceId.isBlank()) {
            uuid(deviceId);
        }
        if (!ownerPsk.isBlank()) {
            if (!keyStore.isBlank() || !keyStorePassword.isEmpty() || !serverFingerprint.isBlank()) {
                throw new IllegalArgumentException("Configure either OwnerPSK or a client key store, not both");
            }
            uuid(ownerId);
            int length = psk().length;
            if (length != 16 && length != 32) {
                throw new IllegalArgumentException("OwnerPSK must contain 16 or 32 bytes in hexadecimal");
            }
        } else {
            if (!ownerId.isBlank() || keyStore.isBlank() && !keyStorePassword.isEmpty()) {
                throw new IllegalArgumentException("Incomplete imported credentials");
            }
            if (!serverFingerprint.isBlank() && fingerprint().length != 32) {
                throw new IllegalArgumentException("A SHA-256 certificate fingerprint is required");
            }
        }
    }

    static UUID uuid(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            if (!uuid.toString().equalsIgnoreCase(value) || uuid.equals(new UUID(0, 0))) {
                throw new IllegalArgumentException();
            }
            return uuid;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("A canonical non-zero UUID is required");
        }
    }

    byte[] psk() {
        return hex(ownerPsk);
    }

    byte[] fingerprint() {
        return hex(serverFingerprint.replace(":", "").strip());
    }

    private static byte[] hex(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Credentials must use valid hexadecimal encoding");
        }
    }
}
