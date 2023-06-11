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
package org.openhab.binding.satel.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IntRSConfig} contains configuration values for Satel INT-RS bridge.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntRSConfig extends SatelBridgeConfig {

    public static final String PORT = "port";

    private String port = "";

    /**
     * @return serial port to which the module is connected
     */
    public String getPort() {
        return port;
    }
}
