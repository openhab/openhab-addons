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
package org.openhab.binding.dscalarm.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the EyezOn Envisalink 3/2DS Ethernet interface.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
public class EnvisalinkBridgeDiscovery {
    private final Logger logger = LoggerFactory.getLogger(EnvisalinkBridgeDiscovery.class);

    static final int ENVISALINK_BRIDGE_PORT = 4025;
    static final int CONNECTION_TIMEOUT = 10;
    static final int SO_TIMEOUT = 15000;
    static final String ENVISALINK_DISCOVERY_RESPONSE = "505";

    private DSCAlarmBridgeDiscovery dscAlarmBridgeDiscovery = null;
    private String ipAddress;

    /**
     * Constructor.
     */
    public EnvisalinkBridgeDiscovery(DSCAlarmBridgeDiscovery dscAlarmBridgeDiscovery) {
        this.dscAlarmBridgeDiscovery = dscAlarmBridgeDiscovery;
    }

    /**
     * Method for Bridge Discovery.
     */
    public synchronized void discoverBridge() {
        logger.debug("Starting Envisalink Bridge Discovery.");

        CidrAddress localCidrAddress;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            localCidrAddress = NetUtil.getAllInterfaceAddresses().stream()
                    .filter(f -> f.getAddress() instanceof Inet4Address && f.getAddress().equals(localHost)).findFirst()
                    .orElse(null);
        } catch (UnknownHostException e) {
            logger.warn("discoverBridge(): UnknownHostException - {}", e.toString());
            return;
        }

        List<InetAddress> addressesToScan = localCidrAddress != null
                ? NetUtil.getAddressesRangeByCidrAddress(localCidrAddress, 16)
                : List.of();

        logger.debug("Performing discovery on {} ip addresses", addressesToScan.size());
        for (InetAddress inetAddress : addressesToScan) {
            try (Socket socket = new Socket()) {

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(32);
                socket.connect(new InetSocketAddress(inetAddress.getHostAddress(), ENVISALINK_BRIDGE_PORT),
                        CONNECTION_TIMEOUT);
                if (socket.isConnected()) {
                    String message = "";
                    socket.setSoTimeout(SO_TIMEOUT);
                    try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        message = input.readLine();
                    } catch (SocketTimeoutException e) {
                        logger.debug("discoverBridge(): No Message Read from Socket at [{}] - {}", ipAddress,
                                e.getMessage());
                        continue;
                    } catch (Exception e) {
                        logger.debug("discoverBridge(): Exception Reading from Socket at [{}]! {}", ipAddress,
                                e.toString());
                        continue;
                    }

                    if (message.substring(0, 3).equals(ENVISALINK_DISCOVERY_RESPONSE)) {
                        logger.debug("discoverBridge(): Bridge Found - [{}]!  Message - '{}'", ipAddress, message);
                        dscAlarmBridgeDiscovery.addEnvisalinkBridge(ipAddress);
                    } else {
                        logger.debug("discoverBridge(): No Response from Connection -  [{}]!  Message - '{}'",
                                ipAddress, message);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.debug("discoverBridge(): Illegal Argument Exception - {}", e.toString());
            } catch (SocketTimeoutException e) {
                logger.trace("discoverBridge(): No Connection on Port 4025! [{}]", ipAddress);
            } catch (SocketException e) {
                logger.debug("discoverBridge(): Socket Exception! [{}] - {}", ipAddress, e.toString());
            } catch (IOException e) {
                logger.debug("discoverBridge(): IO Exception! [{}] - {}", ipAddress, e.toString());
            }
        }
    }
}
