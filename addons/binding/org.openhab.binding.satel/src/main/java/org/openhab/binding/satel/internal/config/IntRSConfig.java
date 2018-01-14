/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.config;

/**
 * The {@link IntRSConfig} contains configuration values for Satel INT-RS bridge.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class IntRSConfig extends SatelBridgeConfig {

    public static final String PORT = "port";

    private String port;

    /**
     * @return serial port to which the module is connected
     */
    public String getPort() {
        return port;
    }

}
