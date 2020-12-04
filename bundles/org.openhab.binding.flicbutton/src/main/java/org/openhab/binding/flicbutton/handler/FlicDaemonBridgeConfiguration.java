/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */
package org.openhab.binding.flicbutton.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Patrick Fink
 *
 */
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
