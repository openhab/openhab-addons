/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.discovery;

import java.net.InetAddress;

/**
 * The {@link KM200GatewayDiscoveryResult} class represents a discovery result obtained from network discovery of a
 * gateway.
 *
 * @author Markus Eckhardt
 *
 */
public class KM200GatewayDiscoveryResult {
    private InetAddress ip;

    public KM200GatewayDiscoveryResult(InetAddress ip) {
        super();
        this.ip = ip;
    }

    public InetAddress getIP() {
        return ip;
    }

}