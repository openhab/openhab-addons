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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonHubLegacyConfiguration} is the configuration for an insteon hub legacy bridge.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonHubLegacyConfiguration extends InsteonBridgeConfiguration {

    private String hostname = "";
    private int port = 9761;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String getId() {
        return hostname + ":" + port;
    }

    @Override
    public String toString() {
        String s = "";
        s += " hostname=" + hostname;
        s += " port=" + port;
        s += super.toString();
        return s;
    }
}
