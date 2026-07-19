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
package org.openhab.binding.fronius.internal.api;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.firmware.types.SemverVersion;

/**
 * The {@link FroniusConfigApiEndpoint} identifies the <code>/config</code> HTTP endpoints of a Fronius inverter and the
 * credentials to access them.
 *
 * @param baseUri the base URI of the inverter, MUST NOT end with a slash
 * @param firmwareVersion the firmware version of the inverter, which determines the hash algorithm to use
 * @param username the username for the inverter Web UI
 * @param password the password for the inverter Web UI
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
@NonNullByDefault
public record FroniusConfigApiEndpoint(URI baseUri, SemverVersion firmwareVersion, String username, String password) {

    @Override
    public String toString() {
        // Do not expose the password, as this is used for logging
        return "FroniusConfigApiEndpoint[baseUri=" + baseUri + ", firmwareVersion=" + firmwareVersion + ", username="
                + username + "]";
    }
}
