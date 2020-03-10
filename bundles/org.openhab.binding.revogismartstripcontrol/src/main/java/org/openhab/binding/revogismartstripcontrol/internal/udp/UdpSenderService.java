/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal.udp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * The {@link UdpSenderService} is responsible for sending and receiving udp packets
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class UdpSenderService {

    private static final int MAX_TIMEOUT_COUNT = 2;
    private static final int REVOGI_PORT = 8888;

    private final Logger logger = LoggerFactory.getLogger(UdpSenderService.class);
    private final DatagramSocketWrapper datagramSocketWrapper;

    public UdpSenderService(DatagramSocketWrapper datagramSocketWrapper) {
        this.datagramSocketWrapper = datagramSocketWrapper;
    }

    public List<UdpResponse> broadcastUpdDatagram(String content) {
        List<InetAddress> broadcastAddresses = new ArrayList<>();
        try {
            broadcastAddresses = getBroadcastAddresses();
        } catch (SocketException e) {
            logger.warn("Could not find broadcast addresse, got socket error {}", e.getMessage(), e);
        }
        return broadcastAddresses.stream()
                .map( address -> sendMessage(content, address))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public List<UdpResponse> sendMessage(String content, String ipAddress) {
        try {
            return sendMessage(content, InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            logger.warn("Could not find host with IP {}", ipAddress);
            return Collections.emptyList();
        }
    }

    private List<UdpResponse> sendMessage(String content, InetAddress inetAddress) {
        logger.debug("Using address {}", inetAddress);
        byte[] buf = content.getBytes(Charset.defaultCharset());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress, REVOGI_PORT);
        List<UdpResponse> responses = new ArrayList<>();
        try {
            datagramSocketWrapper.initSocket();
            datagramSocketWrapper.sendPacket(packet);
            responses = receiveResponses();
        } catch (IOException e) {
            logger.warn("Error sending message or reading anwser {}", e.getMessage(), e);
        } finally {
            datagramSocketWrapper.closeSocket();
        }
        return responses;
    }

    private List<UdpResponse> receiveResponses() throws IOException {
        List<UdpResponse> list = new ArrayList<>();
        int timeoutCounter = 0;
        while (timeoutCounter < MAX_TIMEOUT_COUNT) {
            byte[] receivedBuf = new byte[512];
            DatagramPacket answer = new DatagramPacket(receivedBuf, receivedBuf.length);
            try {
                datagramSocketWrapper.receiveAnswer(answer);
            } catch (SocketTimeoutException e) {
                timeoutCounter++;
                logger.info("Socket receive time no. {}", timeoutCounter);
                try {
                    TimeUnit.MILLISECONDS.sleep(timeoutCounter * 800L);
                } catch (InterruptedException ex) {
                    logger.warn("Interrupted sleep");
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (answer.getLength() > 0) {
                list.add(new UdpResponse(new String(answer.getData(), 0, answer.getLength()), answer.getAddress().getHostAddress()));
            }
        }

        return list;
    }

    private List<InetAddress> getBroadcastAddresses() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        List<InetAddress> broadcastAdresses = new ArrayList<>();
        while (networkInterfaces.hasMoreElements()) {
            broadcastAdresses.addAll(findInterfaceBroadcastAddresses(networkInterfaces));
        }
        return broadcastAdresses;
    }

    private List<InetAddress> findInterfaceBroadcastAddresses(final Enumeration<NetworkInterface> networkInterfaces) throws SocketException {
        NetworkInterface anInterface = networkInterfaces.nextElement();
        if (anInterface.isUp()) {
            List<InetAddress> addresses = anInterface.getInterfaceAddresses().stream()
                    .filter(address -> address.getBroadcast() != null)
                    .map(InterfaceAddress::getBroadcast)
                    .collect(toList());
            if (!addresses.isEmpty()) {
                return addresses;
            }
        }
        return Collections.emptyList();
    }
}
