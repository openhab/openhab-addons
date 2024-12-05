/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkProtocol;
import org.openhab.binding.broadlink.internal.NetworkUtils;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocket;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocketListener;
import org.slf4j.Logger;

/**
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class DiscoveryProtocol {

    private static class AsyncDiscoveryThread extends Thread {
        private final BroadlinkSocketListener listener;
        private final long timeoutMillis;
        private final DiscoveryFinishedListener finishedListener;
        private final Logger logger;

        AsyncDiscoveryThread(BroadlinkSocketListener listener, long timeoutMillis,
                DiscoveryFinishedListener finishedListener, Logger logger) {
            super(String.format("OH-binding-%s-%s", BroadlinkBindingConstants.BINDING_ID, "Discovery"));
            this.listener = listener;
            this.timeoutMillis = timeoutMillis;
            this.finishedListener = finishedListener;
            this.logger = logger;
        }

        @Override
        public void run() {
            BroadlinkSocket.registerListener(listener, logger);
            DiscoveryProtocol.discoverDevices(logger);
            DiscoveryProtocol.waitUntilEnded(timeoutMillis, logger);
            BroadlinkSocket.unregisterListener(listener, logger);
            finishedListener.onDiscoveryFinished();
        }
    }

    public static void beginAsync(BroadlinkSocketListener listener, long discoveryTimeoutMillis,
            DiscoveryFinishedListener discoveryFinishedListener, Logger logger) {
        AsyncDiscoveryThread adt = new AsyncDiscoveryThread(listener, discoveryTimeoutMillis, discoveryFinishedListener,
                logger);
        adt.start();
    }

    public static void discoverDevices(Logger logger) {
        try {
            InetAddress localAddress = NetworkUtils.getLocalHostLANAddress();
            int localPort = NetworkUtils.nextFreePort(localAddress, 1024, 3000);
            byte message[] = BroadlinkProtocol.buildDiscoveryPacket(localAddress.getHostAddress(), localPort);
            BroadlinkSocket.sendMessage(message, "255.255.255.255", 80, logger);
        } catch (UnknownHostException e) {
            logger.warn("Failed to initiate discovery: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to find free port: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Cannot find a port to discovber new devices");
        }
    }

    private static void waitUntilEnded(long discoveryTimeoutMillis, Logger logger) {
        try {
            Thread.sleep(discoveryTimeoutMillis);
        } catch (InterruptedException e) {
            logger.warn("Unexpected problem during discovery: {}", e.getMessage());
        }
    }
}
