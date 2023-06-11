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
package org.openhab.binding.lcn.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PckGatewayConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class PckGatewayConfiguration {
    private @NonNullByDefault({}) String hostname;
    private int port;
    private @NonNullByDefault({}) String username;
    private @NonNullByDefault({}) String password;
    private @NonNullByDefault({}) String mode;
    private @NonNullByDefault({}) int timeoutMs;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMode() {
        return mode;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
