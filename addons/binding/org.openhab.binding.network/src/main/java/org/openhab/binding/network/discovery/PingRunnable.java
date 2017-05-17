/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.discovery;

import org.eclipse.smarthome.model.script.actions.Ping;
import org.openhab.binding.network.service.NetworkUtils;

/**
 * This runnable pings the given IP address and is used by the {@see NetworkDiscoveryService}.
 * If the java ping does not work, a native ping will be tried. This procedure is necessary,
 * because in some OS versions (e.g. Windows 7) the java ping does not work reliably.
 *
 * @author David Graeff <david.graeff@web.de>
 */
class PingRunnable implements Runnable {
    final String ip;
    final NetworkDiscoveryService service;

    public PingRunnable(String ip, NetworkDiscoveryService service) {
        this.ip = ip;
        this.service = service;
        if (ip == null) {
            throw new RuntimeException("ip may not be null!");
        }
    }

    @Override
    public void run() {
        try {
            if (Ping.checkVitality(ip, 0, NetworkDiscoveryService.PING_TIMEOUT_IN_MS)) {
                service.newDevice(ip);
            } else if (NetworkUtils.nativePing(ip, 0, NetworkDiscoveryService.PING_TIMEOUT_IN_MS)) {
                service.newDevice(ip);
            }
        } catch (Exception e) {
        }
    }
}
