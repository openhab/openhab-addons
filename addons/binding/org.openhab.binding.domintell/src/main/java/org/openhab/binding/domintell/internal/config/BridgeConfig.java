/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The {@link BridgeConfig} class is a Domintell connection configuration holder
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class BridgeConfig {
    /**
     * Class logger
     */
    private Logger logger = LoggerFactory.getLogger(BridgeConfig.class);

    /**
     * Host name or IP address of the DETH02 module
     */
    private String address;

    /**
     * Port of the DETH02 module
     */
    private Integer port;

    public Integer getPort() {
        return port;
    }

    public boolean isValid() {
        try {
            return address != null && port != null && InetAddress.getByName(address) != null;
        } catch (UnknownHostException e) {
            logger.debug("Invalid configuration: {}", e.getMessage(), e);
            return false;
        }
    }

    public InetAddress getInternetAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    @Override
    public String toString() {
        return "BridgeConfig{" +
                "address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
