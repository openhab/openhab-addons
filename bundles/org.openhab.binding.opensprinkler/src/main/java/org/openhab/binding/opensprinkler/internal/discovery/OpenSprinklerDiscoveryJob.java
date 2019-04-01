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
package org.openhab.binding.opensprinkler.internal.discovery;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.DISCOVERY_DEFAULT_IP_TIMEOUT_RATE;
import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerDiscoveryJob} class allow manual discovery of
 * OpenSprinkler devices for a single IP address. This is used
 * for threading to make discovery faster.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerDiscoveryJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDiscoveryJob.class);

    private OpenSprinklerDiscoveryService discoveryClass;

    private String ipAddress;

    public OpenSprinklerDiscoveryJob(OpenSprinklerDiscoveryService service, String ip) {
        this.discoveryClass = service;
        this.ipAddress = ip;
    }

    @Override
    public void run() {
        if (hasOpenSprinklerDevice(this.ipAddress)) {
            discoveryClass.submitDiscoveryResults(this.ipAddress);
        }
    }

    /**
     * Determines if an OpenSprinkler device is available at a given IP address.
     *
     * @param ip IP address of the OpenSprinkler device as a string.
     * @return True if a device is found, false if not.
     */
    private boolean hasOpenSprinklerDevice(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);

            if (canEstablishConnection(address, DEFAULT_API_PORT)) {
                OpenSprinklerApi openSprinkler = OpenSprinklerApiFactory.getHttpApi(ip, DEFAULT_API_PORT,
                        DEFAULT_ADMIN_PASSWORD);

                return (openSprinkler != null);
            } else {
                logger.trace("No OpenSprinkler device found at IP address ({})", ip);

                return false;
            }
        } catch (Exception exp) {
            logger.debug("No OpenSprinkler device found at IP address ({}) because of error: {}", ip, exp.getMessage());

            return false;
        }
    }

    /**
     * Tries to establish a connection to a hostname and port.
     *
     * @param host Hostname or IP address to connect to.
     * @param port Port to attempt to connect to.
     * @return True if a connection can be established, false if not.
     */
    private boolean canEstablishConnection(InetAddress host, int port) {
        boolean reachable = false;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), DISCOVERY_DEFAULT_IP_TIMEOUT_RATE);

            reachable = true;
        } catch (IOException e) {
            // do nothing
        }

        return reachable;
    }
}
