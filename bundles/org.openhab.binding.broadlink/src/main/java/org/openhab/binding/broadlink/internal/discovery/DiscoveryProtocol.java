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
package org.openhab.binding.broadlink.internal.discovery;

import java.net.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkProtocol;
import org.openhab.binding.broadlink.internal.NetworkUtils;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocket;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class DiscoveryProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryProtocol.class);

    private static class AsyncDiscoveryThread extends Thread {
        private final BroadlinkSocketListener listener;
        private final long timeoutMillis;
        private final DiscoveryFinishedListener finishedListener;

        AsyncDiscoveryThread(BroadlinkSocketListener listener, long timeoutMillis,
                DiscoveryFinishedListener finishedListener) {
            this.listener = listener;
            this.timeoutMillis = timeoutMillis;
            this.finishedListener = finishedListener;
        }

        public void run() {
            BroadlinkSocket.registerListener(listener);
            DiscoveryProtocol.discoverDevices();
            DiscoveryProtocol.waitUntilEnded(timeoutMillis);
            LOGGER.warn("Ended Broadlink device scan...");
            BroadlinkSocket.unregisterListener(listener);
            finishedListener.onDiscoveryFinished();
        }
    }

    public static void beginAsync(BroadlinkSocketListener listener, long discoveryTimeoutMillis,
            DiscoveryFinishedListener discoveryFinishedListener) {
        LOGGER.warn("Beginning async Broadlink device scan; will wait {} ms for responses", discoveryTimeoutMillis);
        AsyncDiscoveryThread adt = new AsyncDiscoveryThread(listener, discoveryTimeoutMillis,
                discoveryFinishedListener);
        adt.start();
    }

    public static void discoverDevices() {
        try {
            InetAddress localAddress = NetworkUtils.getLocalHostLANAddress();
            int localPort = NetworkUtils.nextFreePort(localAddress, 1024, 3000);
            byte message[] = BroadlinkProtocol.buildDiscoveryPacket(localAddress.getHostAddress(), localPort);
            BroadlinkSocket.sendMessage(message, "255.255.255.255", 80);
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to initiate discovery", e);
        }
    }

    private static void waitUntilEnded(long discoveryTimeoutMillis) {
        try {
            LOGGER.warn("Broadlink device scan waiting for {} ms to complete ...", discoveryTimeoutMillis);
            Thread.sleep(discoveryTimeoutMillis);
            LOGGER.warn("Device scan: wait complete ...");

        } catch (InterruptedException e) {
            LOGGER.error("problem {}", e.getMessage());
        }
    }
}
