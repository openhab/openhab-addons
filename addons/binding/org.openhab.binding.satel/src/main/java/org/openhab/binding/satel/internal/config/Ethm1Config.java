/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * The {@link Ethm1Config} contains configuration values for Satel ETHM-1 bridge.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class Ethm1Config extends SatelBridgeConfig {

    public static final String HOST = "host";

    private String host;
    private int port;
    private String encryptionKey;

    /**
     * @return IP or hostname of the bridge
     */
    public String getHost() {
        return host;
    }

    /**
     * @return IP port the bridge listens to
     */
    public int getPort() {
        return port;
    }

    /**
     * @return key used to encrypt messages
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

}
