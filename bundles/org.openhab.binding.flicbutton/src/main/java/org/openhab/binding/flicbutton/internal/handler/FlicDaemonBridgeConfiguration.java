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
package org.openhab.binding.flicbutton.internal.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Patrick Fink - Initial contribution
 *
 */
public class FlicDaemonBridgeConfiguration {

    private String hostname;
    private int port;

    private InetAddress host;

    public void initAndValidate() throws UnknownHostException {
        host = InetAddress.getByName(hostname);
    }

    public InetAddress getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
