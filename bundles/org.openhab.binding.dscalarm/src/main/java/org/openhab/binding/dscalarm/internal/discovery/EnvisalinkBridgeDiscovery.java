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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
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

        SubnetUtils subnetUtils = null;
        SubnetInfo subnetInfo = null;
        long lowIP = 0;
        long highIP = 0;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            subnetUtils = new SubnetUtils(localHost.getHostAddress() + "/"
                    + networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
            subnetInfo = subnetUtils.getInfo();
            lowIP = convertIPToNumber(subnetInfo.getLowAddress());
            highIP = convertIPToNumber(subnetInfo.getHighAddress());
        } catch (IllegalArgumentException e) {
            logger.error("discoverBridge(): Illegal Argument Exception - {}", e.toString());
            return;
        } catch (Exception e) {
            logger.error("discoverBridge(): Error - Unable to get Subnet Information! {}", e.toString());
            return;
        }

        logger.debug("   Local IP Address: {} - {}", subnetInfo.getAddress(),
                convertIPToNumber(subnetInfo.getAddress()));
        logger.debug("   Subnet:           {} - {}", subnetInfo.getNetworkAddress(),
                convertIPToNumber(subnetInfo.getNetworkAddress()));
        logger.debug("   Network Prefix:   {}", subnetInfo.getCidrSignature().split("/")[1]);
        logger.debug("   Network Mask:     {}", subnetInfo.getNetmask());
        logger.debug("   Low IP:           {}", convertNumberToIP(lowIP));
        logger.debug("   High IP:          {}", convertNumberToIP(highIP));

        for (long ip = lowIP; ip <= highIP; ip++) {
            try (Socket socket = new Socket()) {
                ipAddress = convertNumberToIP(ip);

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(32);
                socket.connect(new InetSocketAddress(ipAddress, ENVISALINK_BRIDGE_PORT), CONNECTION_TIMEOUT);
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

    /**
     * Convert an IP address to a number.
     *
     * @param ipAddress
     * @return
     */
    private long convertIPToNumber(String ipAddress) {
        String[] octets = ipAddress.split("\\.");

        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }

        long ip = 0;

        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);

            if (octet != (octet & 0xff)) {
                throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
            }

            ip |= octet << (i * 8);
        }

        return ip;
    }

    /**
     * Convert a number to an IP address.
     *
     * @param ip
     * @return
     */
    private String convertNumberToIP(long ip) {
        StringBuilder ipAddress = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            ipAddress.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                ipAddress.insert(0, '.');
            }

            ip = ip >> 8;
        }

        return ipAddress.toString();
    }
}
