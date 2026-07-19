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
package org.openhab.binding.atv.internal.client.settings;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Settings related to RAOP.
 *
 * @param identifier device identifier for the RAOP service (may be {@code null})
 * @param credentials pairing credentials (may be {@code null})
 * @param password access password (may be {@code null})
 * @param protocolVersion protocol (AirPlay) version used (never {@code null})
 * @param timingPort server side (UDP) port used by timing server, 0 for a random free port
 * @param controlPort server side (UDP) port used by control server, 0 for a random free port
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record RaopSettings(@Nullable String identifier, @Nullable String credentials, @Nullable String password,
        AirPlayVersion protocolVersion, int timingPort, int controlPort) {

    /**
     * AirPlay version to use.
     */
    public enum AirPlayVersion {
        /** Automatically determine what version to use. */
        Auto("auto"),
        /** Use version 1 of AirPlay. */
        V1("1"),
        /** Use version 2 of AirPlay. */
        V2("2");

        private final String value;

        AirPlayVersion(String value) {
            this.value = value;
        }

        /**
         * Returns the string value used when persisting this setting.
         */
        public String value() {
            return value;
        }
    }

    /**
     * Creates empty default settings.
     *
     * @return default settings
     */
    public static RaopSettings ofDefaults() {
        return new RaopSettings(null, null, null, AirPlayVersion.Auto, 0, 0);
    }

    /**
     * Returns a copy with another identifier.
     *
     * @param identifier new identifier
     * @return updated copy
     */
    public RaopSettings withIdentifier(String identifier) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }

    /**
     * Returns a copy with other credentials.
     *
     * @param credentials new credentials
     * @return updated copy
     */
    public RaopSettings withCredentials(String credentials) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }

    /**
     * Returns a copy with another password.
     *
     * @param password new password
     * @return updated copy
     */
    public RaopSettings withPassword(String password) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }

    /**
     * Returns a copy with another protocol version.
     *
     * @param protocolVersion new protocol version
     * @return updated copy
     */
    public RaopSettings withProtocolVersion(AirPlayVersion protocolVersion) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }

    /**
     * Returns a copy with another timing port.
     *
     * @param timingPort new timing port
     * @return updated copy
     */
    public RaopSettings withTimingPort(int timingPort) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }

    /**
     * Returns a copy with another control port.
     *
     * @param controlPort new control port
     * @return updated copy
     */
    public RaopSettings withControlPort(int controlPort) {
        return new RaopSettings(identifier, credentials, password, protocolVersion, timingPort, controlPort);
    }
}
