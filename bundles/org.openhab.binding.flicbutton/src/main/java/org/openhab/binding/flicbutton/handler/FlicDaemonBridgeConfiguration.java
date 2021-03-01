/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Patrick Fink - Initial contribution
 *
 */
@NonNullByDefault
public class FlicDaemonBridgeConfiguration {

    private final InetAddress hostname;
    private final int port;

    FlicDaemonBridgeConfiguration(InetAddress hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    FlicDaemonBridgeConfiguration(Object rawHostname, Object rawPort)
            throws UnknownHostException, IllegalArgumentException {
        if (rawHostname == null || rawPort == null) {
            throw new IllegalArgumentException("Hostname and port must not be null");
        }
        this.hostname = parseBridgeHostname(rawHostname);
        this.port = parseBridgePort(rawPort);
    }

    private InetAddress parseBridgeHostname(Object rawHostname) throws UnknownHostException {
        String host_config = ((rawHostname instanceof String) ? (String) rawHostname
                : (rawHostname instanceof InetAddress) ? ((InetAddress) rawHostname).getHostAddress() : null);

        return InetAddress.getByName(host_config);
    }

    private int parseBridgePort(Object rawPort) {
        return Integer.parseInt(rawPort.toString());
    }

    public InetAddress getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
