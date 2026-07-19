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
 * Settings related to AirPlay.
 *
 * @param identifier device identifier for the AirPlay service (may be {@code null})
 * @param credentials pairing credentials (may be {@code null})
 * @param password access password (may be {@code null})
 * @param mrpTunnel how MRP tunneling over AirPlay is handled (never {@code null})
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record AirPlaySettings(@Nullable String identifier, @Nullable String credentials, @Nullable String password,
        MrpTunnel mrpTunnel) {

    /**
     * How MRP tunneling over AirPlay is handled.
     */
    public enum MrpTunnel {
        /** Automatically set up MRP tunnel if supported by remote device. */
        Auto("auto"),
        /** Force set up of MRP tunnel even if remote device does not support it. */
        Force("force"),
        /** Fully disable set up of MRP tunnel. */
        Disable("disable");

        private final String value;

        MrpTunnel(String value) {
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
    public static AirPlaySettings ofDefaults() {
        return new AirPlaySettings(null, null, null, MrpTunnel.Auto);
    }

    /**
     * Returns a copy with another identifier.
     *
     * @param identifier new identifier
     * @return updated copy
     */
    public AirPlaySettings withIdentifier(String identifier) {
        return new AirPlaySettings(identifier, credentials, password, mrpTunnel);
    }

    /**
     * Returns a copy with other credentials.
     *
     * @param credentials new credentials
     * @return updated copy
     */
    public AirPlaySettings withCredentials(String credentials) {
        return new AirPlaySettings(identifier, credentials, password, mrpTunnel);
    }

    /**
     * Returns a copy with another password.
     *
     * @param password new password
     * @return updated copy
     */
    public AirPlaySettings withPassword(String password) {
        return new AirPlaySettings(identifier, credentials, password, mrpTunnel);
    }

    /**
     * Returns a copy with another MRP tunnel mode.
     *
     * @param mrpTunnel new MRP tunnel mode
     * @return updated copy
     */
    public AirPlaySettings withMrpTunnel(MrpTunnel mrpTunnel) {
        return new AirPlaySettings(identifier, credentials, password, mrpTunnel);
    }
}
